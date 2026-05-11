package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.SunshineRunWeeklyCompletionResponse;
import com.example.pexitong2.dto.pe.SunshineRunWeeklyGroupsResponse;
import com.example.pexitong2.entity.pe.SchoolSettings;
import com.example.pexitong2.repository.pe.SchoolSettingsRepository;
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
 * 本周阳光跑达标率（独立 SQL，不影响既有统计大屏查询逻辑）。
 * 自然周：周一至周日（与 MySQL WEEKDAY 一致：0=周一）。
 */
@Service
public class SunshineRunWeeklyCompletionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SchoolSettingsRepository schoolSettingsRepository;

    private static final String WEEKLY_RUN_JOIN = "LEFT JOIN ("
            + "SELECT r.user_id, COUNT(*) AS run_cnt FROM sunshine_run_records r "
            + "WHERE r.created_at >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) "
            + "AND r.created_at < DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY) "
            + "GROUP BY r.user_id"
            + ") w ON w.user_id = u.id ";

    public SunshineRunWeeklyCompletionResponse buildForAdmin(String adminUserId) {
        WeeklyScopeContext ctx = resolveWeeklyScope(adminUserId);

        String countSql = "SELECT COUNT(*) FROM users1 u WHERE " + ctx.userWhere;
        Long totalStudents = jdbcTemplate.queryForObject(countSql, Long.class, ctx.params.toArray());
        int total = totalStudents != null ? totalStudents.intValue() : 0;

        SunshineRunWeeklyCompletionResponse resp = new SunshineRunWeeklyCompletionResponse();
        resp.setRequiredRunsPerWeek(ctx.runsPerWeek);
        resp.setTotalWeeksInPlan(ctx.totalWeeks);
        resp.setWeekStartDate(ctx.weekStart);
        resp.setWeekEndDate(ctx.weekEnd);
        resp.setScopeDescription(ctx.scopeDescription);
        resp.setTotalStudents(total);

        resp.setRequirementConfigured(ctx.configured);
        if (!ctx.configured || total == 0) {
            resp.setCompliantStudents(ctx.configured ? 0 : null);
            resp.setCompletionRatePercent(ctx.configured ? 0.0 : null);
            return resp;
        }

        String aggSql = "SELECT SUM(CASE WHEN IFNULL(w.run_cnt, 0) >= ? THEN 1 ELSE 0 END) AS compliant "
                + "FROM users1 u " + WEEKLY_RUN_JOIN + "WHERE " + ctx.userWhere;
        List<Object> aggParams = new ArrayList<>();
        aggParams.add(ctx.runsPerWeek);
        aggParams.addAll(ctx.params);

        Long compliant = jdbcTemplate.queryForObject(aggSql, Long.class, aggParams.toArray());
        int comp = compliant != null ? compliant.intValue() : 0;
        resp.setCompliantStudents(comp);

        BigDecimal rate = BigDecimal.valueOf((double) comp * 100.0 / total)
                .setScale(2, RoundingMode.HALF_UP);
        resp.setCompletionRatePercent(rate.doubleValue());
        return resp;
    }

    /**
     * 按院系（view=school）或班级（view=college）统计本周完成率，与阳光跑大屏分组一致。
     */
    public SunshineRunWeeklyGroupsResponse buildGroupsForAdmin(String adminUserId, String view) {
        if (view == null || view.isBlank()) {
            throw new RuntimeException("view 不能为空：school 或 college");
        }
        String vm = view.trim().toLowerCase();
        if (!"school".equals(vm) && !"college".equals(vm)) {
            throw new RuntimeException("view 必须为 school 或 college");
        }

        WeeklyScopeContext ctx = resolveWeeklyScope(adminUserId);
        String groupCol = "school".equals(vm) ? "u.college" : "u.class_name";

        SunshineRunWeeklyGroupsResponse out = new SunshineRunWeeklyGroupsResponse();
        out.setView(vm);
        out.setRequiredRunsPerWeek(ctx.runsPerWeek);
        out.setTotalWeeksInPlan(ctx.totalWeeks);
        out.setWeekStartDate(ctx.weekStart);
        out.setWeekEndDate(ctx.weekEnd);
        out.setRequirementConfigured(ctx.configured);

        List<SunshineRunWeeklyGroupsResponse.GroupWeeklyCompletion> list = new ArrayList<>();

        if (!ctx.configured) {
            String sql = "SELECT " + groupCol + " AS group_name, COUNT(*) AS total_students "
                    + "FROM users1 u WHERE " + ctx.userWhere
                    + " AND " + groupCol + " IS NOT NULL AND TRIM(" + groupCol + ") <> '' "
                    + "GROUP BY " + groupCol + " ORDER BY group_name";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, ctx.params.toArray());
            for (Map<String, Object> r : rows) {
                SunshineRunWeeklyGroupsResponse.GroupWeeklyCompletion g =
                        new SunshineRunWeeklyGroupsResponse.GroupWeeklyCompletion();
                g.setGroupName((String) r.get("group_name"));
                g.setTotalStudents(toInt(r.get("total_students")));
                g.setCompliantStudents(null);
                g.setCompletionRatePercent(null);
                list.add(g);
            }
            out.setGroups(list);
            return out;
        }

        String sql = "SELECT " + groupCol + " AS group_name, COUNT(*) AS total_students, "
                + "SUM(CASE WHEN IFNULL(w.run_cnt, 0) >= ? THEN 1 ELSE 0 END) AS compliant "
                + "FROM users1 u " + WEEKLY_RUN_JOIN + "WHERE " + ctx.userWhere
                + " AND " + groupCol + " IS NOT NULL AND TRIM(" + groupCol + ") <> '' "
                + "GROUP BY " + groupCol + " ORDER BY group_name";
        List<Object> qParams = new ArrayList<>();
        qParams.add(ctx.runsPerWeek);
        qParams.addAll(ctx.params);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, qParams.toArray());
        for (Map<String, Object> r : rows) {
            SunshineRunWeeklyGroupsResponse.GroupWeeklyCompletion g =
                    new SunshineRunWeeklyGroupsResponse.GroupWeeklyCompletion();
            String name = (String) r.get("group_name");
            int tot = toInt(r.get("total_students"));
            int comp = toInt(r.get("compliant"));
            g.setGroupName(name);
            g.setTotalStudents(tot);
            g.setCompliantStudents(comp);
            if (tot > 0) {
                g.setCompletionRatePercent(
                        BigDecimal.valueOf((double) comp * 100.0 / tot)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue());
            } else {
                g.setCompletionRatePercent(0.0);
            }
            list.add(g);
        }
        out.setGroups(list);
        return out;
    }

    private WeeklyScopeContext resolveWeeklyScope(String adminUserId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT user_type, school, department_name FROM users WHERE id = ?", adminUserId);
        String userType = String.valueOf(row.get("user_type"));
        String school = (String) row.get("school");
        String dept = (String) row.get("department_name");

        if (school == null || school.isBlank()) {
            throw new RuntimeException("未配置学校信息");
        }

        boolean schoolLevel = "school_admin".equals(userType) || "super_admin".equals(userType);
        boolean deptAdmin = "department_admin".equals(userType);
        boolean counselor = "counselor".equals(userType);

        if (!schoolLevel && !deptAdmin && !counselor) {
            throw new RuntimeException("权限不足");
        }

        Optional<SchoolSettings> optSettings = schoolSettingsRepository.findBySchool(school);
        int runsPerWeek = optSettings.map(SchoolSettings::getSunshineRunRunsPerWeek).orElse(3);
        int totalWeeks = optSettings.map(SchoolSettings::getSunshineRunTotalWeeks).orElse(16);
        if (runsPerWeek < 0) runsPerWeek = 0;
        if (totalWeeks < 1) totalWeeks = 1;

        Map<String, Object> weekBounds = jdbcTemplate.queryForMap(
                "SELECT DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AS week_start, "
                        + "DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 6 DAY) AS week_end");
        String weekStart = ((Date) weekBounds.get("week_start")).toLocalDate().toString();
        String weekEnd = ((Date) weekBounds.get("week_end")).toLocalDate().toString();

        StringBuilder userWhere = new StringBuilder("u.school = ? AND u.role = 'STUDENT'");
        List<Object> params = new ArrayList<>();
        params.add(school);

        String scopeDescription;

        if (schoolLevel) {
            scopeDescription = "全校";
        } else if (deptAdmin) {
            if (dept == null || dept.isBlank()) {
                throw new RuntimeException("院级管理员未配置所属院系");
            }
            userWhere.append(" AND u.college = ?");
            params.add(dept.trim());
            scopeDescription = dept.trim();
        } else {
            List<String> counselorClasses = new ArrayList<>();
            List<Map<String, Object>> classRows = jdbcTemplate.queryForList(
                    "SELECT class_name FROM counselor_class_assignments WHERE counselor_id = ?", adminUserId);
            for (Map<String, Object> r : classRows) {
                counselorClasses.add((String) r.get("class_name"));
            }
            if (dept == null || dept.isBlank()) {
                List<Map<String, Object>> deptRows = jdbcTemplate.queryForList(
                        "SELECT DISTINCT department_name FROM counselor_class_assignments WHERE counselor_id = ? "
                                + "AND department_name IS NOT NULL LIMIT 1",
                        adminUserId);
                if (!deptRows.isEmpty()) {
                    dept = (String) deptRows.get(0).get("department_name");
                }
            }
            if (counselorClasses.isEmpty()) {
                userWhere.append(" AND 1=0");
                scopeDescription = "辅导员（未分配班级）";
            } else {
                String placeholders = String.join(",", Collections.nCopies(counselorClasses.size(), "?"));
                userWhere.append(" AND u.class_name IN (").append(placeholders).append(")");
                params.addAll(counselorClasses);
                scopeDescription = "管辖班级";
            }
        }

        WeeklyScopeContext ctx = new WeeklyScopeContext();
        ctx.userWhere = userWhere.toString();
        ctx.params = params;
        ctx.scopeDescription = scopeDescription;
        ctx.school = school;
        ctx.runsPerWeek = runsPerWeek;
        ctx.totalWeeks = totalWeeks;
        ctx.weekStart = weekStart;
        ctx.weekEnd = weekEnd;
        ctx.configured = runsPerWeek > 0;
        return ctx;
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

    private static final class WeeklyScopeContext {
        String school;
        String userWhere;
        List<Object> params;
        String scopeDescription;
        int runsPerWeek;
        int totalWeeks;
        String weekStart;
        String weekEnd;
        boolean configured;
    }
}
