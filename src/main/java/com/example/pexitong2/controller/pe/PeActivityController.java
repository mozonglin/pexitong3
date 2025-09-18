package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.service.pe.PeActivityService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PE活动管理控制器
 */
@RestController
@RequestMapping("/pe/activities")
@CrossOrigin(origins = "*")
public class PeActivityController {
    
    @Autowired
    private PeActivityService activityService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取活动列表
     */
    @GetMapping
    public PeApiResponse<PageResponse<ActivityResponse>> getActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String organizerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<ActivityResponse> activities = activityService.getActivities(
                page, pageSize, approvalStatus, category, organizerId, 
                startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", activities);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个活动详情
     */
    @GetMapping("/{id}")
    public PeApiResponse<ActivityResponse> getActivityById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            ActivityResponse activity = activityService.getActivityById(id, currentUserId);
            
            return PeApiResponse.success("获取成功", activity);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 审核活动
     */
    @PutMapping("/{id}/review")
    public PeApiResponse<Void> reviewActivity(
            @PathVariable String id,
            @RequestBody ActivityReviewRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            activityService.reviewActivity(id, request, currentUserId);
            
            return PeApiResponse.success("审核成功", null);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取活动签到记录
     */
    @GetMapping("/{id}/attendance")
    public PeApiResponse<PageResponse<AttendanceRecordResponse>> getActivityAttendance(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Boolean isCheckedOut,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<AttendanceRecordResponse> attendance = 
                activityService.getActivityAttendance(id, page, pageSize, studentId, isCheckedOut, currentUserId);
            
            return PeApiResponse.success("获取成功", attendance);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取活动统计（按类别）
     */
    @GetMapping("/statistics")
    public PeApiResponse<List<StatisticsResponse.ChartItem>> getActivityStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            List<StatisticsResponse.ChartItem> statistics = 
                activityService.getActivityStatisticsByCategory(startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", statistics);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 从Token中获取当前用户ID
     */
    private String getCurrentUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtUtil.extractUserId(token);
    }
}
