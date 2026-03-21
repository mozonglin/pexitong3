package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

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
                    || userType.equals("super_admin");
            if (!allowed) throw new SecurityException("权限不足");

            if (userType.equals("super_admin")) {
                return new AdminContext(userType, null, null);
            }

            // 从 users 表查管理员的学校和院系
            String userId = jwtUtil.extractUserId(token);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT school, department_name FROM users WHERE id = ? LIMIT 1", userId);
            if (rows.isEmpty()) throw new SecurityException("管理员账号不存在");

            String school     = (String) rows.get(0).get("school");
            String department = (String) rows.get(0).get("department_name");

            if (school == null || school.isBlank()) {
                throw new SecurityException("管理员账号未绑定学校信息");
            }
            return new AdminContext(userType, school, department);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("令牌解析失败: " + e.getMessage());
        }
    }

    /** 管理员上下文 */
    private static class AdminContext {
        final String userType;
        final String school;     // null → super_admin，不限学校
        final String department; // department_admin 的院系名（对应 users1.college）

        AdminContext(String userType, String school, String department) {
            this.userType   = userType;
            this.school     = school;
            this.department = department;
        }

        boolean isDeptAdmin()   { return "department_admin".equals(userType); }
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
        // department_admin：限制到本校本院（users1.college 对应 users.department_name）
        if (ctx.isDeptAdmin() && ctx.department != null && !ctx.department.isBlank()) {
            return new Object[]{"u.school = ? AND u.college = ?",
                    new Object[]{ctx.school, ctx.department}};
        }
        // school_admin：只限本校
        return new Object[]{"u.school = ?", new Object[]{ctx.school}};
    }

    // ── 1. 总览（直接读 users1 的 total_* 字段） ─────────────────────────────────
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(HttpServletRequest request) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
        try {
            Object[] scope = buildUserScope(ctx);
            String where  = (String)   scope[0];
            Object[] params = (Object[]) scope[1];

            // ① 汇总：有作业记录的学生数 + 各运动类型总次数
            String sumSql =
                "SELECT COUNT(*) AS totalStudents, " +
                "SUM(u.total_squat)     AS totalSquat, " +
                "SUM(u.total_sit_up)    AS totalSitUp, " +
                "SUM(u.total_push_up)   AS totalPushUp, " +
                "SUM(u.total_pull_up)   AS totalPullUp, " +
                "SUM(u.total_jump_rope) AS totalJumpRope " +
                "FROM users1 u WHERE " + where +
                " AND (u.total_squat + u.total_sit_up + u.total_push_up" +
                "      + u.total_pull_up + u.total_jump_rope) > 0";
            Map<String, Object> sums = jdbcTemplate.queryForMap(sumSql, params);

            long totalSquat    = toLong(sums.get("totalSquat"));
            long totalSitUp    = toLong(sums.get("totalSitUp"));
            long totalPushUp   = toLong(sums.get("totalPushUp"));
            long totalPullUp   = toLong(sums.get("totalPullUp"));
            long totalJumpRope = toLong(sums.get("totalJumpRope"));
            long totalReps     = totalSquat + totalSitUp + totalPushUp + totalPullUp + totalJumpRope;

            // ② 今日 / 本周提交次数（需要时间维度，join homework_scores）
            //    将 where 中的别名 u. 替换为 u1. 以匹配 join 后的别名
            String joinWhere = where.replace("u.", "u1.");

            String todaySql =
                "SELECT COUNT(*) AS todayRecords, COUNT(DISTINCT h.student_id) AS todayStudents " +
                "FROM homework_scores h " +
                "JOIN users1 u1 ON u1.student_id = h.student_id " +
                "WHERE DATE(h.timestamp) = CURDATE() AND " + joinWhere;
            Map<String, Object> today = jdbcTemplate.queryForMap(todaySql, params);

            String weekSql =
                "SELECT COUNT(*) AS weekRecords " +
                "FROM homework_scores h " +
                "JOIN users1 u1 ON u1.student_id = h.student_id " +
                "WHERE YEARWEEK(h.timestamp, 1) = YEARWEEK(CURDATE(), 1) AND " + joinWhere;
            Map<String, Object> week = jdbcTemplate.queryForMap(weekSql, params);

            // ③ 各运动类型分布（从 users1 聚合，5 次小查询）
            List<Map<String, Object>> typeStats = new ArrayList<>();
            String[][] types = {
                {"SQUAT",     "total_squat"},
                {"SIT_UP",    "total_sit_up"},
                {"PUSH_UP",   "total_push_up"},
                {"PULL_UP",   "total_pull_up"},
                {"JUMP_ROPE", "total_jump_rope"},
            };
            for (String[] t : types) {
                String col = t[1];
                String typeSql =
                    "SELECT COUNT(*) AS students, COALESCE(SUM(u." + col + "), 0) AS totalReps " +
                    "FROM users1 u WHERE " + where + " AND u." + col + " > 0";
                Map<String, Object> row = jdbcTemplate.queryForMap(typeSql, params);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type",      t[0]);
                item.put("students",  row.get("students"));
                item.put("totalReps", row.get("totalReps"));
                typeStats.add(item);
            }
            typeStats.sort((a, b) -> Long.compare(toLong(b.get("totalReps")), toLong(a.get("totalReps"))));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalStudents", sums.get("totalStudents"));
            data.put("totalReps",     totalReps);
            data.put("totalRecords",  today.get("todayRecords")); // 兼容前端
            data.put("todayRecords",  today.get("todayRecords"));
            data.put("todayStudents", today.get("todayStudents"));
            data.put("weekRecords",   week.get("weekRecords"));
            data.put("typeStats",     typeStats);

            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 2. 近30天趋势（需要时间维度，join homework_scores） ──────────────────────
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTrend(HttpServletRequest request) {
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

            String sql =
                "SELECT DATE_FORMAT(h.timestamp, '%Y-%m-%d') AS date, h.exercise_type AS type, " +
                "COUNT(*) AS sessions, COUNT(DISTINCT h.student_id) AS students, " +
                "COALESCE(SUM(h.`count`), 0) AS totalReps " +
                "FROM homework_scores h " +
                "JOIN users1 u1 ON u1.student_id = h.student_id " +
                "WHERE h.timestamp >= DATE_SUB(CURDATE(), INTERVAL 29 DAY) AND " + joinWhere +
                " GROUP BY DATE_FORMAT(h.timestamp, '%Y-%m-%d'), h.exercise_type " +
                "ORDER BY date ASC, totalReps DESC";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 3. 班级排名 ───────────────────────────────────────────────────────────────
    // totalRecords  = homework_scores 实际提交条数
    // activeStudents = 有过提交记录的去重学生数
    // totalReps     = homework_scores.count 之和（实际完成次数）
    // avgReps       = totalReps / activeStudents
    @GetMapping("/class-rank")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getClassRank(HttpServletRequest request) {
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
            String sql =
                "SELECT u.class_name AS className, u.college AS departmentName, " +
                "COUNT(DISTINCT u.id) AS totalStudents, " +
                "COUNT(DISTINCT h.student_id) AS activeStudents, " +
                "COALESCE(SUM(h.`count`), 0) AS totalReps, " +
                "COALESCE(COUNT(h.id), 0) AS totalRecords, " +
                "ROUND(COALESCE(SUM(h.`count`), 0) / NULLIF(COUNT(DISTINCT h.student_id), 0), 1) AS avgReps " +
                "FROM users1 u " +
                "LEFT JOIN homework_scores h ON h.student_id = u.student_id " +
                "WHERE " + where + " AND u.class_name IS NOT NULL AND u.class_name != '' " +
                "GROUP BY u.class_name, u.college " +
                "ORDER BY totalReps DESC " +
                "LIMIT 50";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 4. 院系排名（仅校级/超级管理员可用） ──────────────────────────────────────
    @GetMapping("/department-rank")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDepartmentRank(HttpServletRequest request) {
        AdminContext ctx;
        try {
            ctx = resolveAdmin(request);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
        if (ctx.isDeptAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error("权限不足，仅校级管理员可查看院系排名"));
        }
        try {
            Object[] scope = buildUserScope(ctx);
            String where    = (String)   scope[0];
            Object[] params = (Object[]) scope[1];

            String sql =
                "SELECT u.college AS departmentName, " +
                "COUNT(DISTINCT u.id) AS totalStudents, " +
                "COUNT(DISTINCT h.student_id) AS activeStudents, " +
                "COALESCE(SUM(h.`count`), 0) AS totalReps, " +
                "COALESCE(COUNT(h.id), 0) AS totalRecords, " +
                "ROUND(COALESCE(SUM(h.`count`), 0) / NULLIF(COUNT(DISTINCT h.student_id), 0), 1) AS avgReps, " +
                "COUNT(DISTINCT u.class_name) AS classCount " +
                "FROM users1 u " +
                "LEFT JOIN homework_scores h ON h.student_id = u.student_id " +
                "WHERE " + where + " AND u.college IS NOT NULL AND u.college != '' " +
                "GROUP BY u.college " +
                "ORDER BY totalReps DESC";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 5. 院内班级排名（院级管理员专用） ─────────────────────────────────────────
    @GetMapping("/dept-class-rank")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDeptClassRank(HttpServletRequest request) {
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

            String sql =
                "SELECT u.class_name AS className, u.college AS departmentName, " +
                "COUNT(DISTINCT u.id) AS totalStudents, " +
                "COUNT(DISTINCT h.student_id) AS activeStudents, " +
                "COALESCE(SUM(h.`count`), 0) AS totalReps, " +
                "COALESCE(COUNT(h.id), 0) AS totalRecords, " +
                "ROUND(COALESCE(SUM(h.`count`), 0) / NULLIF(COUNT(DISTINCT h.student_id), 0), 1) AS avgReps " +
                "FROM users1 u " +
                "LEFT JOIN homework_scores h ON h.student_id = u.student_id " +
                "WHERE " + where + " AND u.class_name IS NOT NULL AND u.class_name != '' " +
                "GROUP BY u.class_name, u.college " +
                "ORDER BY totalReps DESC " +
                "LIMIT 30";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
            return ResponseEntity.ok(ApiResponse.success("获取成功", rows));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    // ── 工具方法 ──────────────────────────────────────────────────────────────────
    private long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }
}
