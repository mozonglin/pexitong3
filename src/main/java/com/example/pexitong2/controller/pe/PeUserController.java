package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.service.pe.PeUserService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * PE用户管理控制器
 */
@RestController
@RequestMapping("/pe/users")
@CrossOrigin(origins = "*")
public class PeUserController {
    
    @Autowired
    private PeUserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取用户列表
     */
    @GetMapping
    public PeApiResponse<PageResponse<PeUserResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String college,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<PeUserResponse> users = userService.getUsers(
                page, pageSize, search, role, school, college, currentUserId);
            
            return PeApiResponse.success("获取成功", users);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个用户详情
     */
    @GetMapping("/{id}")
    public PeApiResponse<PeUserResponse> getUserById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PeUserResponse user = userService.getUserById(id, currentUserId);
            
            return PeApiResponse.success("获取成功", user);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 设置用户角色
     */
    @PutMapping("/{id}/role")
    public PeApiResponse<Void> setUserRole(
            @PathVariable String id,
            @RequestBody SetUserRoleRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            userService.setUserRole(id, request, currentUserId);
            
            return PeApiResponse.success("角色设置成功", null);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户积分历史
     */
    @GetMapping("/{id}/points-history")
    public PeApiResponse<PageResponse<PointsRecordResponse>> getUserPointsHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String activityType,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<PointsRecordResponse> pointsHistory = userService.getUserPointsHistory(
                id, page, pageSize, startDate, endDate, activityType, currentUserId);
            
            return PeApiResponse.success("获取成功", pointsHistory);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 强制登出（重置登录状态）
     */
    @PostMapping("/{id}/force-logout")
    public PeApiResponse<Void> forceLogout(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String currentUserId = getCurrentUserId(token);
            userService.forceLogout(id, currentUserId);
            return PeApiResponse.success("已强制登出", null);
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    /**
     * 修改手机号
     */
    @PutMapping("/{id}/phone")
    public PeApiResponse<Void> updatePhone(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> body,
            @RequestHeader("Authorization") String token) {
        try {
            String currentUserId = getCurrentUserId(token);
            String newPhone = body.get("phoneNumber");
            if (newPhone == null || newPhone.isBlank()) {
                return PeApiResponse.error("手机号不能为空");
            }
            userService.updatePhone(id, newPhone, currentUserId);
            return PeApiResponse.success("手机号修改成功", null);
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
