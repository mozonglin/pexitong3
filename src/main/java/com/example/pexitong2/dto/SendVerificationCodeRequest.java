package com.example.pexitong2.dto;

public class SendVerificationCodeRequest {
    private String phone;
    private String type;
    
    // 构造函数
    public SendVerificationCodeRequest() {}
    
    // Getters and Setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
} 