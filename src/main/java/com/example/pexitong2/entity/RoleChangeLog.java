package com.example.pexitong2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_change_logs")
public class RoleChangeLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "old_role")
    private User.UserType oldRole;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "new_role", nullable = false)
    private User.UserType newRole;
    
    @Column(name = "changed_by", nullable = false, length = 50)
    private String changedBy;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    // 构造函数
    public RoleChangeLog() {}
    
    public RoleChangeLog(String userId, User.UserType oldRole, User.UserType newRole, 
                        String changedBy, String reason) {
        this.userId = userId;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.changedBy = changedBy;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public User.UserType getOldRole() { return oldRole; }
    public void setOldRole(User.UserType oldRole) { this.oldRole = oldRole; }
    
    public User.UserType getNewRole() { return newRole; }
    public void setNewRole(User.UserType newRole) { this.newRole = newRole; }
    
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
} 