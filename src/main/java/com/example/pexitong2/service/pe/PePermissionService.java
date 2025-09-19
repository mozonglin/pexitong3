package com.example.pexitong2.service.pe;

import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * PE管理权限控制服务
 * 根据用户在原登录系统中的身份，为PE管理端提供权限控制
 */
@Service
public class PePermissionService {
    
    @Autowired
    private UserRepository userRepository; // 使用原有用户系统的用户表
    
    /**
     * 验证用户是否有PE管理权限
     */
    public boolean hasPeManagementPermission(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        // 管理员级别的用户可以访问PE管理端
        return user.getUserType() == User.UserType.school_admin || 
               user.getUserType() == User.UserType.super_admin ||
               user.getUserType() == User.UserType.department_admin;
    }
    
    /**
     * 获取用户可以管理的学校范围
     * @param userId 用户ID
     * @return 学校名称，null表示可以管理所有学校
     */
    public String getAllowedSchool(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!hasPeManagementPermission(userId)) {
            throw new RuntimeException("无PE管理权限");
        }
        
        if (user.getUserType() == User.UserType.super_admin) {
            return null; // 超级管理员可以管理所有学校
        } else if (user.getUserType() == User.UserType.school_admin || 
                   user.getUserType() == User.UserType.department_admin) {
            return user.getSchool(); // 校级管理员和院级管理员只能管理本校
        }
        
        throw new RuntimeException("权限不足");
    }
    
    /**
     * 获取用户可以管理的学院范围
     * @param userId 用户ID
     * @return 学院名称，null表示可以管理该学校的所有学院
     */
    public String getAllowedCollege(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!hasPeManagementPermission(userId)) {
            throw new RuntimeException("无PE管理权限");
        }
        
        if (user.getUserType() == User.UserType.super_admin || 
            user.getUserType() == User.UserType.school_admin) {
            return null; // 超级管理员和校级管理员可以管理该校所有学院
        } else if (user.getUserType() == User.UserType.department_admin) {
            return user.getDepartmentName(); // 院级管理员只能管理本学院
        }
        
        throw new RuntimeException("权限不足");
    }
    
    /**
     * 验证用户是否可以管理指定学校的数据
     */
    public boolean canManageSchool(String userId, String targetSchool) {
        String allowedSchool = getAllowedSchool(userId);
        
        if (allowedSchool == null) {
            return true; // 超级管理员可以管理所有学校
        }
        
        return allowedSchool.equals(targetSchool);
    }
    
    /**
     * 验证用户是否可以管理指定学院的数据
     */
    public boolean canManageCollege(String userId, String targetSchool, String targetCollege) {
        if (!canManageSchool(userId, targetSchool)) {
            return false;
        }
        
        String allowedCollege = getAllowedCollege(userId);
        
        if (allowedCollege == null) {
            return true; // 可以管理该校所有学院
        }
        
        return allowedCollege.equals(targetCollege);
    }
    
    /**
     * 获取用户信息（供权限控制使用）
     */
    public User getUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    /**
     * 验证并抛出权限异常
     */
    public void validatePeManagementPermission(String userId) {
        if (!hasPeManagementPermission(userId)) {
            throw new RuntimeException("权限不足：需要管理员权限才能访问PE管理功能");
        }
    }
    
    /**
     * 验证用户是否有早操发布权限
     * 只有院级管理员可以发布早操活动
     */
    public boolean hasMorningExercisePublishPermission(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        // 只有院级管理员可以发布早操活动
        return user.getUserType() == User.UserType.department_admin;
    }
    
    /**
     * 验证早操发布权限并抛出异常
     */
    public void validateMorningExercisePublishPermission(String userId) {
        if (!hasMorningExercisePublishPermission(userId)) {
            throw new RuntimeException("权限不足：只有院级管理员可以发布早操活动");
        }
    }
    
    /**
     * 验证院级管理员是否可以修改指定早操活动
     * （只能修改同学院管理员创建的早操活动）
     */
    public void validateMorningExerciseModifyPermission(String userId, String createdBy) {
        // 验证发布权限
        validateMorningExercisePublishPermission(userId);
        
        // 获取当前用户和创建者的学院信息
        User currentUser = getUser(userId);
        User creator = getUser(createdBy);
        
        // 检查是否同学院
        if (!currentUser.getSchool().equals(creator.getSchool()) || 
            !currentUser.getDepartmentName().equals(creator.getDepartmentName())) {
            throw new RuntimeException("权限不足：只能修改本学院管理员创建的早操活动");
        }
    }
}




