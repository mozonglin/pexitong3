package com.example.pexitong2.dto;

public class SetRoleRequest {
    private String userType;
    private String reason;
    
    // 构造函数
    public SetRoleRequest() {}
    
    // Getters and Setters
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
} 