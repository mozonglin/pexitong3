package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.service.pe.MorningExerciseService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 早操管理控制器
 */
@RestController
@RequestMapping("/pe/morning-exercises")
@CrossOrigin(origins = "*")
public class MorningExerciseController {
    
    @Autowired
    private MorningExerciseService morningExerciseService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取早操活动列表
     */
    @GetMapping
    public PeApiResponse<PageResponse<MorningExerciseResponse>> getMorningExercises(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<MorningExerciseResponse> exercises = morningExerciseService.getMorningExercises(
                page, pageSize, date, isActive, startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", exercises);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个早操活动详情
     */
    @GetMapping("/{id}")
    public PeApiResponse<MorningExerciseResponse> getMorningExerciseById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            MorningExerciseResponse exercise = morningExerciseService.getMorningExerciseById(id, currentUserId);
            
            return PeApiResponse.success("获取成功", exercise);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 创建早操活动
     */
    @PostMapping
    public PeApiResponse<MorningExerciseResponse> createMorningExercise(
            @RequestBody MorningExerciseRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            MorningExerciseResponse exercise = morningExerciseService.createMorningExercise(request, currentUserId);
            
            return PeApiResponse.success("早操活动创建成功", exercise);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新早操活动
     */
    @PutMapping("/{id}")
    public PeApiResponse<MorningExerciseResponse> updateMorningExercise(
            @PathVariable String id,
            @RequestBody MorningExerciseRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            MorningExerciseResponse exercise = morningExerciseService.updateMorningExercise(id, request, currentUserId);
            
            return PeApiResponse.success("早操活动更新成功", exercise);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除早操活动
     */
    @DeleteMapping("/{id}")
    public PeApiResponse<Void> deleteMorningExercise(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            morningExerciseService.deleteMorningExercise(id, currentUserId);
            
            return PeApiResponse.success("早操活动删除成功", null);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取早操考勤记录
     */
    @GetMapping("/{id}/attendance")
    public PeApiResponse<PageResponse<MorningExerciseAttendanceResponse>> getMorningExerciseAttendance(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Boolean isCheckedOut,
            @RequestParam(required = false) String checkedBy,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<MorningExerciseAttendanceResponse> attendance = 
                morningExerciseService.getMorningExerciseAttendance(
                    id, page, pageSize, studentId, isCheckedOut, checkedBy, currentUserId);
            
            return PeApiResponse.success("获取成功", attendance);
            
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
