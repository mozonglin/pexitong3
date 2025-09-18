package com.example.pexitong2.dto.teaching;

import com.example.pexitong2.entity.teaching.TeacherAttendancePhoto;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 签到照片响应DTO
 */
public class AttendancePhotoResponse {
    
    private String id;
    private String url;
    private String thumbnailUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime uploadTime;
    
    private Long size;
    private String originalName;
    private String fileName;
    private String location;
    
    // 构造函数
    public AttendancePhotoResponse() {}
    
    public AttendancePhotoResponse(TeacherAttendancePhoto photo) {
        this.id = "photo_" + photo.getId();
        
        // 数据库中存储的是相对路径，直接使用即可
        // 例如: attendance_photos\2025-08-25\test_teacher_001\9753c23a.jpg
        String relativePath = photo.getFilePath();
        if (relativePath != null) {
            // 标准化路径分隔符为URL格式
            relativePath = relativePath.replace("\\", "/");
            this.url = "/api/teaching/files/" + relativePath;
        } else {
            this.url = null;
        }
        
        // 处理缩略图路径
        if (photo.getThumbnailPath() != null) {
            String thumbnailRelativePath = photo.getThumbnailPath().replace("\\", "/");
            this.thumbnailUrl = "/api/teaching/files/" + thumbnailRelativePath;
        } else {
            this.thumbnailUrl = this.url;
        }
        
        this.uploadTime = photo.getUploadTime();
        this.size = photo.getFileSize();
        this.originalName = photo.getOriginalName();
        this.fileName = photo.getFileName();
        this.location = photo.getPhotoLocation();
    }
    
    // Getter 和 Setter
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public String getOriginalName() {
        return originalName;
    }
    
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
}


