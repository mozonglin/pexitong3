package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * checkuser 库数据导入接口（super_admin 和 school_admin 可用）
 *
 * GET  /checkuser/teacher-schools    → 公开接口，获取教师预导入库中的学校列表（注册页用）
 * GET  /checkuser/schools            → 查询各学校预导入学生/教师数量（需超管或校管）
 * GET  /checkuser/template/student   → 下载学生导入模板
 * GET  /checkuser/template/teacher   → 下载教师导入模板
 * POST /checkuser/import/student     → 上传学生 Excel，两阶段导入（preview / confirm）
 * POST /checkuser/import/teacher     → 上传教师 Excel，两阶段导入（preview / confirm）
 */
@RestController
@RequestMapping("/checkuser")
@CrossOrigin(origins = "*")
public class CheckUserImportController {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    // ── 权限校验 ──────────────────────────────────────────────────────────────

    private User resolveCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtUtil.extractUsername(token);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new SecurityException("用户不存在"));
    }

    private void requireSuperAdmin(HttpServletRequest request) {
        User user = resolveCurrentUser(request);
        if (user.getUserType() != User.UserType.super_admin) {
            throw new SecurityException("权限不足，仅超级管理员可操作");
        }
    }

    private User requireSchoolAdminOrAbove(HttpServletRequest request) {
        User user = resolveCurrentUser(request);
        if (user.getUserType() != User.UserType.super_admin
                && user.getUserType() != User.UserType.school_admin) {
            throw new SecurityException("权限不足，仅超级管理员或校级管理员可操作");
        }
        return user;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        throw new SecurityException("未提供认证 Token");
    }

    // ── 公开接口：注册页获取教师学校列表 ──────────────────────────────────────

    /**
     * 公开接口（无需登录），返回 checkteacher 中所有有记录的学校名称列表。
     * 供注册页面的学校下拉框使用。
     */
    @GetMapping("/teacher-schools")
    public ResponseEntity<ApiResponse<List<String>>> getTeacherSchools() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DISTINCT school FROM checkuser1.checkteacher WHERE school IS NOT NULL ORDER BY school");
            List<String> schools = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String school = (String) row.get("school");
                if (school != null && !school.isBlank()) {
                    schools.add(school);
                }
            }
            return ResponseEntity.ok(ApiResponse.success("获取成功", schools));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    // ── 学校统计 ──────────────────────────────────────────────────────────────

    /**
     * 查询 checkuser 库中各学校的预导入学生/教师数量
     * 返回结构：[ { school, studentCount, teacherCount } ]
     */
    @GetMapping("/schools")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSchoolStats(HttpServletRequest request) {
        try {
            User admin = requireSchoolAdminOrAbove(request);
            boolean isSchoolAdmin = admin.getUserType() == User.UserType.school_admin;
            String adminSchool = admin.getSchool();

            List<Map<String, Object>> studentRows;
            List<Map<String, Object>> teacherRows;

            if (isSchoolAdmin && adminSchool != null && !adminSchool.isBlank()) {
                studentRows = jdbcTemplate.queryForList(
                    "SELECT school, COUNT(*) AS cnt FROM checkuser1.checkstudent WHERE school = ? GROUP BY school", adminSchool);
                teacherRows = jdbcTemplate.queryForList(
                    "SELECT school, COUNT(*) AS cnt FROM checkuser1.checkteacher WHERE school = ? GROUP BY school", adminSchool);
            } else {
                studentRows = jdbcTemplate.queryForList(
                    "SELECT school, COUNT(*) AS cnt FROM checkuser1.checkstudent GROUP BY school ORDER BY school");
                teacherRows = jdbcTemplate.queryForList(
                    "SELECT school, COUNT(*) AS cnt FROM checkuser1.checkteacher GROUP BY school ORDER BY school");
            }

            Map<String, Long> studentMap = new LinkedHashMap<>();
            for (Map<String, Object> row : studentRows) {
                String school = (String) row.get("school");
                Long cnt = ((Number) row.get("cnt")).longValue();
                studentMap.put(school != null ? school : "", cnt);
            }
            Map<String, Long> teacherMap = new LinkedHashMap<>();
            for (Map<String, Object> row : teacherRows) {
                String school = (String) row.get("school");
                Long cnt = ((Number) row.get("cnt")).longValue();
                teacherMap.put(school != null ? school : "", cnt);
            }

            Set<String> allSchools = new LinkedHashSet<>();
            allSchools.addAll(studentMap.keySet());
            allSchools.addAll(teacherMap.keySet());

            List<Map<String, Object>> result = new ArrayList<>();
            for (String school : allSchools) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("school", school);
                item.put("studentCount", studentMap.getOrDefault(school, 0L));
                item.put("teacherCount", teacherMap.getOrDefault(school, 0L));
                result.add(item);
            }

            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    // ── 下载模板 ──────────────────────────────────────────────────────────────

    /** 学生导入模板：school / college / class_name / studentid / name / gender */
    @GetMapping("/template/student")
    public ResponseEntity<byte[]> downloadStudentTemplate(HttpServletRequest request) {
        try {
            requireSchoolAdminOrAbove(request);
            String[] headers = {"学校", "学院", "班级", "学号", "姓名", "性别"};
            String[] example = {"济南校区", "电气学院", "电气工程及其自动化2021-1", "202100000001", "张三", "男"};
            byte[] bytes = buildTemplate("学生导入模板", headers, example);
            return buildDownloadResponse(bytes, "学生导入模板.xlsx");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /** 教师导入模板：school / college / teacherid / name */
    @GetMapping("/template/teacher")
    public ResponseEntity<byte[]> downloadTeacherTemplate(HttpServletRequest request) {
        try {
            requireSchoolAdminOrAbove(request);
            String[] headers = {"学校", "学院", "工号", "姓名"};
            String[] example = {"济南校区", "体育学院", "20210001", "李四"};
            byte[] bytes = buildTemplate("教师导入模板", headers, example);
            return buildDownloadResponse(bytes, "教师导入模板.xlsx");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── 导入数据 ──────────────────────────────────────────────────────────────

    /**
     * 导入学生数据 - 两阶段模式
     * mode=preview: 预检，返回新记录与重复记录列表
     * mode=confirm: 确认导入，带上重复记录的处理决策
     * 无 mode 参数时保持兼容：自动跳过重复
     */
    @PostMapping("/import/student")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "overrideIds", required = false) String overrideIds,
            HttpServletRequest request) {
        try {
            User admin = requireSchoolAdminOrAbove(request);
            if (file.isEmpty()) return ResponseEntity.badRequest()
                .body(ApiResponse.error("文件不能为空"));

            boolean isSchoolAdmin = admin.getUserType() == User.UserType.school_admin;
            String adminSchool = admin.getSchool();

            List<Map<String, String>> rows = parseExcel(file, 6);
            List<String> errors = new ArrayList<>();

            if ("preview".equals(mode)) {
                List<Map<String, Object>> newRecords = new ArrayList<>();
                List<Map<String, Object>> duplicates = new ArrayList<>();

                for (int i = 0; i < rows.size(); i++) {
                    Map<String, String> row = rows.get(i);
                    String school    = isSchoolAdmin ? adminSchool : row.get("0");
                    String college   = row.get("1");
                    String className = row.get("2");
                    String studentId = row.get("3");
                    String name      = row.get("4");
                    String gender    = row.get("5");

                    if (isBlank(studentId) || isBlank(name)) {
                        errors.add("第 " + (i + 2) + " 行：学号或姓名为空，已跳过");
                        continue;
                    }

                    List<Map<String, Object>> existing = jdbcTemplate.queryForList(
                        "SELECT school, college, class_name, studentid, name, gender FROM checkuser1.checkstudent WHERE studentid = ?",
                        studentId);

                    Map<String, Object> record = new LinkedHashMap<>();
                    record.put("rowIndex", i);
                    record.put("school", school);
                    record.put("college", college);
                    record.put("className", className);
                    record.put("studentId", studentId);
                    record.put("name", name);
                    record.put("gender", gender);

                    if (!existing.isEmpty()) {
                        Map<String, Object> existingRow = existing.get(0);
                        record.put("existingName", existingRow.get("name"));
                        record.put("existingSchool", existingRow.get("school"));
                        record.put("existingCollege", existingRow.get("college"));
                        record.put("existingClassName", existingRow.get("class_name"));
                        record.put("existingGender", existingRow.get("gender"));
                        duplicates.add(record);
                    } else {
                        newRecords.add(record);
                    }
                }

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("total", rows.size());
                result.put("newCount", newRecords.size());
                result.put("duplicateCount", duplicates.size());
                result.put("newRecords", newRecords);
                result.put("duplicates", duplicates);
                result.put("errors", errors);
                return ResponseEntity.ok(ApiResponse.success("预检完成", result));
            }

            // confirm 模式或无 mode（兼容旧逻辑）
            Set<String> overrideSet = new HashSet<>();
            if ("confirm".equals(mode) && overrideIds != null && !overrideIds.isBlank()) {
                overrideSet.addAll(Arrays.asList(overrideIds.split(",")));
            }

            int inserted = 0, skipped = 0, updated = 0;
            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> row = rows.get(i);
                String school    = isSchoolAdmin ? adminSchool : row.get("0");
                String college   = row.get("1");
                String className = row.get("2");
                String studentId = row.get("3");
                String name      = row.get("4");
                String gender    = row.get("5");

                if (isBlank(studentId)) {
                    errors.add("第 " + (i + 2) + " 行：学号为空，已跳过");
                    continue;
                }
                if (isBlank(name)) {
                    errors.add("第 " + (i + 2) + " 行：姓名为空，已跳过");
                    continue;
                }

                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM checkuser1.checkstudent WHERE studentid = ?",
                    Integer.class, studentId);
                if (count != null && count > 0) {
                    if (overrideSet.contains(studentId)) {
                        jdbcTemplate.update(
                            "UPDATE checkuser1.checkstudent SET school=?, college=?, class_name=?, name=?, gender=? WHERE studentid=?",
                            school, college, className, name, gender, studentId);
                        updated++;
                    } else {
                        skipped++;
                    }
                    continue;
                }

                jdbcTemplate.update(
                    "INSERT INTO checkuser1.checkstudent (school, college, class_name, studentid, name, gender) VALUES (?,?,?,?,?,?)",
                    school, college, className, studentId, name, gender);
                inserted++;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total",    rows.size());
            result.put("inserted", inserted);
            result.put("updated",  updated);
            result.put("skipped",  skipped);
            result.put("errors",   errors);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));

        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("导入失败：" + e.getMessage()));
        }
    }

    /**
     * 导入教师数据 - 两阶段模式
     * mode=preview: 预检，返回新记录与重复记录列表
     * mode=confirm: 确认导入，带上重复记录的处理决策
     */
    @PostMapping("/import/teacher")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importTeachers(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "overrideIds", required = false) String overrideIds,
            HttpServletRequest request) {
        try {
            User admin = requireSchoolAdminOrAbove(request);
            if (file.isEmpty()) return ResponseEntity.badRequest()
                .body(ApiResponse.error("文件不能为空"));

            boolean isSchoolAdmin = admin.getUserType() == User.UserType.school_admin;
            String adminSchool = admin.getSchool();

            List<Map<String, String>> rows = parseExcel(file, 4);
            List<String> errors = new ArrayList<>();

            if ("preview".equals(mode)) {
                List<Map<String, Object>> newRecords = new ArrayList<>();
                List<Map<String, Object>> duplicates = new ArrayList<>();

                for (int i = 0; i < rows.size(); i++) {
                    Map<String, String> row = rows.get(i);
                    String school    = isSchoolAdmin ? adminSchool : row.get("0");
                    String college   = row.get("1");
                    String teacherId = row.get("2");
                    String name      = row.get("3");

                    if (isBlank(teacherId) || isBlank(name)) {
                        errors.add("第 " + (i + 2) + " 行：工号或姓名为空，已跳过");
                        continue;
                    }

                    List<Map<String, Object>> existing = jdbcTemplate.queryForList(
                        "SELECT school, college, teacherid, name FROM checkuser1.checkteacher WHERE teacherid = ?",
                        teacherId);

                    Map<String, Object> record = new LinkedHashMap<>();
                    record.put("rowIndex", i);
                    record.put("school", school);
                    record.put("college", college);
                    record.put("teacherId", teacherId);
                    record.put("name", name);

                    if (!existing.isEmpty()) {
                        Map<String, Object> existingRow = existing.get(0);
                        record.put("existingName", existingRow.get("name"));
                        record.put("existingSchool", existingRow.get("school"));
                        record.put("existingCollege", existingRow.get("college"));
                        duplicates.add(record);
                    } else {
                        newRecords.add(record);
                    }
                }

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("total", rows.size());
                result.put("newCount", newRecords.size());
                result.put("duplicateCount", duplicates.size());
                result.put("newRecords", newRecords);
                result.put("duplicates", duplicates);
                result.put("errors", errors);
                return ResponseEntity.ok(ApiResponse.success("预检完成", result));
            }

            Set<String> overrideSet = new HashSet<>();
            if ("confirm".equals(mode) && overrideIds != null && !overrideIds.isBlank()) {
                overrideSet.addAll(Arrays.asList(overrideIds.split(",")));
            }

            int inserted = 0, skipped = 0, updated = 0;
            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> row = rows.get(i);
                String school    = isSchoolAdmin ? adminSchool : row.get("0");
                String college   = row.get("1");
                String teacherId = row.get("2");
                String name      = row.get("3");

                if (isBlank(teacherId)) {
                    errors.add("第 " + (i + 2) + " 行：工号为空，已跳过");
                    continue;
                }
                if (isBlank(name)) {
                    errors.add("第 " + (i + 2) + " 行：姓名为空，已跳过");
                    continue;
                }

                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM checkuser1.checkteacher WHERE teacherid = ?",
                    Integer.class, teacherId);
                if (count != null && count > 0) {
                    if (overrideSet.contains(teacherId)) {
                        jdbcTemplate.update(
                            "UPDATE checkuser1.checkteacher SET school=?, college=?, name=? WHERE teacherid=?",
                            school, college, name, teacherId);
                        updated++;
                    } else {
                        skipped++;
                    }
                    continue;
                }

                jdbcTemplate.update(
                    "INSERT INTO checkuser1.checkteacher (school, college, teacherid, name) VALUES (?,?,?,?)",
                    school, college, teacherId, name);
                inserted++;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total",    rows.size());
            result.put("inserted", inserted);
            result.put("updated",  updated);
            result.put("skipped",  skipped);
            result.put("errors",   errors);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));

        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("导入失败：" + e.getMessage()));
        }
    }

    // ── 工具方法 ──────────────────────────────────────────────────────────────

    /** 解析 Excel，跳过第一行表头，返回每行按列索引的字符串 Map */
    private List<Map<String, String>> parseExcel(MultipartFile file, int colCount) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> map = new LinkedHashMap<>();
                boolean allEmpty = true;
                for (int c = 0; c < colCount; c++) {
                    String val = getCellString(row.getCell(c));
                    map.put(String.valueOf(c), val);
                    if (!val.isEmpty()) allEmpty = false;
                }
                if (!allEmpty) result.add(map);
            }
        }
        return result;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    /** 生成带表头和示例行的 xlsx 模板 */
    private byte[] buildTemplate(String sheetName, String[] headers, String[] example) throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(sheetName);

            // 表头样式
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // 表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // 示例行（灰色）
            CellStyle exampleStyle = wb.createCellStyle();
            Font exampleFont = wb.createFont();
            exampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            exampleStyle.setFont(exampleFont);

            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < example.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(example[i]);
                cell.setCellStyle(exampleStyle);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private ResponseEntity<byte[]> buildDownloadResponse(byte[] bytes, String filename) {
        String encoded = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
            .replace("+", "%20");
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
