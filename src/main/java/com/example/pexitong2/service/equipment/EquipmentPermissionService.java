package com.example.pexitong2.service.equipment;

import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EquipmentPermissionService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 检查用户是否有器材管理权限
     * 院级管理员、校级管理员和超级管理员有权限
     */
    public boolean hasEquipmentManagementPermission(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        User.UserType userType = user.getUserType();
        
        return userType == User.UserType.department_admin
                || userType == User.UserType.school_admin
                || userType == User.UserType.super_admin;
    }
    
    /**
     * 检查用户是否有删除器材的权限
     * 只有超级管理员有删除权限
     */
    public boolean hasDeleteEquipmentPermission(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        return user.getUserType() == User.UserType.super_admin;
    }
    
    /**
     * 验证权限，如果无权限则抛出异常
     */
    public void validateEquipmentManagementPermission(String userId) {
        if (!hasEquipmentManagementPermission(userId)) {
            throw new RuntimeException("权限不足，只有院级及以上管理员可以管理器材");
        }
    }
    
    /**
     * 验证删除权限，如果无权限则抛出异常
     */
    public void validateDeleteEquipmentPermission(String userId) {
        if (!hasDeleteEquipmentPermission(userId)) {
            throw new RuntimeException("权限不足，只有超级管理员可以删除器材");
        }
    }
}





