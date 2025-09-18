package com.example.pexitong2.service;

import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 根据用户ID获取用户实体
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * 获取当前用户档案
     */
    public Map<String, Object> getCurrentUserProfile(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("studentId", user.getStudentId());
        userInfo.put("userType", user.getUserType().name());
        userInfo.put("school", user.getSchool());
        userInfo.put("departmentId", null); // 不使用departmentId
        userInfo.put("departmentName", user.getDepartmentName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus().name());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        userInfo.put("loginCount", user.getLoginCount());
        userInfo.put("isFirstLogin", user.getIsFirstLogin());
        
        // 模拟统计数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("listeningCount", 25);
        statistics.put("uploadCount", 8);
        statistics.put("feedbackCount", 12);
        statistics.put("averageScore", 8.7);
        userInfo.put("statistics", statistics);
        
        return userInfo;
    }
    
    /**
     * 更新用户资料
     */
    public Map<String, Object> updateProfile(String userId, Map<String, Object> updateData) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 只允许更新部分字段
        if (updateData.containsKey("phone")) {
            String newPhone = (String) updateData.get("phone");
            if (userRepository.existsByPhone(newPhone) && !newPhone.equals(user.getPhone())) {
                throw new RuntimeException("该手机号已被使用");
            }
            user.setPhone(newPhone);
        }
        
        if (updateData.containsKey("avatar")) {
            user.setAvatar((String) updateData.get("avatar"));
        }
        
        userRepository.save(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        
        return result;
    }
    
    /**
     * 修改密码
     */
    public void changePassword(String userId, Map<String, String> passwordData) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        String confirmPassword = passwordData.get("confirmPassword");
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 验证新密码
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        if (newPassword.length() < 8) {
            throw new RuntimeException("密码长度至少8位");
        }
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setIsFirstLogin(false);
        
        userRepository.save(user);
    }
} 