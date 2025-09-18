package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.service.pe.PeStatisticsService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * PE统计管理控制器
 * 仅开放给校级管理员和院级管理员
 */
@RestController
@RequestMapping("/pe/admin/statistics")
@CrossOrigin(origins = "*")
public class PeStatisticsController {
    
    @Autowired
    private PeStatisticsService peStatisticsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 校级管理员设置PE积分指标
     */
    @PostMapping("/targets")
    public PeApiResponse<String> setSchoolPeTargets(
            @RequestBody PeTargetRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            peStatisticsService.setSchoolPeTargets(currentUserId, request);
            
            return PeApiResponse.success("PE积分指标设置成功", null);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取校级管理员统计数据
     * 包括：学校总体达标率、各院系达标率、院系排名、学生总数、各院人数
     */
    @GetMapping("/school")
    public PeApiResponse<SchoolStatisticsResponse> getSchoolStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            SchoolStatisticsResponse statistics = peStatisticsService.getSchoolStatistics(currentUserId);
            
            return PeApiResponse.success("获取成功", statistics);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取院级管理员统计数据
     * 包括：本院整体达标率、各班达标率、班级排名
     */
    @GetMapping("/college")
    public PeApiResponse<CollegeStatisticsResponse> getCollegeStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            CollegeStatisticsResponse statistics = peStatisticsService.getCollegeStatistics(currentUserId);
            
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


