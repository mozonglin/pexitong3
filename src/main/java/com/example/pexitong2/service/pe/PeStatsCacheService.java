package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.SunshineRunStatisticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * PE 统计缓存服务
 * 阳光跑统计接口数据变化频率低，缓存 5 分钟可大幅减轻并发时的 DB 压力。
 */
@Service
public class PeStatsCacheService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 带日期过滤的阳光跑统计（校级 school 视图 / 院级 college 视图）
     * cacheKey = adminUserId + ":" + viewMode + ":" + period
     */
    @Cacheable(value = "sunshineRunStats", key = "#cacheKey")
    public SunshineRunStatisticsResponse buildFilteredSunshineRunStats(
            String cacheKey, String adminUserId, String period, String viewMode) {

        Map<String, Object> admin = jdbcTemplate.queryForMap(
                "SELECT user_type, school, department_name FROM users WHERE id = ?", adminUserId);
        String userType = String.valueOf(admin.get("user_type"));
        String school   = (String) admin.get("school");
        String dept     = (String) admin.get("department_name");

        boolean isSchoolLevel = "school_admin".equals(userType) || "super_admin".equals(userType);
        boolean isCollegeView = "college".equals(viewMode);

        if ("school".equals(viewMode) && !isSchoolLevel) {
            throw new RuntimeException("权限不足，只有校级管理员可以查看统计数据");
        }
        if (isCollegeView && !isSchoolLevel && !"department_admin".equals(userType)) {
            throw new RuntimeException("权限不足");
        }

        String dateFilter = getDateCondition("r.created_at", period);
        String groupCol   = isCollegeView ? "u.class_name" : "u.college";
        String scopeFilter = "u.school = ?";
        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(school);

        if (isCollegeView && !isSchoolLevel) {
            scopeFilter += " AND u.college = ?";
            sqlParams.add(dept);
        }
        Object[] params = sqlParams.toArray();

        String overallSql =
            "SELECT COUNT(*) AS totalRuns, " +
            "COALESCE(SUM(r.total_distance), 0) AS totalDistanceM, " +
            "COALESCE(SUM(r.total_duration), 0) AS totalDurationMs, " +
            "COUNT(DISTINCT r.user_id) AS activeStudents " +
            "FROM sunshine_run_records r JOIN users1 u ON u.id = r.user_id " +
            "WHERE " + scopeFilter + " AND " + dateFilter;
        Map<String, Object> ov = jdbcTemplate.queryForMap(overallSql, params);

        long totalStudents = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users1 u WHERE " + scopeFilter, Long.class, params);

        int active = toInt(ov.get("activeStudents"));
        SunshineRunStatisticsResponse.SunshineRunAggregate overallAgg = buildAggregate(
                toLong(ov.get("totalRuns")),
                toLong(ov.get("totalDistanceM")),
                toLong(ov.get("totalDurationMs")),
                active > 0 ? active : 1);

        String groupSql =
            "SELECT " + groupCol + " AS groupName, " +
            "COUNT(DISTINCT r.user_id) AS activeStudents, " +
            "COUNT(*) AS totalRuns, " +
            "COALESCE(SUM(r.total_distance), 0) AS totalDistanceM, " +
            "COALESCE(SUM(r.total_duration), 0) AS totalDurationMs " +
            "FROM sunshine_run_records r JOIN users1 u ON u.id = r.user_id " +
            "WHERE " + scopeFilter + " AND " + groupCol + " IS NOT NULL AND " + groupCol + " != '' " +
            "AND " + dateFilter +
            " GROUP BY " + groupCol;
        List<Map<String, Object>> groups = jdbcTemplate.queryForList(groupSql, params);

        List<SunshineRunStatisticsResponse.GroupStatistics> groupStats = new ArrayList<>();
        List<SunshineRunStatisticsResponse.GroupRanking> groupRankings = new ArrayList<>();

        for (Map<String, Object> g : groups) {
            String name = (String) g.get("groupName");
            int cnt = toInt(g.get("activeStudents"));
            SunshineRunStatisticsResponse.SunshineRunAggregate agg = buildAggregate(
                    toLong(g.get("totalRuns")),
                    toLong(g.get("totalDistanceM")),
                    toLong(g.get("totalDurationMs")),
                    cnt > 0 ? cnt : 1);
            groupStats.add(new SunshineRunStatisticsResponse.GroupStatistics(name, cnt, agg));
            groupRankings.add(new SunshineRunStatisticsResponse.GroupRanking(
                    0, name, agg.getAvgDistancePerStudent(), agg));
        }

        groupRankings.sort((a, b) -> Double.compare(b.getAvgDistancePerStudent(), a.getAvgDistancePerStudent()));
        for (int i = 0; i < groupRankings.size(); i++) groupRankings.get(i).setRank(i + 1);

        SunshineRunStatisticsResponse resp = new SunshineRunStatisticsResponse();
        resp.setSchool(school);
        resp.setScope(isCollegeView ? (isSchoolLevel ? "全校所有院系" : dept) : "全校");
        resp.setTotalStudents((int) totalStudents);
        resp.setOverall(overallAgg);
        resp.setGroupStats(groupStats);
        resp.setGroupRankings(groupRankings);
        return resp;
    }

    private SunshineRunStatisticsResponse.SunshineRunAggregate buildAggregate(
            long totalRuns, long totalDistanceMeters, long totalDurationMs, int studentCount) {
        long totalDurationSec = totalDurationMs / 1000;
        double avgRuns = Math.round((double) totalRuns / studentCount * 100.0) / 100.0;
        double avgDist = Math.round((double) totalDistanceMeters / studentCount * 100.0) / 100.0;
        double avgDur  = Math.round((double) totalDurationSec / studentCount * 100.0) / 100.0;
        return new SunshineRunStatisticsResponse.SunshineRunAggregate(
                totalRuns, totalDistanceMeters, totalDurationSec, avgRuns, avgDist, avgDur);
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

    private int toInt(Object v) { return (int) toLong(v); }
}
