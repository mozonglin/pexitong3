package com.example.pexitong2.service;

import com.example.pexitong2.dto.ResetPasswordRequest;
import com.example.pexitong2.dto.SetRoleRequest;
import com.example.pexitong2.dto.UpdateUserStatusRequest;
import com.example.pexitong2.entity.RoleChangeLog;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.RoleChangeLogRepository;
import com.example.pexitong2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleChangeLogRepository roleChangeLogRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 获取用户列表
     */
    public Map<String, Object> getUsers(String userType, String school, String keyword, 
                                       String status, String startDate, String endDate,
                                       int page, int limit, String sortBy, String sortOrder,
                                       String currentUserId) {
        
        // 获取当前管理员信息
        User currentAdmin = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        // 根据管理员级别进行权限过滤
        String filteredSchool = school;
        String filteredDepartment = null;
        
        if (currentAdmin.getUserType() == User.UserType.school_admin) {
            // 校级管理员只能查看所属学校的用户
            filteredSchool = currentAdmin.getSchool();
        } else if (currentAdmin.getUserType() == User.UserType.department_admin) {
            // 院级管理员只能查看所属学校所属院系的用户
            filteredSchool = currentAdmin.getSchool();
            filteredDepartment = currentAdmin.getDepartmentName();
        }
        // 超级管理员不需要过滤，保持原有参数
        
        // 构建分页和排序
        Sort sort = "desc".equals(sortOrder) ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        
        // 转换枚举
        User.UserType userTypeEnum = userType != null ? User.UserType.valueOf(userType) : null;
        User.UserStatus statusEnum = status != null ? User.UserStatus.valueOf(status) : null;
        
        // 查询用户
        Page<User> userPage;
        if (filteredDepartment != null) {
            // 院级管理员：按学校和院系过滤
            userPage = userRepository.findUsersWithFiltersAndDepartment(
                userTypeEnum, filteredSchool, filteredDepartment, keyword, statusEnum, pageable);
        } else {
            // 校级管理员或超级管理员：按学校过滤（超级管理员时filteredSchool可能为null）
            userPage = userRepository.findUsersWithFilters(
                userTypeEnum, filteredSchool, keyword, statusEnum, pageable);
        }
        
        // 转换为响应格式
        List<Map<String, Object>> userList = userPage.getContent().stream()
            .map(this::convertUserToMap)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", userList);
        result.put("total", userPage.getTotalElements());
        result.put("page", page);
        result.put("limit", limit);
        result.put("totalPages", userPage.getTotalPages());
        
        // 添加权限信息到响应中
        result.put("adminLevel", currentAdmin.getUserType().name());
        result.put("adminSchool", currentAdmin.getSchool());
        if (currentAdmin.getUserType() == User.UserType.department_admin) {
            result.put("adminDepartment", currentAdmin.getDepartmentName());
        }
        
        return result;
    }
    
    /**
     * 设置用户角色
     */
    public Map<String, Object> setUserRole(String userId, SetRoleRequest request, String currentUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        // 验证权限
        validateRoleChangePermission(currentUser, user, User.UserType.valueOf(request.getUserType()));
        
        User.UserType oldRole = user.getUserType();
        User.UserType newRole = User.UserType.valueOf(request.getUserType());
        
        // 更新用户角色
        user.setUserType(newRole);
        userRepository.save(user);
        
        // 记录角色变更
        RoleChangeLog log = new RoleChangeLog(userId, oldRole, newRole, currentUserId, request.getReason());
        roleChangeLogRepository.save(log);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("oldRole", oldRole.name());
        result.put("newRole", newRole.name());
        result.put("changedBy", currentUserId);
        result.put("changedAt", LocalDateTime.now());
        result.put("reason", request.getReason());
        
        return result;
    }
    
    /**
     * 更新用户状态
     */
    public Map<String, Object> updateUserStatus(String userId, UpdateUserStatusRequest request, String currentUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        User.UserStatus oldStatus = user.getStatus();
        User.UserStatus newStatus = User.UserStatus.valueOf(request.getStatus());
        
        user.setStatus(newStatus);
        userRepository.save(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("oldStatus", oldStatus.name());
        result.put("newStatus", newStatus.name());
        result.put("reason", request.getReason());
        result.put("changedBy", currentUserId);
        
        if ("suspended".equals(request.getStatus()) && request.getDuration() != null) {
            result.put("effectiveUntil", LocalDateTime.now().plusDays(request.getDuration()));
        }
        
        return result;
    }
    
    /**
     * 重置用户密码
     */
    public Map<String, Object> resetUserPassword(String userId, ResetPasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String tempPassword = request.getTemporaryPassword();
        if (tempPassword == null || tempPassword.isEmpty()) {
            tempPassword = generateTempPassword();
        }
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        if (Boolean.TRUE.equals(request.getForceChange())) {
            user.setIsFirstLogin(true);
        }
        userRepository.save(user);
        
        // 模拟发送通知
        if (Boolean.TRUE.equals(request.getNotifyUser())) {
            String message = String.format("您的密码已重置，临时密码: %s，请及时登录修改", tempPassword);
            System.out.println("发送通知到 " + user.getPhone() + ": " + message);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("temporaryPassword", tempPassword);
        result.put("mustChangeOnNextLogin", request.getForceChange());
        result.put("notificationSent", request.getNotifyUser());
        result.put("notifyMethod", request.getNotifyMethod());
        
        return result;
    }
    
    /**
     * 获取管理员统计
     */
    public Map<String, Object> getAdminStats() {
        Map<String, Object> userCounts = new HashMap<>();
        for (User.UserType type : User.UserType.values()) {
            long count = userRepository.count(); // 简化实现
            userCounts.put(type.name(), count);
        }
        
        Map<String, Object> onlineStats = new HashMap<>();
        onlineStats.put("totalOnline", 156);
        onlineStats.put("userTypeStats", Map.of(
            "student", 120,
            "teacher", 25,
            "department_admin", 8,
            "school_admin", 2,
            "super_admin", 1
        ));
        
        Map<String, Object> recentActivity = new HashMap<>();
        recentActivity.put("newUsers", 15);
        recentActivity.put("activeUsers", 340);
        recentActivity.put("loginToday", 280);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userCounts", userCounts);
        result.put("onlineStats", onlineStats);
        result.put("recentActivity", recentActivity);
        
        return result;
    }
    
    /**
     * 验证角色变更权限
     */
    private void validateRoleChangePermission(User currentUser, User targetUser, User.UserType newRole) {
        User.UserType currentRole = currentUser.getUserType();
        
        // 超级管理员可以设置任何角色
        if (currentRole == User.UserType.super_admin) {
            return;
        }
        
        // 校级管理员只能设置教师为院级管理员，或将院级管理员降级为教师
        if (currentRole == User.UserType.school_admin) {
            if (targetUser.getUserType() == User.UserType.teacher && newRole == User.UserType.department_admin) {
                return; // 允许：教师 -> 院级管理员
            }
            if (targetUser.getUserType() == User.UserType.department_admin && newRole == User.UserType.teacher) {
                return; // 允许：院级管理员 -> 教师
            }
        }
        
        throw new RuntimeException("权限不足，无法设置该角色");
    }
    
    /**
     * 转换用户为Map格式
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("realName", user.getRealName());
        userMap.put("studentId", user.getStudentId());
        userMap.put("userType", user.getUserType().name());
        userMap.put("school", user.getSchool());
        userMap.put("departmentName", user.getDepartmentName());
        userMap.put("phone", user.getPhone());
        userMap.put("status", user.getStatus().name());
        userMap.put("lastLoginAt", user.getLastLoginAt());
        userMap.put("loginCount", user.getLoginCount());
        userMap.put("createdAt", user.getCreatedAt());
        return userMap;
    }
    
    /**
     * 生成临时密码
     */
    private String generateTempPassword() {
        return "TempPass" + (new Random().nextInt(900) + 100);
    }
} 