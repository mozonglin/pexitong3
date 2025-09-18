package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile(Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            Map<String, Object> userInfo = userService.getCurrentUserProfile(userId);
            return ApiResponse.success("获取成功", userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> updateData,
            Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            Map<String, Object> result = userService.updateProfile(userId, updateData);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ApiResponse<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> passwordData,
            Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            userService.changePassword(userId, passwordData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isFirstLogin", false);
            result.put("passwordChangedAt", java.time.LocalDateTime.now());
            
            return ApiResponse.success("密码修改成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 