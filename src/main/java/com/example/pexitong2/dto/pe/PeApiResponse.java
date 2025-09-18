package com.example.pexitong2.dto.pe;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PeApiResponse<T> {
    
    private int code;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime timestamp;
    
    public PeApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public PeApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    public static <T> PeApiResponse<T> success(T data) {
        return new PeApiResponse<>(200, "操作成功", data);
    }
    
    public static <T> PeApiResponse<T> success(String message, T data) {
        return new PeApiResponse<>(200, message, data);
    }
    
    public static <T> PeApiResponse<T> error(int code, String message) {
        return new PeApiResponse<>(code, message, null);
    }
    
    public static <T> PeApiResponse<T> error(String message) {
        return new PeApiResponse<>(500, message, null);
    }
    
    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}




