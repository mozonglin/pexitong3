package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 超级管理员或校级管理员：按 checkuser 学校维度开户 / 新建学校
 * 校级管理员仅能操作自己的学校，且不能创建新学校
 */
@RestController
@RequestMapping("/checkuser/school-account")
@CrossOrigin(origins = "*")
public class SchoolAccountController {

    private static final List<User.UserType> REGISTERED_TEACHER_ROLES = List.of(
        User.UserType.teacher,
        User.UserType.department_admin,
        User.UserType.school_admin
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User requireSchoolAdminOrAbove(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("未提供认证 Token");
        }
        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new SecurityException("用户不存在"));
        if (user.getUserType() != User.UserType.super_admin
                && user.getUserType() != User.UserType.school_admin) {
            throw new SecurityException("权限不足，仅超级管理员或校级管理员可操作");
        }
        return user;
    }

    private void requireSuperAdmin(HttpServletRequest request) {
        User user = requireSchoolAdminOrAbove(request);
        if (user.getUserType() != User.UserType.super_admin) {
            throw new SecurityException("权限不足，仅超级管理员可操作");
        }
    }

    private void enforceSchoolScope(User admin, String school) {
        if (admin.getUserType() == User.UserType.school_admin) {
            String adminSchool = admin.getSchool();
            if (adminSchool == null || !adminSchool.equals(school)) {
                throw new SecurityException("校级管理员只能操作本校数据");
            }
        }
    }

    /**
     * 学校模块列表（仅含 checkuser 中出现过的学校），含预导入与已注册教师数
     */
    @GetMapping("/school-modules")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listSchoolModules(HttpServletRequest request) {
        try {
            User admin = requireSchoolAdminOrAbove(request);
            boolean isSchoolAdmin = admin.getUserType() == User.UserType.school_admin;
            String adminSchool = admin.getSchool();
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT school FROM (" +
                    "SELECT DISTINCT school FROM checkuser1.checkstudent WHERE school IS NOT NULL AND TRIM(school) <> '' " +
                    "UNION " +
                    "SELECT DISTINCT school FROM checkuser1.checkteacher WHERE school IS NOT NULL AND TRIM(school) <> ''" +
                ") u ORDER BY school");
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String school = (String) row.get("school");
                if (school == null || school.isBlank()) continue;
                if (isSchoolAdmin && adminSchool != null && !adminSchool.equals(school)) continue;
                Long studentCnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM checkuser1.checkstudent WHERE school = ?", Long.class, school);
                Long preTeacherCnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM checkuser1.checkteacher WHERE school = ?", Long.class, school);
                long regTeacherCnt = userRepository.countBySchoolAndUserTypeIn(school, REGISTERED_TEACHER_ROLES);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("school", school);
                item.put("studentCount", studentCnt != null ? studentCnt : 0L);
                item.put("teacherPreimportCount", preTeacherCnt != null ? preTeacherCnt : 0L);
                item.put("teacherRegisteredCount", regTeacherCnt);
                result.add(item);
            }
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 单校详情：预导入教师列表 + 已注册教师列表
     */
    @GetMapping("/school-detail")
    public ResponseEntity<ApiResponse<Map<String, Object>>> schoolDetail(
            @RequestParam("school") String school,
            HttpServletRequest request) {
        try {
            User admin = requireSchoolAdminOrAbove(request);
            enforceSchoolScope(admin, school);
            if (school == null || school.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("school 不能为空"));
            }
            Long studentCnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM checkuser1.checkstudent WHERE school = ?", Long.class, school);
            List<Map<String, Object>> preRows = jdbcTemplate.queryForList(
                "SELECT school, college, teacherid, name FROM checkuser1.checkteacher WHERE school = ? " +
                    "ORDER BY college, teacherid", school);
            List<Map<String, Object>> preimportList = new ArrayList<>();
            for (Map<String, Object> r : preRows) {
                String tid = Objects.toString(r.get("teacherid"), "");
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("school", r.get("school"));
                m.put("college", r.get("college"));
                m.put("teacherId", tid);
                m.put("name", r.get("name"));
                m.put("hasAccount", tid.isEmpty() ? false : userRepository.existsByStudentId(tid));
                preimportList.add(m);
            }
            List<User> regUsers = userRepository.findBySchoolAndUserTypeIn(school, REGISTERED_TEACHER_ROLES);
            List<Map<String, Object>> registeredList = new ArrayList<>();
            for (User u : regUsers) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("realName", u.getRealName());
                m.put("studentId", u.getStudentId());
                m.put("userType", u.getUserType().name());
                m.put("departmentName", u.getDepartmentName());
                m.put("school", u.getSchool());
                registeredList.add(m);
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("school", school);
            data.put("studentCount", studentCnt != null ? studentCnt : 0L);
            data.put("preimportTeachers", preimportList);
            data.put("registeredTeachers", registeredList);
            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 预导入表中已有教师：直接开户（设密码、角色：teacher / department_admin / school_admin）
     */
    @PostMapping("/activate-from-preimport")
    public ResponseEntity<ApiResponse<Map<String, Object>>> activateFromPreimport(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            User admin = requireSchoolAdminOrAbove(request);
            String school = body.get("school");
            enforceSchoolScope(admin, school);
            String teacherId = body.get("teacherId");
            String password = body.get("password");
            String userTypeStr = body.get("userType");
            if (isBlank(school) || isBlank(teacherId) || isBlank(password) || isBlank(userTypeStr)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("school、teacherId、password、userType 均为必填"));
            }
            User.UserType role = parseTeacherAdminRole(userTypeStr);
            if (userRepository.existsByStudentId(teacherId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("该工号已注册"));
            }
            List<Map<String, Object>> found = jdbcTemplate.queryForList(
                "SELECT college, name FROM checkuser1.checkteacher WHERE school = ? AND teacherid = ? LIMIT 1",
                school, teacherId);
            if (found.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("预导入表中无此教师记录"));
            }
            String college = (String) found.get(0).get("college");
            String realName = (String) found.get(0).get("name");
            if (isBlank(realName)) realName = teacherId;
            Map<String, Object> created = createUserAccount(realName, teacherId, school, college, password, role);
            return ResponseEntity.ok(ApiResponse.success("开户成功", created));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("开户失败：" + e.getMessage()));
        }
    }

    /**
     * 不在预导入表中的教师：同时写入 users 与 checkuser1.checkteacher
     */
    @PostMapping("/create-teacher-with-preimport")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTeacherWithPreimport(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            User admin = requireSchoolAdminOrAbove(request);
            String school = body.get("school");
            enforceSchoolScope(admin, school);
            String college = body.get("college");
            String teacherId = body.get("teacherId");
            String name = body.get("name");
            String password = body.get("password");
            String userTypeStr = body.get("userType");
            if (isBlank(school) || isBlank(teacherId) || isBlank(name) || isBlank(password) || isBlank(userTypeStr)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("school、teacherId、name、password、userType 均为必填"));
            }
            User.UserType role = parseTeacherAdminRole(userTypeStr);
            if (userRepository.existsByStudentId(teacherId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("该工号已在系统中注册"));
            }
            Integer existsPre = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM checkuser1.checkteacher WHERE teacherid = ?", Integer.class, teacherId);
            if (existsPre != null && existsPre > 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("该工号已在预导入表中，请使用「从预导入开户」"));
            }
            jdbcTemplate.update(
                "INSERT INTO checkuser1.checkteacher (school, college, teacherid, name) VALUES (?,?,?,?)",
                school, blankToNull(college), teacherId, name);
            Map<String, Object> created = createUserAccount(name, teacherId, school, college, password, role);
            created.put("preimportInserted", true);
            return ResponseEntity.ok(ApiResponse.success("开户成功，已同步预导入表", created));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("开户失败：" + e.getMessage()));
        }
    }

    /**
     * 新建学校：创建该校校级管理员，并写入预导入教师表
     */
    @PostMapping("/create-school")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSchool(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            requireSuperAdmin(request);
            String schoolName = body.get("schoolName");
            String college = body.get("college");
            String teacherId = body.get("teacherId");
            String realName = body.get("realName");
            String password = body.get("password");
            if (isBlank(schoolName) || isBlank(teacherId) || isBlank(realName) || isBlank(password)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("schoolName、teacherId、realName、password 均为必填"));
            }
            Integer schoolKnown = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" +
                    "SELECT school FROM checkuser1.checkstudent WHERE school = ? " +
                    "UNION SELECT school FROM checkuser1.checkteacher WHERE school = ?) t",
                Integer.class, schoolName, schoolName);
            if (schoolKnown != null && schoolKnown > 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("该校名在预导入库中已存在，请从「学校开户」进入操作"));
            }
            if (userRepository.existsByStudentId(teacherId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("该工号已注册"));
            }
            Integer tidUsed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM checkuser1.checkteacher WHERE teacherid = ?", Integer.class, teacherId);
            if (tidUsed != null && tidUsed > 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("该工号已在预导入表中使用"));
            }
            jdbcTemplate.update(
                "INSERT INTO checkuser1.checkteacher (school, college, teacherid, name) VALUES (?,?,?,?)",
                schoolName, blankToNull(college), teacherId, realName);
            Map<String, Object> created = createUserAccount(
                realName, teacherId, schoolName, college, password, User.UserType.school_admin);
            created.put("schoolCreated", true);
            return ResponseEntity.ok(ApiResponse.success("学校已创建，校管账户已开通", created));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    private Map<String, Object> createUserAccount(
            String realName, String studentId, String school, String college,
            String password, User.UserType userType) {
        String namePrefix = realName.substring(0, Math.min(3, realName.length())).toLowerCase(Locale.ROOT);
        String idSuffix = studentId.length() > 4 ? studentId.substring(studentId.length() - 4) : studentId;
        String username = namePrefix + idSuffix;
        if (userRepository.existsByUsername(username)) {
            username = username + (new Random().nextInt(90) + 10);
        }
        String prefix = (userType == User.UserType.student) ? "stu_" : "tea_";
        String userId = prefix + UUID.randomUUID().toString().substring(0, 8);
        User user = new User(userId, username, passwordEncoder.encode(password),
            realName, studentId, userType, school);
        if (!isBlank(college)) user.setDepartmentName(college.trim());
        userRepository.save(user);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", userId);
        m.put("username", username);
        m.put("realName", realName);
        m.put("studentId", studentId);
        m.put("userType", userType.name());
        m.put("school", school);
        return m;
    }

    private User.UserType parseTeacherAdminRole(String userTypeStr) {
        try {
            User.UserType t = User.UserType.valueOf(userTypeStr.trim());
            if (t != User.UserType.teacher && t != User.UserType.counselor && t != User.UserType.department_admin && t != User.UserType.school_admin) {
                throw new IllegalArgumentException("userType 仅支持 teacher、counselor、department_admin、school_admin");
            }
            return t;
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("userType")) throw e;
            throw new IllegalArgumentException("无效的用户类型: " + userTypeStr);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
