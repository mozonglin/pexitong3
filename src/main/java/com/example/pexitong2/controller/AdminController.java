package com.example.pexitong2.controller;

import com.example.pexitong2.dto.*;
import com.example.pexitong2.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> getUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            Authentication authentication) {
        
        try {
            // 从认证对象中获取当前用户ID
            String currentUserId = (String) authentication.getDetails();
            
            Map<String, Object> result = adminService.getUsers(
                userType, school, keyword, status, startDate, endDate,
                page, limit, sortBy, sortOrder, currentUserId);
            return ApiResponse.success("获取成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 设置用户角色
     */
    @PutMapping("/users/{userId}/role")
    public ApiResponse<Map<String, Object>> setUserRole(
            @PathVariable String userId,
            @RequestBody SetRoleRequest request,
            Authentication authentication) {
        
        try {
            // 从认证对象中获取当前用户ID
            String currentUserId = (String) authentication.getDetails();
            Map<String, Object> result = adminService.setUserRole(userId, request, currentUserId);
            return ApiResponse.success("用户角色设置成功", result);
        } catch (Exception e) {
            if (e.getMessage().contains("权限不足")) {
                return ApiResponse.error(403, e.getMessage());
            }
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/users/{userId}/status")
    public ApiResponse<Map<String, Object>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody UpdateUserStatusRequest request,
            Authentication authentication) {
        
        try {
            String currentUserId = (String) authentication.getDetails();
            Map<String, Object> result = adminService.updateUserStatus(userId, request, currentUserId);
            return ApiResponse.success("用户状态更新成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/users/{userId}/reset-password")
    public ApiResponse<Map<String, Object>> resetUserPassword(
            @PathVariable String userId,
            @RequestBody ResetPasswordRequest request) {
        
        try {
            Map<String, Object> result = adminService.resetUserPassword(userId, request);
            return ApiResponse.success("密码重置成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取管理员统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getAdminStats() {
        try {
            Map<String, Object> stats = adminService.getAdminStats();
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 