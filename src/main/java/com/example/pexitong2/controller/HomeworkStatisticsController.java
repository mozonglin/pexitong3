package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.dto.HomeworkWeeklySubmissionCompletionResponse;
import com.example.pexitong2.dto.HomeworkWeeklySubmissionGroupsResponse;
import com.example.pexitong2.service.HomeworkStatsCacheService;
import com.example.pexitong2.service.HomeworkWeeklySubmissionCompletionService;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 课后作业统计接口
 *
 * 权限隔离规则：
 *   - super_admin      → 查全库（不加学校过滤）
 *   - school_admin     → 只查本校（users1.school = admin.school）
 *   - department_admin → 只查本院（users1.school = admin.school AND users1.college = admin.department_name）
 *
 * 管理员信息来源：users 表（user_type / school / department_name）
 * 学生数据来源：users1 表（school / college / class_name / total_* 字段）
 * 趋势数据来源：homework_scores JOIN users1（需要时间维度）
 */
@RestController
@RequestMapping("/statistics/homework")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HomeworkStatisticsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HomeworkStatsCacheService cacheService;

    @Autowired
    private HomeworkWeeklySubmissionCompletionService homeworkWeeklySubmissionCompletionService;

    // ── 内部辅助：从 Token 解析管理员信息 ────────────────────────────────────────

    /**
     * 解析 Token，返回管理员上下文；若无权限则抛出异常。
     * 管理员账号在 users 表，字段：id / user_type / school / department_name
     */
    private AdminContext resolveAdmin(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("未提供有效的授权令牌");
        }
        String token = header.substring(7);
        try {
            String userType = jwtUtil.extractUserType(token);
            if (userType == null) throw new SecurityException("无效的令牌");

            boolean allowed = userType.equals("department_admin")
                    || userType.equals("school_admin")
                    || userType.equals("super_admin")
                    || userType.equals("counselor");
            if (!allowed) throw new SecurityException("权限不足");

            if (userType.equals("super_admin")) {
                return new AdminContext(userType, null, null, null);
            }

            String userId = jwtUtil.extractUserId(token);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT school, department_name FROM users WHERE id = ? LIMIT 1", userId);
            if (rows.isEmpty()) throw new SecurityException("管理员账号不存在");

            String school     = (String) rows.get(0).get("school");
            String department = (String) rows.get(0).get("department_name");

            if (school == null || school.isBlank()) {
                throw new SecurityException("管理员账号未绑定学校信息");
            }

            List<String> counselorClasses = null;
            if ("counselor".equals(userType)) {
                if (department == null || department.isBlank()) {
                    List<Map<String, Object>> deptRows = jdbcTemplate.queryForList(
                        "SELECT DISTINCT department_name FROM counselor_class_assignments WHERE counselor_id = ? AND department_name IS NOT NULL LIMIT 1", userId);
                    if (!deptRows.isEmpty()) department = (String) deptRows.get(0).get("department_name");
                }
                List<Map<String, Object>> classRows = jdbcTemplate.queryForList(
                    "SELECT class_name FROM counselor_class_assignments WHERE counselor_id = ?", userId);
                counselorClasses = new ArrayList<>();
                for (Map<String, Object> r : classRows) {
                    counselorClasses.add((String) r.get("class_name"));
                }
            }
            return new AdminContext(userType, school, department, counselorClasses);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("令牌解析失败: " + e.getMessage());
        }
    }

    private static class AdminContext {
        final String userType;
        final String school;
        final String department;
        final List<String> counselorClasses;

        AdminContext(String userType, String school, String department, List<String> counselorClasses) {
            this.userType         = userType;
            this.school           = school;
            this.department       = department;
            this.counselorClasses = counselorClasses;
        }

        boolean isDeptAdmin()   { return "department_admin".equals(userType); }
        boolean isCounselor()   { return "counselor".equals(userType); }
        boolean isSuperAdmin()  { return "super_admin".equals(userType); }
    }

    /**
     * 构建针对 users1 表的 WHERE 子句（别名固定为 u），用于学校/学院隔离。
     * 返回 Object[]{ whereClause(String), params(Object[]) }
     */
    private Object[] buildUserScope(AdminContext ctx) {
        if (ctx.isSuperAdmin()) {
            return new Object[]{"1=1", new Object[0]};
        }
        if (ctx.isCounselor() && ctx.counselorClasses != null && !ctx.counselorClasses.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(ctx.counselorClasses.size(), "?"));
            List<Object> params = new ArrayList<>();
            params.add(ctx.school);
            params.addAll(ctx.counselorClasses);
            return new Object[]{"u.school = ? AND u.class_name IN (" + placeholders + ")",
                    params.toArray()};
        }
        if ((ctx.isDeptAdmin() || ctx.isCounselor()) && ctx.department != null && !ctx.department.isBlank()) {
            return new Object[]{"u.school = ? AND u.college = ?",
                    new Object[]{ctx.school, ctx.department}};
        }
        return new Object[]{"u.school = ?", new Object[]{ctx.school}};
    }

    // ── 1. 总览 ─────────────────────────────────────────────────────────────────
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(
            HttpServletRequest request,
            @RequestParam(required = false) String period) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
        try {
            Object[] scope = buildUserScope(ctx);
            String where    = (String)   scope[0];
            Object[] params = (Object[]) scope[1];
            String joinWhere = where.replace("u.", "u1.");

            // ── 有日期过滤时：所有数据从 homework_scores 聚合 ──
            if (hasPeriodFilter(period)) {
                String df = getDateCondition("h.timestamp", period);

                // 一次查询同时获取汇总数据和分类型数据（WITH rollup 写法兼容性差，改用 UNION ALL）
                String sumSql =
                    "SELECT COUNT(*) AS totalRecords, " +
                    "COUNT(DISTINCT h.student_id) AS totalStudents, " +
                    "COALESCE(SUM(h.`count`), 0) AS totalReps " +
                    "FROM homework_scores h " +
                    "JOIN users1 u1 ON u1.student_id = h.student_id " +
                    "WHERE " + df + " AND " + joinWhere;
                Map<String, Object> sums = jdbcTemplate.queryForMap(sumSql, params);

                String typeSql =
                    "SELECT h.exercise_type AS type, " +
                    "COUNT(DISTINCT h.student_id) AS students, " +
                    "COALESCE(SUM(h.`count`), 0) AS totalReps " +
                    "FROM homework_scores h " +
                    "JOIN users1 u1 ON u1.student_id = h.student_id " +
                    "WHERE " + df + " AND " + joinWhere +
                    " GROUP BY h.exercise_type ORDER BY totalReps DESC";
                List<Map<String, Object>> typeStats = jdbcTemplate.queryForList(typeSql, params);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("totalStudents", sums.get("totalStudents"));
                data.put("totalReps",     sums.get("totalReps"));
                data.put("totalRecords",  sums.get("totalRecords"));
                data.put("todayRecords",  sums.get("totalRecords"));
                data.put("todayStudents", sums.get("totalStudents"));
                data.put("weekRecords",   sums.get("totalRecords"));
                data.put("typeStats",     typeStats);
                return ResponseEntity.ok(ApiResponse.success("获取成功", data));
            }

            // ── 无日期过滤：合并为 3 次查询（原来是 10+ 次） ──

            // 查询1：从 users1 累计字段一次性汇总所有运动类型
            String sumSql =
                "SELECT COUNT(*) AS totalStudents, " +
                "SUM(u.total_squat)        AS totalSquat, " +
                "SUM(u.total_sit_up)       AS totalSitUp, " +
                "SUM(u.total_push_up)      AS totalPushUp, " +
                "SUM(u.total_pull_up)      AS totalPullUp, " +
                "SUM(u.total_jump_rope)    AS totalJumpRope, " +
                "SUM(u.total_jumping_jack) AS totalJumpingJack, " +
                "SUM(u.total_high_knees)   AS totalHighKnees, " +
                "COUNT(CASE WHEN u.total_squat > 0 THEN 1 END)        AS studentsSquat, " +
                "COUNT(CASE WHEN u.total_sit_up > 0 THEN 1 END)       AS studentsSitUp, " +
                "COUNT(CASE WHEN u.total_push_up > 0 THEN 1 END)      AS studentsPushUp, " +
                "COUNT(CASE WHEN u.total_pull_up > 0 THEN 1 END)      AS studentsPullUp, " +
                "COUNT(CASE WHEN u.total_jump_rope > 0 THEN 1 END)    AS studentsJumpRope, " +
                "COUNT(CASE WHEN u.total_jumping_jack > 0 THEN 1 END) AS studentsJumpingJack, " +
                "COUNT(CASE WHEN u.total_high_knees > 0 THEN 1 END)   AS studentsHighKnees " +
                "FROM users1 u WHERE " + where;
            Map<String, Object> sums = jdbcTemplate.queryForMap(sumSql, params);

            // 查询2：今日记录数和活跃学生数
            String todaySql =
                "SELECT COUNT(*) AS todayRecords, COUNT(DISTINCT h.student_id) AS todayStudents " +
                "FROM homework_scores h " +
                "JOIN users1 u1 ON u1.student_id = h.student_id " +
                "WHERE h.timestamp >= CURDATE() AND h.timestamp < CURDATE() + INTERVAL 1 DAY AND " + joinWhere;
            Map<String, Object> today = jdbcTemplate.queryForMap(todaySql, params);

            // 查询3：本周记录数
            String weekSql =
                "SELECT COUNT(*) AS weekRecords " +
                "FROM homework_scores h " +
                "JOIN users1 u1 ON u1.student_id = h.student_id " +
                "WHERE h.timestamp >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND " + joinWhere;
            Map<String, Object> week = jdbcTemplate.queryForMap(weekSql, params);

            long totalSquat       = toLong(sums.get("totalSquat"));
            long totalSitUp       = toLong(sums.get("totalSitUp"));
            long totalPushUp      = toLong(sums.get("totalPushUp"));
            long totalPullUp      = toLong(sums.get("totalPullUp"));
            long totalJumpRope    = toLong(sums.get("totalJumpRope"));
            long totalJumpingJack = toLong(sums.get("totalJumpingJack"));
            long totalHighKnees   = toLong(sums.get("totalHighKnees"));
            long totalReps        = totalSquat + totalSitUp + totalPushUp + totalPullUp
                                  + totalJumpRope + totalJumpingJack + totalHighKnees;

            // 从已有汇总中直接构造 typeStats，无需额外查询
            List<Map<String, Object>> typeStats = new ArrayList<>();
            Object[][] types = {
                {"SQUAT",        totalSquat,       sums.get("studentsSquat")},
                {"SIT_UP",       totalSitUp,       sums.get("studentsSitUp")},
                {"PUSH_UP",      totalPushUp,      sums.get("studentsPushUp")},
                {"PULL_UP",      totalPullUp,      sums.get("studentsPullUp")},
                {"JUMP_ROPE",    totalJumpRope,    sums.get("studentsJumpRope")},
                {"JUMPING_JACK", totalJumpingJack, sums.get("studentsJumpingJack")},
                {"HIGH_KNEES",   totalHighKnees,   sums.get("studentsHighKnees")},
            };
            for (Object[] t : types) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type",      t[0]);
                item.put("students",  t[2]);
                item.put("totalReps", t[1]);
                typeStats.add(item);
            }
            typeStats.sort((a, b) -> Long.compare(toLong(b.get("totalReps")), toLong(a.get("totalReps"))));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalStudents", sums.get("totalStudents"));
            data.put("totalReps",     totalReps);
            data.put("totalRecords",  today.get("todayRecords"));
            data.put("todayRecords",  today.get("todayRecords"));
            data.put("todayStudents", today.get("todayStudents"));
            data.put("weekRecords",   week.get("weekRecords"));
            data.put("typeStats",     typeStats);

            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 2. 趋势（按日期分组，缓存5分钟） ─────────────────────────────────────────
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTrend(
            HttpServletRequest request,
            @RequestParam(required = false) String period) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
        try {
            Object[] scope = buildUserScope(ctx);
            String where    = (String)   scope[0];
            Object[] params = (Object[]) scope[1];
            String joinWhere = where.replace("u.", "u1.");
            String scopeKey = buildScopeKey(ctx);

            List<Map<String, Object>> rows = cacheService.getTrend(scopeKey, joinWhere, params, period);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 3. 班级排名（缓存5分钟） ─────────────────────────────────────────────────
    @GetMapping("/class-rank")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getClassRank(
            HttpServletRequest request,
            @RequestParam(required = false) String period) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
        try {
            Object[] scope = buildUserScope(ctx);
            String where    = (String)   scope[0];
            Object[] params = (Object[]) scope[1];
            String scopeKey = buildScopeKey(ctx);

            List<Map<String, Object>> rows = cacheService.getClassRank(scopeKey, where, params, period);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 4. 院系排名（仅校级/超级管理员，缓存5分钟） ──────────────────────────────
    @GetMapping("/department-rank")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDepartmentRank(
            HttpServletRequest request,
            @RequestParam(required = false) String period) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
        if (ctx.isDeptAdmin() || ctx.isCounselor()) {
            return ResponseEntity.status(403).body(ApiResponse.error("权限不足，仅校级管理员可查看院系排名"));
        }
        try {
            Object[] scope = buildUserScope(ctx);
            String where    = (String)   scope[0];
            Object[] params = (Object[]) scope[1];
            String scopeKey = buildScopeKey(ctx);

            List<Map<String, Object>> rows = cacheService.getDepartmentRank(scopeKey, where, params, period);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 5. 院内班级排名（院级管理员，缓存5分钟） ─────────────────────────────────
    @GetMapping("/dept-class-rank")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDeptClassRank(
            HttpServletRequest request,
            @RequestParam(required = false) String period) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
        try {
            Object[] scope = buildUserScope(ctx);
            String where    = (String)   scope[0];
            Object[] params = (Object[]) scope[1];
            String scopeKey = buildScopeKey(ctx);

            List<Map<String, Object>> rows = cacheService.getClassRank(scopeKey, where, params, period);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 6. 导出 Excel ──────────────────────────────────────────────────────────────
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            HttpServletRequest request,
            @RequestParam(required = false) String period) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }
        try {
            String effectivePeriod = "month";
            if (hasPeriodFilter(period)) {
                if ("today".equals(period) || "week".equals(period) || "month".equals(period) || "four_months".equals(period)) {
                    effectivePeriod = period;
                }
            }

            Object[] scope = buildUserScope(ctx);
            String where = (String) scope[0];
            Object[] params = (Object[]) scope[1];
            String joinWhere = where.replace("u.", "u1.");
            String df = getDateCondition("h.timestamp", effectivePeriod);

            String sql = "SELECT u1.name, u1.student_id, u1.school, u1.college, u1.class_name, " +
                    "COUNT(*) AS record_count, COALESCE(SUM(h.`count`), 0) AS total_reps " +
                    "FROM homework_scores h " +
                    "JOIN users1 u1 ON u1.student_id = h.student_id " +
                    "WHERE " + df + " AND " + joinWhere +
                    " GROUP BY u1.student_id, u1.name, u1.school, u1.college, u1.class_name " +
                    "ORDER BY total_reps DESC";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

            byte[] bytes = buildExportExcel("课后作业统计",
                    new String[]{"姓名", "学号", "学校", "学院", "班级", "作业次数", "总次数"},
                    rows, new String[]{"name", "student_id", "school", "college", "class_name", "record_count", "total_reps"});

            String filename = java.net.URLEncoder.encode("课后作业统计.xlsx", java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 本周课后作业「提交次数」整体达标率（homework_scores 行数，全项目合计）；独立统计，不影响其它接口。
     */
    @GetMapping("/weekly-submission-completion")
    public ResponseEntity<ApiResponse<HomeworkWeeklySubmissionCompletionResponse>> getWeeklySubmissionCompletion(
            HttpServletRequest request) {
        try {
            HomeworkWeeklySubmissionCompletionResponse data = homeworkWeeklySubmissionCompletionService.buildOverall(request);
            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 各院系/班级本周提交达标率；view=school 按 college，view=college 按 class_name+college。
     */
    @GetMapping("/weekly-submission-completion-groups")
    public ResponseEntity<ApiResponse<HomeworkWeeklySubmissionGroupsResponse>> getWeeklySubmissionCompletionGroups(
            HttpServletRequest request,
            @RequestParam String view) {
        try {
            HomeworkWeeklySubmissionGroupsResponse data = homeworkWeeklySubmissionCompletionService.buildGroups(request, view);
            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    private byte[] buildExportExcel(String sheetName, String[] headers, List<Map<String, Object>> rows, String[] keys) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(sheetName);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                Map<String, Object> data = rows.get(r);
                for (int c = 0; c < keys.length; c++) {
                    Object val = data.get(keys[c]);
                    Cell cell = row.createCell(c);
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── 工具方法 ──────────────────────────────────────────────────────────────────

    /** 构造缓存 key（区分不同管理员的数据范围） */
    private String buildScopeKey(AdminContext ctx) {
        if (ctx.isSuperAdmin()) return "super";
        if (ctx.isDeptAdmin())  return ctx.school + ":" + ctx.department;
        return ctx.school;
    }

    private boolean hasPeriodFilter(String period) {
        return period != null && !period.isBlank() && !"all".equalsIgnoreCase(period);
    }

    private String getDateCondition(String column, String period) {
        if (period == null || period.isBlank()) return "1=1";
        switch (period.toLowerCase()) {
            case "today":       return column + " >= CURDATE() AND " + column + " < CURDATE() + INTERVAL 1 DAY";
            case "week":        return column + " >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            case "month":       return column + " >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
            case "four_months": return column + " >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH)";
            default:            return "1=1";
        }
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }
}
