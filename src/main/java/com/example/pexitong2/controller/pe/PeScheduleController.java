package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.PeSchedule;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.pe.PeScheduleService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/pe/schedules")
public class PeScheduleController {

    @Autowired
    private PeScheduleService peScheduleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/pre-check")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> preCheckSchedules(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            String userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String school = user.getSchool();

            List<String> unknownNames = peScheduleService.preCheckTeachers(file.getInputStream(), school);
            return buildPreCheckResponse(unknownNames, school);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/pre-check-json")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> preCheckSchedulesJson(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            String userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String school = user.getSchool();

            @SuppressWarnings("unchecked")
            List<String> teacherNames = (List<String>) body.get("teacherNames");
            List<String> unknownNames = peScheduleService.preCheckTeacherNames(teacherNames, school);
            return buildPreCheckResponse(unknownNames, school);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    private ResponseEntity<ApiResponse<List<Map<String, Object>>>> buildPreCheckResponse(
            List<String> unknownNames, String school) {
        if (unknownNames.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("所有老师均已存在", List.of()));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String name : unknownNames) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);

            List<Map<String, Object>> preRows = jdbcTemplate.queryForList(
                    "SELECT teacherid, college FROM checkuser.checkteacher WHERE school = ? AND name = ? LIMIT 1",
                    school, name);
            if (!preRows.isEmpty()) {
                item.put("inPreimport", true);
                item.put("teacherId", Objects.toString(preRows.get(0).get("teacherid"), ""));
                item.put("college", Objects.toString(preRows.get(0).get("college"), ""));
            } else {
                item.put("inPreimport", false);
                item.put("teacherId", "");
                item.put("college", "");
            }
            result.add(item);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "发现 " + unknownNames.size() + " 位老师不在系统中", result));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Integer>> importSchedules(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam String semester) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            String userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String school = user.getSchool();

            int count = peScheduleService.importFromExcel(file.getInputStream(), school, semester);
            return ResponseEntity.ok(ApiResponse.success("导入成功，共导入" + count + "条记录", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/import-json")
    public ResponseEntity<ApiResponse<Integer>> importSchedulesJson(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            String userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String school = user.getSchool();
            String semester = (String) body.get("semester");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) body.get("rows");
            int count = peScheduleService.importFromRows(rows, school, semester);
            return ResponseEntity.ok(ApiResponse.success("导入成功，共导入" + count + "条记录", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSchedules(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String semester) {
        try {
            String resolvedSchool = school;
            if (resolvedSchool == null || resolvedSchool.isBlank()) {
                String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
                String userId = jwtUtil.extractUserId(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                resolvedSchool = user.getSchool();
            }
            List<Map<String, Object>> schedules = peScheduleService.getScheduleRows(resolvedSchool, semester);
            return ResponseEntity.ok(ApiResponse.success("获取课表成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            peScheduleService.deleteSchedule(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
