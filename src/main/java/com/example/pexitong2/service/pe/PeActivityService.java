package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.entity.pe.Activity;
import com.example.pexitong2.repository.pe.ActivityRepository;
import com.example.pexitong2.repository.pe.AttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PE活动管理服务
 */
@Service
public class PeActivityService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private PePermissionService permissionService;
    
    /**
     * 获取活动列表
     */
    public PageResponse<ActivityResponse> getActivities(
            int page, int pageSize, String approvalStatus, String category, 
            String organizerId, String startDate, String endDate, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        // 解析参数，增加错误处理
        Activity.ApprovalStatus status = null;
        if (approvalStatus != null) {
            try {
                status = Activity.ApprovalStatus.valueOf(approvalStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的审核状态: " + approvalStatus);
            }
        }
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
        
        Page<Activity> activityPage = activityRepository.findActivitiesWithFilters(
            status, category, organizerId, start, end, allowedSchool, allowedCollege, pageable);
        
        List<ActivityResponse> responses = activityPage.getContent().stream()
            .map(ActivityResponse::new)
            .collect(Collectors.toList());
        
        return new PageResponse<>(responses, activityPage.getTotalElements(), page, pageSize);
    }
    
    /**
     * 获取单个活动详情
     */
    public ActivityResponse getActivityById(String id, String currentUserId) {
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        // TODO: 验证是否在权限范围内（需要关联组织者的学校/学院信息）
        
        return new ActivityResponse(activity);
    }
    
    /**
     * 审核活动
     */
    @Transactional
    public void reviewActivity(String id, ActivityReviewRequest request, String currentUserId) {
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        // 验证审核状态
        Activity.ApprovalStatus newStatus;
        try {
            newStatus = Activity.ApprovalStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的审核状态");
        }
        
        if (newStatus != Activity.ApprovalStatus.APPROVED && 
            newStatus != Activity.ApprovalStatus.REJECTED) {
            throw new RuntimeException("只能设置为通过或拒绝");
        }
        
        // 更新活动状态
        activity.setApprovalStatus(newStatus);
        activity.setReviewedBy(currentUserId);
        activity.setReviewedAt(LocalDateTime.now());
        activity.setReviewComment(request.getComment());
        
        activityRepository.save(activity);
    }
    
    /**
     * 获取活动签到记录
     */
    public PageResponse<AttendanceRecordResponse> getActivityAttendance(
            String activityId, int page, int pageSize, String studentId, 
            Boolean isCheckedOut, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 验证活动存在
        activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        Page<com.example.pexitong2.entity.pe.AttendanceRecord> recordPage = 
            attendanceRecordRepository.findByActivityIdWithFilters(
                activityId, studentId, isCheckedOut, allowedSchool, allowedCollege, pageable);
        
        List<AttendanceRecordResponse> responses = recordPage.getContent().stream()
            .map(AttendanceRecordResponse::new)
            .collect(Collectors.toList());
        
        return new PageResponse<>(responses, recordPage.getTotalElements(), page, pageSize);
    }
    
    /**
     * 获取活动统计数据
     */
    public List<StatisticsResponse.ChartItem> getActivityStatisticsByCategory(
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
        
        return statistics.stream()
            .map(row -> new StatisticsResponse.ChartItem(
                (String) row[0],     // category
                ((Number) row[1]).longValue(),  // count
                ((Number) row[2]).longValue()   // participants
            ))
            .collect(Collectors.toList());
    }
}
