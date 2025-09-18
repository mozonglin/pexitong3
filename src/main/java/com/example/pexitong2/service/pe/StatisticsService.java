package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.StatisticsResponse;
import com.example.pexitong2.entity.pe.Activity;
import com.example.pexitong2.entity.pe.PeUser;
import com.example.pexitong2.repository.pe.ActivityRepository;
import com.example.pexitong2.repository.pe.AttendanceRecordRepository;
import com.example.pexitong2.repository.pe.MorningExerciseRepository;
import com.example.pexitong2.repository.pe.PeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计数据服务
 */
@Service
public class StatisticsService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private PeUserRepository peUserRepository;
    
    @Autowired
    private MorningExerciseRepository morningExerciseRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private PePermissionService permissionService;
    
    /**
     * 获取总体统计数据
     */
    public StatisticsResponse getOverallStatistics(
            String startDate, String endDate, String school, String college, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        // 应用权限过滤
        String filteredSchool = allowedSchool != null ? allowedSchool : school;
        String filteredCollege = allowedCollege != null ? allowedCollege : college;
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
        LocalDate dateStart = startDate != null ? LocalDate.parse(startDate.substring(0, 10)) : null;
        LocalDate dateEnd = endDate != null ? LocalDate.parse(endDate.substring(0, 10)) : null;
        
        // 活动统计
        StatisticsResponse.ActivityStatistics activityStats = getActivityStatistics(
            filteredSchool, filteredCollege);
        
        // 用户统计
        StatisticsResponse.UserStatistics userStats = getUserStatistics(
            filteredSchool, filteredCollege);
        
        // 早操统计
        StatisticsResponse.MorningExerciseStatistics morningExerciseStats = 
            getMorningExerciseStatistics(dateStart, dateEnd, filteredSchool, filteredCollege);
        
        // 签到统计
        StatisticsResponse.AttendanceStatistics attendanceStats = 
            getAttendanceStatistics(start, end, filteredSchool, filteredCollege);
        
        return new StatisticsResponse(activityStats, userStats, morningExerciseStats, attendanceStats);
    }
    
    /**
     * 获取活动统计（按类别分组）
     */
    public StatisticsResponse.ActivityChartData getActivityStatisticsByCategory(
            String startDate, String endDate, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
        
        List<Object[]> statistics = activityRepository.getActivityStatisticsByCategory(
            start, end, allowedSchool, allowedCollege);
        
        List<StatisticsResponse.ChartItem> chartData = statistics.stream()
            .map(row -> new StatisticsResponse.ChartItem(
                (String) row[0],                    // category
                ((Number) row[1]).longValue(),      // count
                ((Number) row[2]).longValue()       // participants
            ))
            .collect(Collectors.toList());
        
        // 计算汇总数据
        long totalActivities = chartData.stream().mapToLong(StatisticsResponse.ChartItem::getCount).sum();
        long totalParticipants = chartData.stream().mapToLong(StatisticsResponse.ChartItem::getParticipants).sum();
        double averageParticipants = totalActivities > 0 ? (double) totalParticipants / totalActivities : 0.0;
        
        StatisticsResponse.ChartSummary summary = new StatisticsResponse.ChartSummary(
            totalActivities, totalParticipants, averageParticipants);
        
        return new StatisticsResponse.ActivityChartData(chartData, summary);
    }
    
    /**
     * 获取早操月度统计
     */
    public List<Object[]> getMorningExerciseMonthlyStatistics(
            String startDate, String endDate, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        
        return morningExerciseRepository.getMorningExerciseMonthlyStatistics(
            start, end, allowedSchool, allowedCollege);
    }
    
    /**
     * 私有方法：获取活动统计
     */
    private StatisticsResponse.ActivityStatistics getActivityStatistics(String school, String college) {
        long total = activityRepository.countByApprovalStatusAndSchoolAndCollege(null, school, college);
        long pending = activityRepository.countByApprovalStatusAndSchoolAndCollege(
            Activity.ApprovalStatus.PENDING, school, college);
        long approved = activityRepository.countByApprovalStatusAndSchoolAndCollege(
            Activity.ApprovalStatus.APPROVED, school, college);
        long rejected = activityRepository.countByApprovalStatusAndSchoolAndCollege(
            Activity.ApprovalStatus.REJECTED, school, college);
        
        return new StatisticsResponse.ActivityStatistics(total, pending, approved, rejected);
    }
    
    /**
     * 私有方法：获取用户统计
     */
    private StatisticsResponse.UserStatistics getUserStatistics(String school, String college) {
        long total = peUserRepository.countBySchoolAndCollege(school, college);
        long students = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.STUDENT, school, college);
        long checkers = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.CHECKER, school, college);
        long subCheckers = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.SUB_CHECKER, school, college);
        long admins = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.ADMIN, school, college);
        
        return new StatisticsResponse.UserStatistics(total, students, checkers, subCheckers, admins);
    }
    
    /**
     * 私有方法：获取早操统计
     */
    private StatisticsResponse.MorningExerciseStatistics getMorningExerciseStatistics(
            LocalDate startDate, LocalDate endDate, String school, String college) {
        
        List<Object[]> statistics = morningExerciseRepository.getMorningExerciseStatistics(
            startDate, endDate, school, college);
        
        if (statistics.isEmpty()) {
            return new StatisticsResponse.MorningExerciseStatistics(0L, 0L, 0L);
        }
        
        Object[] row = statistics.get(0);
        long total = ((Number) row[0]).longValue();
        long active = ((Number) row[1]).longValue();
        long completed = total - active;
        
        return new StatisticsResponse.MorningExerciseStatistics(total, active, completed);
    }
    
    /**
     * 私有方法：获取签到统计
     */
    private StatisticsResponse.AttendanceStatistics getAttendanceStatistics(
            LocalDateTime startDate, LocalDateTime endDate, String school, String college) {
        
        List<Object[]> statistics = attendanceRecordRepository.getAttendanceStatistics(
            startDate, endDate, school, college);
        
        if (statistics.isEmpty()) {
            return new StatisticsResponse.AttendanceStatistics(0L, 0L, 0.0, 0L);
        }
        
        Object[] row = statistics.get(0);
        long totalCheckins = ((Number) row[0]).longValue();
        long totalCheckouts = ((Number) row[1]).longValue();
        double averageDuration = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
        long totalPoints = row[3] != null ? ((Number) row[3]).longValue() : 0L;
        
        return new StatisticsResponse.AttendanceStatistics(
            totalCheckins, totalCheckouts, averageDuration, totalPoints);
    }
}




