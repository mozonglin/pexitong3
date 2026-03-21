package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.entity.pe.PeUser;
import com.example.pexitong2.repository.pe.PeUserRepository;
import com.example.pexitong2.repository.pe.PointsRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PE用户管理服务
 */
@Service
public class PeUserService {
    
    @Autowired
    private PeUserRepository peUserRepository;
    
    @Autowired
    private PointsRecordRepository pointsRecordRepository;
    
    @Autowired
    private PePermissionService permissionService;
    
    /**
     * 获取用户列表
     */
    public PageResponse<PeUserResponse> getUsers(
            int page, int pageSize, String search, String role, 
            String school, String college, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        // 应用权限过滤
        String filteredSchool = allowedSchool != null ? allowedSchool : school;
        String filteredCollege = allowedCollege != null ? allowedCollege : college;
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        // 解析角色参数，增加错误处理
        PeUser.Role roleEnum = null;
        if (role != null) {
            try {
                roleEnum = PeUser.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的角色类型: " + role);
            }
        }
        
        Page<PeUser> userPage = peUserRepository.findUsersWithFilters(
            search, roleEnum, filteredSchool, filteredCollege, pageable);
        
        List<PeUserResponse> responses = userPage.getContent().stream()
            .map(PeUserResponse::new)
            .collect(Collectors.toList());
        
        return new PageResponse<>(responses, userPage.getTotalElements(), page, pageSize);
    }
    
    /**
     * 获取单个用户详情
     */
    public PeUserResponse getUserById(String id, String currentUserId) {
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        PeUser user = peUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证是否在权限范围内
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        if (allowedSchool != null && !allowedSchool.equals(user.getSchool())) {
            throw new RuntimeException("权限不足：无法查看该用户信息");
        }
        
        if (allowedCollege != null && !allowedCollege.equals(user.getCollege())) {
            throw new RuntimeException("权限不足：无法查看该用户信息");
        }
        
        return new PeUserResponse(user);
    }
    
    /**
     * 设置用户角色
     */
    @Transactional
    public void setUserRole(String userId, SetUserRoleRequest request, String currentUserId) {
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        PeUser user = peUserRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证是否在权限范围内
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        if (allowedSchool != null && !allowedSchool.equals(user.getSchool())) {
            throw new RuntimeException("权限不足：无法修改该用户角色");
        }
        
        if (allowedCollege != null && !allowedCollege.equals(user.getCollege())) {
            throw new RuntimeException("权限不足：无法修改该用户角色");
        }
        
        // 验证新角色
        PeUser.Role newRole;
        try {
            newRole = PeUser.Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的角色类型");
        }
        
        // 更新角色
        user.setRole(newRole);
        peUserRepository.save(user);
    }
    
    /**
     * 获取用户积分历史
     */
    public PageResponse<PointsRecordResponse> getUserPointsHistory(
            String userId, int page, int pageSize, String startDate, 
            String endDate, String activityType, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 验证用户存在
        PeUser user = peUserRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证是否在权限范围内
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        if (allowedSchool != null && !allowedSchool.equals(user.getSchool())) {
            throw new RuntimeException("权限不足：无法查看该用户积分历史");
        }
        
        if (allowedCollege != null && !allowedCollege.equals(user.getCollege())) {
            throw new RuntimeException("权限不足：无法查看该用户积分历史");
        }
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
        
        Page<com.example.pexitong2.entity.pe.PointsRecord> recordPage = 
            pointsRecordRepository.findUserPointsHistoryWithFilters(
                userId, start, end, activityType, allowedSchool, allowedCollege, pageable);
        
        List<PointsRecordResponse> responses = recordPage.getContent().stream()
            .map(PointsRecordResponse::new)
            .collect(Collectors.toList());
        
        return new PageResponse<>(responses, recordPage.getTotalElements(), page, pageSize);
    }
    
    /**
     * 强制登出（清除登录状态）
     */
    @Transactional
    public void forceLogout(String userId, String currentUserId) {
        permissionService.validatePeManagementPermission(currentUserId);
        PeUser user = peUserRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        if (allowedSchool != null && !allowedSchool.equals(user.getSchool())) {
            throw new RuntimeException("权限不足：无法操作该用户");
        }
        if (allowedCollege != null && !allowedCollege.equals(user.getCollege())) {
            throw new RuntimeException("权限不足：无法操作该用户");
        }
        user.setIsLoggedIn(false);
        peUserRepository.save(user);
    }

    /**
     * 修改手机号
     */
    @Transactional
    public void updatePhone(String userId, String newPhone, String currentUserId) {
        permissionService.validatePeManagementPermission(currentUserId);
        PeUser user = peUserRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        if (allowedSchool != null && !allowedSchool.equals(user.getSchool())) {
            throw new RuntimeException("权限不足：无法操作该用户");
        }
        if (allowedCollege != null && !allowedCollege.equals(user.getCollege())) {
            throw new RuntimeException("权限不足：无法操作该用户");
        }
        // 检查手机号是否已被其他用户使用
        peUserRepository.findByPhoneNumber(newPhone).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new RuntimeException("该手机号已被其他用户使用");
            }
        });
        user.setPhoneNumber(newPhone);
        peUserRepository.save(user);
    }

    /**
     * 获取用户统计数据
     */
    public StatisticsResponse.UserStatistics getUserStatistics(String currentUserId) {
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        long total = peUserRepository.countBySchoolAndCollege(allowedSchool, allowedCollege);
        long students = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.STUDENT, allowedSchool, allowedCollege);
        long checkers = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.CHECKER, allowedSchool, allowedCollege);
        long subCheckers = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.SUB_CHECKER, allowedSchool, allowedCollege);
        long admins = peUserRepository.countByRoleAndSchoolAndCollege(
            PeUser.Role.ADMIN, allowedSchool, allowedCollege);
        
        return new StatisticsResponse.UserStatistics(total, students, checkers, subCheckers, admins);
    }
}
