package com.example.pexitong2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.pexitong2.entity.EvaluationTemplate;

import java.time.LocalDateTime;

public class TemplateResponse {
    
    private Long id;
    private String name;
    
    @JsonProperty("file_path")
    private String filePath;
    
    @JsonProperty("file_type")
    private String fileType;
    
    @JsonProperty("file_size")
    private Long fileSize;
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("uploaded_by")
    private String uploadedBy;
    
    @JsonProperty("uploaded_at")
    private LocalDateTime uploadedAt;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public TemplateResponse() {}
    
    public TemplateResponse(EvaluationTemplate template) {
        this.id = template.getId();
        this.name = template.getName();
        this.filePath = template.getFilePath();
        this.fileType = template.getFileType();
        this.fileSize = template.getFileSize();
        this.isDefault = template.getIsDefault();
        this.uploadedBy = template.getUploadedBy();
        this.uploadedAt = template.getUploadedAt();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
    }
    
    // 静态工厂方法
    public static TemplateResponse fromEntity(EvaluationTemplate template) {
        return new TemplateResponse(template);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 