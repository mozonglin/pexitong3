package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 测试JWT生成
     */
    @GetMapping("/jwt")
    public ApiResponse<Map<String, Object>> testJwt() {
        String token = jwtUtil.generateToken("testuser", "test123", "student");
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", jwtUtil.extractUsername(token));
        result.put("userId", jwtUtil.extractUserId(token));
        result.put("userType", jwtUtil.extractUserType(token));
        result.put("expiresAt", jwtUtil.getExpirationDateTime(token));
        
        return ApiResponse.success("JWT测试成功", result);
    }
    
    /**
     * 测试密码加密
     */
    @GetMapping("/password")
    public ApiResponse<Map<String, Object>> testPassword() {
        String rawPassword = "testpassword123";
        String encoded = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encoded);
        
        Map<String, Object> result = new HashMap<>();
        result.put("rawPassword", rawPassword);
        result.put("encodedPassword", encoded);
        result.put("matches", matches);
        
        return ApiResponse.success("密码加密测试成功", result);
    }
    
    /**
     * 测试认证信息
     */
    @GetMapping("/auth")
    public ApiResponse<Map<String, Object>> testAuth(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication != null) {
            result.put("username", authentication.getName());
            result.put("authorities", authentication.getAuthorities());
            result.put("details", authentication.getDetails());
            result.put("authenticated", authentication.isAuthenticated());
        } else {
            result.put("message", "未认证用户");
        }
        
        return ApiResponse.success("认证测试", result);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "系统运行正常");
        
        return ApiResponse.success("健康检查", result);
    }
} 