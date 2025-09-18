package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.entity.pe.Activity;
import com.example.pexitong2.entity.pe.AttendanceRecord;
import com.example.pexitong2.entity.pe.PeUser;
import com.example.pexitong2.repository.pe.ActivityRepository;
import com.example.pexitong2.repository.pe.AttendanceRecordRepository;
import com.example.pexitong2.repository.pe.PeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到记录管理服务
 */
@Service
public class AttendanceRecordService {
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private PeUserRepository peUserRepository;
    
    @Autowired
    private PePermissionService permissionService;
    
    /**
     * 获取签到记录列表
     */
    public PageResponse<AttendanceRecordResponse> getAttendanceRecords(
            int page, int pageSize, String activityId, String studentId, 
            String studentName, String startDate, String endDate, 
            Boolean isCheckedOut, Integer minDuration, Integer maxDuration, 
            String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
        
        Page<AttendanceRecord> recordPage = attendanceRecordRepository.findAttendanceRecordsWithFilters(
            activityId, studentId, studentName, start, end, isCheckedOut, 
            minDuration, maxDuration, allowedSchool, allowedCollege, pageable);
        
        List<AttendanceRecordResponse> responses = recordPage.getContent().stream()
            .map(AttendanceRecordResponse::new)
            .collect(Collectors.toList());
        
        return new PageResponse<>(responses, recordPage.getTotalElements(), page, pageSize);
    }
    
    /**
     * 获取单个签到记录详情（包含关联信息）
     */
    public AttendanceRecordResponse getAttendanceRecordById(String id, String currentUserId) {
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        AttendanceRecord record = attendanceRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("签到记录不存在"));
        
        // 验证是否在权限范围内
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        // 获取用户信息进行权限验证
        PeUser user = peUserRepository.findById(record.getUserId()).orElse(null);
        if (user != null) {
            if (allowedSchool != null && !allowedSchool.equals(user.getSchool())) {
                throw new RuntimeException("权限不足：无法查看该签到记录");
            }
            if (allowedCollege != null && !allowedCollege.equals(user.getCollege())) {
                throw new RuntimeException("权限不足：无法查看该签到记录");
            }
        }
        
        AttendanceRecordResponse response = new AttendanceRecordResponse(record);
        
        // 添加关联信息
        Activity activity = activityRepository.findById(record.getActivityId()).orElse(null);
        if (activity != null) {
            response.setActivity(new AttendanceRecordResponse.ActivitySummary(
                activity.getId(), activity.getTitle(), activity.getCategory()));
        }
        
        if (user != null) {
            response.setUser(new AttendanceRecordResponse.UserSummary(
                user.getId(), user.getName(), user.getStudentId(), 
                user.getSchool(), user.getCollege()));
        }
        
        return response;
    }
    
    /**
     * 获取签到统计数据
     */
    public StatisticsResponse.AttendanceStatistics getAttendanceStatistics(
            String startDate, String endDate, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
        
        List<Object[]> statistics = attendanceRecordRepository.getAttendanceStatistics(
            start, end, allowedSchool, allowedCollege);
        
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
    
    /**
     * 导出签到记录
     * TODO: 实现Excel导出功能
     */
    public byte[] exportAttendanceRecords(
            String activityId, String studentId, String studentName, 
            String startDate, String endDate, Boolean isCheckedOut, 
            Integer minDuration, Integer maxDuration, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
        
        // 分页查询所有记录（这里简化处理，实际应用中可能需要分批处理）
        Pageable pageable = PageRequest.of(0, 10000); // 限制最大导出数量
        
        attendanceRecordRepository.findAttendanceRecordsWithFilters(
            activityId, studentId, studentName, start, end, isCheckedOut, 
            minDuration, maxDuration, allowedSchool, allowedCollege, pageable);
        
        // TODO: 实现Excel生成逻辑
        // 这里返回空字节数组作为占位符
        return new byte[0];
    }
}
