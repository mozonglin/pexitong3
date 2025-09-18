package com.example.pexitong2.dto;

public class UpdateUserStatusRequest {
    private String status;
    private String reason;
    private Integer duration;
    
    // 构造函数
    public UpdateUserStatusRequest() {}
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
} 