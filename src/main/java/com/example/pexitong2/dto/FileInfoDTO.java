package com.example.pexitong2.dto;

import com.example.pexitong2.entity.FileInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class FileInfoDTO {
    
    private String name;
    private String path;
    private String url;
    private String type;
    private Long size;
    
    @JsonProperty("uploaded_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime uploadedAt;
    
    // 构造函数
    public FileInfoDTO() {}
    
    public FileInfoDTO(String name, String path, String type, Long size, LocalDateTime uploadedAt) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
        this.uploadedAt = uploadedAt;
        if (path != null) {
            this.url = "/api/files/download/" + path;
        }
    }
    
    // 从实体转换
    public static FileInfoDTO fromEntity(FileInfo fileInfo) {
        if (fileInfo == null) {
            return null;
        }
        return new FileInfoDTO(
            fileInfo.getName(),
            fileInfo.getPath(),
            fileInfo.getType(),
            fileInfo.getSize(),
            fileInfo.getUploadedAt()
        );
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
        if (path != null) {
            this.url = "/api/files/download/" + path;
        }
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
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
} 