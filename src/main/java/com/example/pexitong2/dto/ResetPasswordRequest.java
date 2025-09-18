package com.example.pexitong2.dto;

public class ResetPasswordRequest {
    private String temporaryPassword;
    private Boolean forceChange;
    private Boolean notifyUser;
    private String notifyMethod;
    
    // 构造函数
    public ResetPasswordRequest() {}
    
    // Getters and Setters
    public String getTemporaryPassword() { return temporaryPassword; }
    public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }
    
    public Boolean getForceChange() { return forceChange; }
    public void setForceChange(Boolean forceChange) { this.forceChange = forceChange; }
    
    public Boolean getNotifyUser() { return notifyUser; }
    public void setNotifyUser(Boolean notifyUser) { this.notifyUser = notifyUser; }
    
    public String getNotifyMethod() { return notifyMethod; }
    public void setNotifyMethod(String notifyMethod) { this.notifyMethod = notifyMethod; }
} 