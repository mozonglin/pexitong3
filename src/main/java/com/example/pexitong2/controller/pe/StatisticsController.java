package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.service.pe.StatisticsService;
import com.example.pexitong2.service.pe.PeUserService;
import com.example.pexitong2.service.pe.MorningExerciseService;
import com.example.pexitong2.service.pe.AttendanceRecordService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/pe/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private PeUserService userService;
    
    @Autowired
    private MorningExerciseService morningExerciseService;
    
    @Autowired
    private AttendanceRecordService attendanceRecordService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取总体统计
     */
    @GetMapping
    public PeApiResponse<StatisticsResponse> getOverallStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String college,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            StatisticsResponse statistics = statisticsService.getOverallStatistics(
                startDate, endDate, school, college, currentUserId);
            
            return PeApiResponse.success("获取成功", statistics);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取活动统计（按类别）
     */
    @GetMapping("/activities")
    public PeApiResponse<StatisticsResponse.ActivityChartData> getActivityStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "category") String groupBy,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            // 目前只支持按类别分组
            StatisticsResponse.ActivityChartData chartData = 
                statisticsService.getActivityStatisticsByCategory(startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", chartData);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户统计
     */
    @GetMapping("/users")
    public PeApiResponse<StatisticsResponse.UserStatistics> getUserStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            StatisticsResponse.UserStatistics userStats = userService.getUserStatistics(currentUserId);
            
            return PeApiResponse.success("获取成功", userStats);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取早操统计
     */
    @GetMapping("/morning-exercises")
    public PeApiResponse<StatisticsResponse.MorningExerciseStatistics> getMorningExerciseStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            StatisticsResponse.MorningExerciseStatistics morningStats = 
                morningExerciseService.getMorningExerciseStatistics(startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", morningStats);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取早操月度统计
     */
    @GetMapping("/morning-exercises/monthly")
    public PeApiResponse<List<Object[]>> getMorningExerciseMonthlyStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            List<Object[]> monthlyStats = statisticsService.getMorningExerciseMonthlyStatistics(
                startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", monthlyStats);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取签到统计
     */
    @GetMapping("/attendance")
    public PeApiResponse<StatisticsResponse.AttendanceStatistics> getAttendanceStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            StatisticsResponse.AttendanceStatistics attendanceStats = 
                attendanceRecordService.getAttendanceStatistics(startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", attendanceStats);
            
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
