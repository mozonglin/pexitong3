package com.example.pexitong2.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Embeddable
public class FileInfo {
    
    private String name;
    
    @Column(length = 500)
    private String path;
    
    @Column(name = "file_type", length = 100)
    private String type;
    
    @Column(name = "file_size")
    private Long size;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    // 构造函数
    public FileInfo() {}
    
    public FileInfo(String name, String path, String type, Long size, LocalDateTime uploadedAt) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    /**
     * 生成文件下载URL
     */
    public String getUrl() {
        if (path != null) {
            return "/api/files/download/" + path;
        }
        return null;
    }
} 