package com.example.pexitong2.service;

import com.example.pexitong2.dto.HomeworkWeeklySubmissionCompletionResponse;
import com.example.pexitong2.dto.HomeworkWeeklySubmissionGroupsResponse;
import com.example.pexitong2.entity.pe.SchoolSettings;
import com.example.pexitong2.repository.pe.SchoolSettingsRepository;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 课后作业「本周提交次数」达标率（独立 SQL）。提交次数 = homework_scores 行数（全运动类型合计）；自然周：周一至周日。
 */
@Service
public class HomeworkWeeklySubmissionCompletionService {

    private static final String WEEK_SUBQUERY =
            "SELECT h.student_id, COUNT(*) AS sub_cnt FROM homework_scores h "
                    + "WHERE h.timestamp >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) "
                    + "AND h.timestamp < DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY) "
                    + "GROUP BY h.student_id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SchoolSettingsRepository schoolSettingsRepository;

    public HomeworkWeeklySubmissionCompletionResponse buildOverall(HttpServletRequest request) {
        HomeworkAdminContext ctx = resolveAdmin(request);
        ScopePair scope = buildUserScope(ctx);
        return buildOverall(ctx, scope);
    }

    public HomeworkWeeklySubmissionGroupsResponse buildGroups(HttpServletRequest request, String view) {
        if (view == null || view.isBlank()) {
            throw new IllegalArgumentException("view 不能为空：school 或 college");
        }
        String vm = view.trim().toLowerCase();
        if (!"school".equals(vm) && !"college".equals(vm)) {
            throw new IllegalArgumentException("view 必须为 school 或 college");
        }
        HomeworkAdminContext ctx = resolveAdmin(request);
        ScopePair scope = buildUserScope(ctx);
        return buildGroups(ctx, scope, vm);
    }

    private HomeworkWeeklySubmissionCompletionResponse buildOverall(HomeworkAdminContext ctx, ScopePair scope) {
        int required;
        int semesterWeeks;
        boolean configured;

        if (!ctx.isSuperAdmin() && ctx.school != null && !ctx.school.isBlank()) {
            Optional<SchoolSettings> opt = schoolSettingsRepository.findBySchool(ctx.school);
            required = opt.map(SchoolSettings::getHomeworkWeeklySubmissionsRequired).orElse(3);
            semesterWeeks = opt.map(SchoolSettings::getHomeworkSubmissionSemesterWeeks).orElse(16);
        } else {
            required = 0;
            semesterWeeks = 16;
        }
        if (required < 0) required = 0;
        if (semesterWeeks < 1) semesterWeeks = 1;
        configured = required > 0;

        Map<String, Object> weekBounds = jdbcTemplate.queryForMap(
                "SELECT DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AS week_start, "
                        + "DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 6 DAY) AS week_end");
        String weekStart = ((Date) weekBounds.get("week_start")).toLocalDate().toString();
        String weekEnd = ((Date) weekBounds.get("week_end")).toLocalDate().toString();

        HomeworkWeeklySubmissionCompletionResponse resp = new HomeworkWeeklySubmissionCompletionResponse();
        resp.setRequiredSubmissionsPerWeek(required);
        resp.setSubmissionSemesterWeeks(semesterWeeks);
        resp.setWeekStartDate(weekStart);
        resp.setWeekEndDate(weekEnd);
        resp.setScopeDescription(scopeDescription(ctx));
        resp.setRequirementConfigured(configured);

        String countSql = "SELECT COUNT(*) FROM users1 u WHERE " + scope.where;
        Long totalObj = jdbcTemplate.queryForObject(countSql, Long.class, scope.params);
        int total = totalObj != null ? totalObj.intValue() : 0;
        resp.setTotalStudents(total);

        if (!configured || total == 0) {
            resp.setCompliantStudents(configured ? 0 : null);
            resp.setCompletionRatePercent(configured ? 0.0 : null);
            return resp;
        }

        String join = "LEFT JOIN (" + WEEK_SUBQUERY + ") s ON s.student_id = u.student_id ";
        String aggSql = "SELECT SUM(CASE WHEN IFNULL(s.sub_cnt, 0) >= ? THEN 1 ELSE 0 END) AS compliant "
                + "FROM users1 u " + join + "WHERE " + scope.where;
        List<Object> aggParams = new ArrayList<>();
        aggParams.add(required);
        Collections.addAll(aggParams, scope.params);

        Long compObj = jdbcTemplate.queryForObject(aggSql, Long.class, aggParams.toArray());
        int comp = compObj != null ? compObj.intValue() : 0;
        resp.setCompliantStudents(comp);
        BigDecimal rate = BigDecimal.valueOf((double) comp * 100.0 / total)
                .setScale(2, RoundingMode.HALF_UP);
        resp.setCompletionRatePercent(rate.doubleValue());
        return resp;
    }

    private HomeworkWeeklySubmissionGroupsResponse buildGroups(HomeworkAdminContext ctx, ScopePair scope, String vm) {
        int required;
        int semesterWeeks;
        boolean configured;

        if (!ctx.isSuperAdmin() && ctx.school != null && !ctx.school.isBlank()) {
            Optional<SchoolSettings> opt = schoolSettingsRepository.findBySchool(ctx.school);
            required = opt.map(SchoolSettings::getHomeworkWeeklySubmissionsRequired).orElse(3);
            semesterWeeks = opt.map(SchoolSettings::getHomeworkSubmissionSemesterWeeks).orElse(16);
        } else {
            required = 0;
            semesterWeeks = 16;
        }
        if (required < 0) required = 0;
        configured = required > 0;

        Map<String, Object> weekBounds = jdbcTemplate.queryForMap(
                "SELECT DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AS week_start, "
                        + "DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 6 DAY) AS week_end");
        String weekStart = ((Date) weekBounds.get("week_start")).toLocalDate().toString();
        String weekEnd = ((Date) weekBounds.get("week_end")).toLocalDate().toString();

        HomeworkWeeklySubmissionGroupsResponse out = new HomeworkWeeklySubmissionGroupsResponse();
        out.setView(vm);
        out.setRequiredSubmissionsPerWeek(required);
        out.setSubmissionSemesterWeeks(Math.max(1, semesterWeeks));
        out.setWeekStartDate(weekStart);
        out.setWeekEndDate(weekEnd);
        out.setRequirementConfigured(configured);

        String join = "LEFT JOIN (" + WEEK_SUBQUERY + ") s ON s.student_id = u.student_id ";

        if (!configured) {
            if ("school".equals(vm)) {
                String sql = "SELECT u.college AS group_name, NULL AS department_name, COUNT(*) AS total_students "
                        + "FROM users1 u WHERE " + scope.where
                        + " AND u.college IS NOT NULL AND TRIM(u.college) <> '' "
                        + "GROUP BY u.college ORDER BY group_name";
                appendGroupsNoRate(out, jdbcTemplate.queryForList(sql, scope.params));
            } else {
                String sql = "SELECT u.class_name AS group_name, u.college AS department_name, COUNT(*) AS total_students "
                        + "FROM users1 u WHERE " + scope.where
                        + " AND u.class_name IS NOT NULL AND TRIM(u.class_name) <> '' "
                        + "GROUP BY u.class_name, u.college ORDER BY group_name";
                appendGroupsNoRate(out, jdbcTemplate.queryForList(sql, scope.params));
            }
            return out;
        }

        if ("school".equals(vm)) {
            String sql = "SELECT u.college AS group_name, NULL AS department_name, "
                    + "COUNT(*) AS total_students, "
                    + "SUM(CASE WHEN IFNULL(s.sub_cnt, 0) >= ? THEN 1 ELSE 0 END) AS compliant "
                    + "FROM users1 u " + join + "WHERE " + scope.where
                    + " AND u.college IS NOT NULL AND TRIM(u.college) <> '' "
                    + "GROUP BY u.college ORDER BY group_name";
            List<Object> qParams = new ArrayList<>();
            qParams.add(required);
            Collections.addAll(qParams, scope.params);
            appendGroupsWithRate(out, jdbcTemplate.queryForList(sql, qParams.toArray()));
        } else {
            String sql = "SELECT u.class_name AS group_name, u.college AS department_name, "
                    + "COUNT(*) AS total_students, "
                    + "SUM(CASE WHEN IFNULL(s.sub_cnt, 0) >= ? THEN 1 ELSE 0 END) AS compliant "
                    + "FROM users1 u " + join + "WHERE " + scope.where
                    + " AND u.class_name IS NOT NULL AND TRIM(u.class_name) <> '' "
                    + "GROUP BY u.class_name, u.college ORDER BY group_name";
            List<Object> qParams = new ArrayList<>();
            qParams.add(required);
            Collections.addAll(qParams, scope.params);
            appendGroupsWithRate(out, jdbcTemplate.queryForList(sql, qParams.toArray()));
        }
        return out;
    }

    private void appendGroupsNoRate(HomeworkWeeklySubmissionGroupsResponse out, List<Map<String, Object>> rows) {
        for (Map<String, Object> r : rows) {
            HomeworkWeeklySubmissionGroupsResponse.HomeworkWeeklyGroupRow g =
                    new HomeworkWeeklySubmissionGroupsResponse.HomeworkWeeklyGroupRow();
            g.setGroupName((String) r.get("group_name"));
            g.setDepartmentName((String) r.get("department_name"));
            g.setTotalStudents(toInt(r.get("total_students")));
            g.setCompliantStudents(null);
            g.setCompletionRatePercent(null);
            out.getGroups().add(g);
        }
    }

    private void appendGroupsWithRate(HomeworkWeeklySubmissionGroupsResponse out, List<Map<String, Object>> rows) {
        for (Map<String, Object> r : rows) {
            HomeworkWeeklySubmissionGroupsResponse.HomeworkWeeklyGroupRow g =
                    new HomeworkWeeklySubmissionGroupsResponse.HomeworkWeeklyGroupRow();
            g.setGroupName((String) r.get("group_name"));
            g.setDepartmentName((String) r.get("department_name"));
            int tot = toInt(r.get("total_students"));
            int comp = toInt(r.get("compliant"));
            g.setTotalStudents(tot);
            g.setCompliantStudents(comp);
            if (tot > 0) {
                g.setCompletionRatePercent(BigDecimal.valueOf((double) comp * 100.0 / tot)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue());
            } else {
                g.setCompletionRatePercent(0.0);
            }
            out.getGroups().add(g);
        }
    }

    private static int toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private String scopeDescription(HomeworkAdminContext ctx) {
        if (ctx.isSuperAdmin()) return "全校（超管）";
        if (ctx.isCounselor() && ctx.counselorClasses != null && !ctx.counselorClasses.isEmpty()) {
            return "管辖班级";
        }
        if (ctx.isDeptAdmin() || ctx.isCounselor()) {
            if (ctx.department != null && !ctx.department.isBlank()) return ctx.department.trim();
        }
        return "全校";
    }

    private HomeworkAdminContext resolveAdmin(HttpServletRequest request) {
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

            if ("super_admin".equals(userType)) {
                return new HomeworkAdminContext(userType, null, null, null);
            }

            String userId = jwtUtil.extractUserId(token);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT school, department_name FROM users WHERE id = ? LIMIT 1", userId);
            if (rows.isEmpty()) throw new SecurityException("管理员账号不存在");

            String school = (String) rows.get(0).get("school");
            String department = (String) rows.get(0).get("department_name");

            if (school == null || school.isBlank()) {
                throw new SecurityException("管理员账号未绑定学校信息");
            }

            List<String> counselorClasses = null;
            if ("counselor".equals(userType)) {
                if (department == null || department.isBlank()) {
                    List<Map<String, Object>> deptRows = jdbcTemplate.queryForList(
                            "SELECT DISTINCT department_name FROM counselor_class_assignments WHERE counselor_id = ? AND department_name IS NOT NULL LIMIT 1",
                            userId);
                    if (!deptRows.isEmpty()) department = (String) deptRows.get(0).get("department_name");
                }
                List<Map<String, Object>> classRows = jdbcTemplate.queryForList(
                        "SELECT class_name FROM counselor_class_assignments WHERE counselor_id = ?", userId);
                counselorClasses = new ArrayList<>();
                for (Map<String, Object> r : classRows) {
                    counselorClasses.add((String) r.get("class_name"));
                }
            }
            return new HomeworkAdminContext(userType, school, department, counselorClasses);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("令牌解析失败: " + e.getMessage());
        }
    }

    private ScopePair buildUserScope(HomeworkAdminContext ctx) {
        if (ctx.isSuperAdmin()) {
            return new ScopePair("1=1", new Object[0]);
        }
        if (ctx.isCounselor() && ctx.counselorClasses != null && !ctx.counselorClasses.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(ctx.counselorClasses.size(), "?"));
            List<Object> params = new ArrayList<>();
            params.add(ctx.school);
            params.addAll(ctx.counselorClasses);
            return new ScopePair("u.school = ? AND u.class_name IN (" + placeholders + ")",
                    params.toArray());
        }
        if ((ctx.isDeptAdmin() || ctx.isCounselor()) && ctx.department != null && !ctx.department.isBlank()) {
            return new ScopePair("u.school = ? AND u.college = ?",
                    new Object[]{ctx.school, ctx.department});
        }
        return new ScopePair("u.school = ?", new Object[]{ctx.school});
    }

    private static final class HomeworkAdminContext {
        final String userType;
        final String school;
        final String department;
        final List<String> counselorClasses;

        HomeworkAdminContext(String userType, String school, String department, List<String> counselorClasses) {
            this.userType = userType;
            this.school = school;
            this.department = department;
            this.counselorClasses = counselorClasses;
        }

        boolean isDeptAdmin() { return "department_admin".equals(userType); }
        boolean isCounselor() { return "counselor".equals(userType); }
        boolean isSuperAdmin() { return "super_admin".equals(userType); }
    }

    private static final class ScopePair {
        final String where;
        final Object[] params;

        ScopePair(String where, Object[] params) {
            this.where = where;
            this.params = params;
        }
    }
}
