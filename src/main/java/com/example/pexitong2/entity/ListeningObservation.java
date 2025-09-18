package com.example.pexitong2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "listening_observations")
public class ListeningObservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "observer_id", nullable = false, length = 50)
    private String observerId;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;
    
    // 评价文件信息
    @Column(name = "evaluation_file_name", length = 255)
    private String evaluationFileName;
    
    @Column(name = "evaluation_file_path", length = 500)
    private String evaluationFilePath;
    
    @Column(name = "evaluation_file_type", length = 100)
    private String evaluationFileType;
    
    @Column(name = "evaluation_file_size")
    private Long evaluationFileSize;
    
    @Column(name = "evaluation_uploaded_at")
    private LocalDateTime evaluationUploadedAt;
    
    // 视频文件信息
    @Column(name = "video_file_name", length = 255)
    private String videoFileName;
    
    @Column(name = "video_file_path", length = 500)
    private String videoFilePath;
    
    @Column(name = "video_file_type", length = 100)
    private String videoFileType;
    
    @Column(name = "video_file_size")
    private Long videoFileSize;
    
    @Column(name = "video_uploaded_at")
    private LocalDateTime videoUploadedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observer_id", insertable = false, updatable = false)
    private User observer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;
    
    // 构造函数
    public ListeningObservation() {}
    
    public ListeningObservation(String observerId, Long courseId, LocalDate classDate) {
        this.observerId = observerId;
        this.courseId = courseId;
        this.classDate = classDate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getObserverId() {
        return observerId;
    }
    
    public void setObserverId(String observerId) {
        this.observerId = observerId;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public LocalDate getClassDate() {
        return classDate;
    }
    
    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }
    
    public String getEvaluationFileName() {
        return evaluationFileName;
    }
    
    public void setEvaluationFileName(String evaluationFileName) {
        this.evaluationFileName = evaluationFileName;
    }
    
    public String getEvaluationFilePath() {
        return evaluationFilePath;
    }
    
    public void setEvaluationFilePath(String evaluationFilePath) {
        this.evaluationFilePath = evaluationFilePath;
    }
    
    public String getEvaluationFileType() {
        return evaluationFileType;
    }
    
    public void setEvaluationFileType(String evaluationFileType) {
        this.evaluationFileType = evaluationFileType;
    }
    
    public Long getEvaluationFileSize() {
        return evaluationFileSize;
    }
    
    public void setEvaluationFileSize(Long evaluationFileSize) {
        this.evaluationFileSize = evaluationFileSize;
    }
    
    public LocalDateTime getEvaluationUploadedAt() {
        return evaluationUploadedAt;
    }
    
    public void setEvaluationUploadedAt(LocalDateTime evaluationUploadedAt) {
        this.evaluationUploadedAt = evaluationUploadedAt;
    }
    
    public String getVideoFileName() {
        return videoFileName;
    }
    
    public void setVideoFileName(String videoFileName) {
        this.videoFileName = videoFileName;
    }
    
    public String getVideoFilePath() {
        return videoFilePath;
    }
    
    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
    }
    
    public String getVideoFileType() {
        return videoFileType;
    }
    
    public void setVideoFileType(String videoFileType) {
        this.videoFileType = videoFileType;
    }
    
    public Long getVideoFileSize() {
        return videoFileSize;
    }
    
    public void setVideoFileSize(Long videoFileSize) {
        this.videoFileSize = videoFileSize;
    }
    
    public LocalDateTime getVideoUploadedAt() {
        return videoUploadedAt;
    }
    
    public void setVideoUploadedAt(LocalDateTime videoUploadedAt) {
        this.videoUploadedAt = videoUploadedAt;
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
    
    public User getObserver() {
        return observer;
    }
    
    public void setObserver(User observer) {
        this.observer = observer;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    // 辅助方法：获取评价文件信息
    public FileInfo getEvaluationFile() {
        if (evaluationFileName != null) {
            return new FileInfo(evaluationFileName, evaluationFilePath, 
                              evaluationFileType, evaluationFileSize, evaluationUploadedAt);
        }
        return null;
    }
    
    // 辅助方法：设置评价文件信息
    public void setEvaluationFile(FileInfo fileInfo) {
        if (fileInfo != null) {
            this.evaluationFileName = fileInfo.getName();
            this.evaluationFilePath = fileInfo.getPath();
            this.evaluationFileType = fileInfo.getType();
            this.evaluationFileSize = fileInfo.getSize();
            this.evaluationUploadedAt = fileInfo.getUploadedAt();
        } else {
            this.evaluationFileName = null;
            this.evaluationFilePath = null;
            this.evaluationFileType = null;
            this.evaluationFileSize = null;
            this.evaluationUploadedAt = null;
        }
    }
    
    // 辅助方法：获取视频文件信息
    public FileInfo getVideoFile() {
        if (videoFileName != null) {
            return new FileInfo(videoFileName, videoFilePath, 
                              videoFileType, videoFileSize, videoUploadedAt);
        }
        return null;
    }
    
    // 辅助方法：设置视频文件信息
    public void setVideoFile(FileInfo fileInfo) {
        if (fileInfo != null) {
            this.videoFileName = fileInfo.getName();
            this.videoFilePath = fileInfo.getPath();
            this.videoFileType = fileInfo.getType();
            this.videoFileSize = fileInfo.getSize();
            this.videoUploadedAt = fileInfo.getUploadedAt();
        } else {
            this.videoFileName = null;
            this.videoFilePath = null;
            this.videoFileType = null;
            this.videoFileSize = null;
            this.videoUploadedAt = null;
        }
    }
} 