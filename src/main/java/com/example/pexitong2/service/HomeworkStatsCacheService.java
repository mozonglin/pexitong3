package com.example.pexitong2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 课后作业统计缓存服务
 * 将高频、重量级的统计查询结果缓存 5 分钟（见 application.properties caffeine.spec），
 * 避免并发管理员登录时同时发起大量相同查询导致连接池耗尽。
 */
@Service
public class HomeworkStatsCacheService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 院系排名（校级/超级管理员）
     * cacheKey = userScope + ":" + period
     */
    @Cacheable(value = "homeworkDeptRank", key = "#scopeKey + ':' + #period")
    public List<Map<String, Object>> getDepartmentRank(
            String scopeKey, String where, Object[] params, String period) {

        String dateJoin = hasPeriodFilter(period)
                ? " AND " + getDateCondition("h.timestamp", period) : "";

        String sql =
            "SELECT u.college AS departmentName, " +
            "COUNT(DISTINCT u.id) AS totalStudents, " +
            "COUNT(DISTINCT h.student_id) AS activeStudents, " +
            "COALESCE(SUM(h.`count`), 0) AS totalReps, " +
            "COALESCE(COUNT(h.id), 0) AS totalRecords, " +
            "ROUND(COALESCE(SUM(h.`count`), 0) / NULLIF(COUNT(DISTINCT h.student_id), 0), 1) AS avgReps, " +
            "COUNT(DISTINCT u.class_name) AS classCount " +
            "FROM users1 u " +
            "LEFT JOIN homework_scores h ON h.student_id = u.student_id" + dateJoin + " " +
            "WHERE " + where + " AND u.college IS NOT NULL AND u.college != '' " +
            "GROUP BY u.college " +
            "ORDER BY totalReps DESC";

        return jdbcTemplate.queryForList(sql, params);
    }

    /**
     * 班级排名
     * cacheKey = userScope + ":" + period
     */
    @Cacheable(value = "homeworkClassRank", key = "#scopeKey + ':' + #period")
    public List<Map<String, Object>> getClassRank(
            String scopeKey, String where, Object[] params, String period) {

        String dateJoin = hasPeriodFilter(period)
                ? " AND " + getDateCondition("h.timestamp", period) : "";

        String sql =
            "SELECT u.class_name AS className, u.college AS departmentName, " +
            "COUNT(DISTINCT u.id) AS totalStudents, " +
            "COUNT(DISTINCT h.student_id) AS activeStudents, " +
            "COALESCE(SUM(h.`count`), 0) AS totalReps, " +
            "COALESCE(COUNT(h.id), 0) AS totalRecords, " +
            "ROUND(COALESCE(SUM(h.`count`), 0) / NULLIF(COUNT(DISTINCT h.student_id), 0), 1) AS avgReps " +
            "FROM users1 u " +
            "LEFT JOIN homework_scores h ON h.student_id = u.student_id" + dateJoin + " " +
            "WHERE " + where + " AND u.class_name IS NOT NULL AND u.class_name != '' " +
            "GROUP BY u.class_name, u.college " +
            "ORDER BY totalReps DESC " +
            "LIMIT 50";

        return jdbcTemplate.queryForList(sql, params);
    }

    /**
     * 趋势数据
     */
    @Cacheable(value = "homeworkTrend", key = "#scopeKey + ':' + #period")
    public List<Map<String, Object>> getTrend(
            String scopeKey, String joinWhere, Object[] params, String period) {

        String dateFilter = hasPeriodFilter(period)
                ? getDateCondition("h.timestamp", period)
                : "h.timestamp >= DATE_SUB(CURDATE(), INTERVAL 29 DAY)";

        String sql =
            "SELECT DATE_FORMAT(h.timestamp, '%Y-%m-%d') AS date, h.exercise_type AS type, " +
            "COUNT(*) AS sessions, COUNT(DISTINCT h.student_id) AS students, " +
            "COALESCE(SUM(h.`count`), 0) AS totalReps " +
            "FROM homework_scores h " +
            "JOIN users1 u1 ON u1.student_id = h.student_id " +
            "WHERE " + dateFilter + " AND " + joinWhere +
            " GROUP BY DATE_FORMAT(h.timestamp, '%Y-%m-%d'), h.exercise_type " +
            "ORDER BY date ASC, totalReps DESC";

        return jdbcTemplate.queryForList(sql, params);
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
}
