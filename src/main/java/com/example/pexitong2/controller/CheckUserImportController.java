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
 * checkuser 库数据导入接口（仅 super_admin 可用）
 *
 * GET  /checkuser/teacher-schools    → 公开接口，获取教师预导入库中的学校列表（注册页用）
 * GET  /checkuser/schools            → 查询各学校预导入学生/教师数量（需超管）
 * GET  /checkuser/template/student   → 下载学生导入模板
 * GET  /checkuser/template/teacher   → 下载教师导入模板
 * POST /checkuser/import/student     → 上传学生 Excel，去重导入
 * POST /checkuser/import/teacher     → 上传教师 Excel，去重导入
 */
@RestController
@RequestMapping("/checkuser")
@CrossOrigin(origins = "*")
public class CheckUserImportController {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    // ── 权限校验 ──────────────────────────────────────────────────────────────

    private void requireSuperAdmin(HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new SecurityException("用户不存在"));
        if (user.getUserType() != User.UserType.super_admin) {
            throw new SecurityException("权限不足，仅超级管理员可操作");
        }
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
                "SELECT DISTINCT school FROM checkuser.checkteacher WHERE school IS NOT NULL ORDER BY school");
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
            requireSuperAdmin(request);

            // 查询学生各学校数量
            List<Map<String, Object>> studentRows = jdbcTemplate.queryForList(
                "SELECT school, COUNT(*) AS cnt FROM checkuser.checkstudent GROUP BY school ORDER BY school");
            Map<String, Long> studentMap = new LinkedHashMap<>();
            for (Map<String, Object> row : studentRows) {
                String school = (String) row.get("school");
                Long cnt = ((Number) row.get("cnt")).longValue();
                studentMap.put(school != null ? school : "", cnt);
            }

            // 查询教师各学校数量
            List<Map<String, Object>> teacherRows = jdbcTemplate.queryForList(
                "SELECT school, COUNT(*) AS cnt FROM checkuser.checkteacher GROUP BY school ORDER BY school");
            Map<String, Long> teacherMap = new LinkedHashMap<>();
            for (Map<String, Object> row : teacherRows) {
                String school = (String) row.get("school");
                Long cnt = ((Number) row.get("cnt")).longValue();
                teacherMap.put(school != null ? school : "", cnt);
            }

            // 合并学校列表
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

    /** 学生导入模板：school / college / class_name / studentid / name */
    @GetMapping("/template/student")
    public ResponseEntity<byte[]> downloadStudentTemplate(HttpServletRequest request) {
        try {
            requireSuperAdmin(request);
            String[] headers = {"学校", "学院", "班级", "学号", "姓名"};
            String[] example = {"济南校区", "电气学院", "电气工程及其自动化2021-1", "202100000001", "张三"};
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
            requireSuperAdmin(request);
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
     * 导入学生数据
     * Excel 列顺序：学校 / 学院 / 班级 / 学号 / 姓名
     * 去重依据：studentid
     */
    @PostMapping("/import/student")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importStudents(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            requireSuperAdmin(request);
            if (file.isEmpty()) return ResponseEntity.badRequest()
                .body(ApiResponse.error("文件不能为空"));

            List<Map<String, String>> rows = parseExcel(file, 5);
            int inserted = 0, skipped = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> row = rows.get(i);
                String school    = row.get("0");
                String college   = row.get("1");
                String className = row.get("2");
                String studentId = row.get("3");
                String name      = row.get("4");

                if (isBlank(studentId)) {
                    errors.add("第 " + (i + 2) + " 行：学号为空，已跳过");
                    continue;
                }
                if (isBlank(name)) {
                    errors.add("第 " + (i + 2) + " 行：姓名为空，已跳过");
                    continue;
                }

                // 去重：studentid 已存在则跳过
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM checkuser.checkstudent WHERE studentid = ?",
                    Integer.class, studentId);
                if (count != null && count > 0) {
                    skipped++;
                    continue;
                }

                jdbcTemplate.update(
                    "INSERT INTO checkuser.checkstudent (school, college, class_name, studentid, name) VALUES (?,?,?,?,?)",
                    school, college, className, studentId, name);
                inserted++;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total",    rows.size());
            result.put("inserted", inserted);
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
     * 导入教师数据
     * Excel 列顺序：学校 / 学院 / 工号 / 姓名
     * 去重依据：teacherid
     */
    @PostMapping("/import/teacher")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importTeachers(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            requireSuperAdmin(request);
            if (file.isEmpty()) return ResponseEntity.badRequest()
                .body(ApiResponse.error("文件不能为空"));

            List<Map<String, String>> rows = parseExcel(file, 4);
            int inserted = 0, skipped = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> row = rows.get(i);
                String school    = row.get("0");
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
                    "SELECT COUNT(*) FROM checkuser.checkteacher WHERE teacherid = ?",
                    Integer.class, teacherId);
                if (count != null && count > 0) {
                    skipped++;
                    continue;
                }

                jdbcTemplate.update(
                    "INSERT INTO checkuser.checkteacher (school, college, teacherid, name) VALUES (?,?,?,?)",
                    school, college, teacherId, name);
                inserted++;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total",    rows.size());
            result.put("inserted", inserted);
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
