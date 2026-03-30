package com.example.pexitong2.entity.race;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 上传的比赛成绩 Excel 文件记录
 */
@Entity
@Table(name = "race_excel_files", indexes = {
        @Index(name = "idx_excel_school",     columnList = "school"),
        @Index(name = "idx_excel_created_at", columnList = "created_at")
})
public class RaceExcelFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 原始文件名（上传时的文件名） */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /** 服务器保存时实际使用的文件名（含时间戳前缀，防重名） */
    @Column(name = "saved_filename", nullable = false, length = 255)
    private String savedFilename;

    @Column(name = "school", nullable = false, length = 200)
    private String school;

    @Column(name = "teacher_name", length = 100)
    private String teacherName;

    @Column(name = "uploader_id", nullable = false, length = 50)
    private String uploaderId;

    /** 比赛/上传时间（由上传方提供，UTC 转本地） */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /** 文件在服务器上的存储路径（完整路径） */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /** 文件字节大小 */
    @Column(name = "file_size")
    private Long fileSize;

    /** 解析入库时间；null 表示尚未解析 */
    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RaceExcelFile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getSavedFilename() { return savedFilename; }
    public void setSavedFilename(String savedFilename) { this.savedFilename = savedFilename; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getParsedAt() { return parsedAt; }
    public void setParsedAt(LocalDateTime parsedAt) { this.parsedAt = parsedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
