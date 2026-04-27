package com.example.pexitong2.controller;

import com.example.pexitong2.dto.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.AuthService;
import com.example.pexitong2.service.VerificationCodeService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发送验证码
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            verificationCodeService.sendVerificationCode(request.getPhone(), request.getType());
            return ResponseEntity.ok(ApiResponse.success("验证码发送成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> result = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("注册成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        try {
            // 在实际项目中，这里可以将token加入黑名单
            // 目前仅返回成功响应
            return ResponseEntity.ok(ApiResponse.success("登出成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            
            String username = jwtUtil.extractUsername(token);
            String userId = jwtUtil.extractUserId(token);
            String userType = jwtUtil.extractUserType(token);
            
            // 验证当前token是否有效
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.badRequest().body(ApiResponse.error(401, "Token无效"));
            }
            
            // 生成新token
            String newToken = jwtUtil.generateToken(username, userId, userType);
            
            Map<String, Object> result = Map.of(
                "token", newToken,
                "expiresAt", jwtUtil.getExpirationDateTime(newToken).toString()
            );
            
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/schools")
    public ResponseEntity<ApiResponse<List<String>>> getSchools() {
        try {
            List<String> schools = userRepository.findDistinctSchools();
            return ResponseEntity.ok(ApiResponse.success("获取学校列表成功", schools));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/users-by-school")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getUsersBySchool(@RequestParam String school) {
        try {
            List<User> users = userRepository.findNonStudentUsersBySchool(school);
            List<Map<String, String>> result = users.stream().map(u -> {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("username", u.getUsername());
                m.put("realName", u.getRealName());
                m.put("userType", u.getUserType().name());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/impersonate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> impersonate(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String callerType = jwtUtil.extractUserType(token);

            if (!"super_admin".equals(callerType)) {
                return ResponseEntity.status(403).body(ApiResponse.error(403, "仅超级管理员可执行此操作"));
            }

            String targetUserId = request.get("targetUserId");
            User target = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("目标用户不存在"));

            String impersonateToken = jwtUtil.generateToken(
                    target.getUsername(), target.getId(), target.getUserType().name());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", target.getId());
            userInfo.put("username", target.getUsername());
            userInfo.put("realName", target.getRealName());
            userInfo.put("userType", target.getUserType().name());
            userInfo.put("school", target.getSchool());
            userInfo.put("departmentName", target.getDepartmentName());

            Map<String, Object> result = new HashMap<>();
            result.put("token", impersonateToken);
            result.put("user", userInfo);
            result.put("expiresAt", jwtUtil.getExpirationDateTime(impersonateToken).toString());

            return ResponseEntity.ok(ApiResponse.success("角色切换成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
} 