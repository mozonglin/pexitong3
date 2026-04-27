package com.example.pexitong2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;
    
    @Column(name = "student_id", unique = true, nullable = false, length = 30)
    private String studentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;
    
    @Column(nullable = false, length = 100)
    private String school;
    
    // departmentId字段已移除，使用departmentName即可
    
    @Column(name = "department_name", length = 100)
    private String departmentName;
    
    @Column(length = 20)
    private String phone;
    
    @Column
    private String avatar;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.active;
    
    @Column(name = "is_first_login", nullable = false)
    private Boolean isFirstLogin = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "login_count", nullable = false)
    private Integer loginCount = 0;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    // PE统计管理字段（校级管理员设置用）
    @Column(name = "weekly_target")
    private Integer weeklyTarget;
    
    @Column(name = "monthly_target")
    private Integer monthlyTarget;
    
    @Column(name = "total_target")
    private Integer totalTarget;
    
    // 构造函数
    public User() {}
    
    public User(String id, String username, String passwordHash, String realName, 
                String studentId, UserType userType, String school) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.realName = realName;
        this.studentId = studentId;
        this.userType = userType;
        this.school = school;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
    
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    // departmentId getter/setter已移除
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public Boolean getIsFirstLogin() { return isFirstLogin; }
    public void setIsFirstLogin(Boolean isFirstLogin) { this.isFirstLogin = isFirstLogin; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public Integer getLoginCount() { return loginCount; }
    public void setLoginCount(Integer loginCount) { this.loginCount = loginCount; }
    
    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    
    public Integer getWeeklyTarget() { return weeklyTarget; }
    public void setWeeklyTarget(Integer weeklyTarget) { this.weeklyTarget = weeklyTarget; }
    
    public Integer getMonthlyTarget() { return monthlyTarget; }
    public void setMonthlyTarget(Integer monthlyTarget) { this.monthlyTarget = monthlyTarget; }
    
    public Integer getTotalTarget() { return totalTarget; }
    public void setTotalTarget(Integer totalTarget) { this.totalTarget = totalTarget; }
    
    // 枚举定义
    public enum UserType {
        student, teacher, department_admin, school_admin, super_admin, counselor
    }
    
    public enum UserStatus {
        active, suspended, banned
    }
} 