package com.example.pexitong2.entity.teaching;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_attendance_photos")
public class TeacherAttendancePhoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "attendance_record_id", nullable = false)
    private Long attendanceRecordId;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "teacher_id", nullable = false, length = 50)
    private String teacherId;
    
    // 照片信息
    @Column(name = "original_name", nullable = false)
    private String originalName;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;
    
    // 缩略图信息
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;
    
    @Column(name = "thumbnail_size")
    private Long thumbnailSize;
    
    // 拍摄信息
    @Column(name = "photo_location", length = 200)
    private String photoLocation;
    
    @Column(name = "photo_note", length = 500)
    private String photoNote;
    
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联签到记录
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_record_id", insertable = false, updatable = false)
    private TeacherAttendanceRecord attendanceRecord;
    
    // 构造函数
    public TeacherAttendancePhoto() {}
    
    public TeacherAttendancePhoto(Long attendanceRecordId, Long courseId, String teacherId,
                                  String originalName, String fileName, String filePath,
                                  Long fileSize, String fileType) {
        this.attendanceRecordId = attendanceRecordId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.originalName = originalName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploadTime = LocalDateTime.now();
    }
    
    // Getter 和 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAttendanceRecordId() {
        return attendanceRecordId;
    }
    
    public void setAttendanceRecordId(Long attendanceRecordId) {
        this.attendanceRecordId = attendanceRecordId;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
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
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getThumbnailPath() {
        return thumbnailPath;
    }
    
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
    
    public Long getThumbnailSize() {
        return thumbnailSize;
    }
    
    public void setThumbnailSize(Long thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }
    
    public String getPhotoLocation() {
        return photoLocation;
    }
    
    public void setPhotoLocation(String photoLocation) {
        this.photoLocation = photoLocation;
    }
    
    public String getPhotoNote() {
        return photoNote;
    }
    
    public void setPhotoNote(String photoNote) {
        this.photoNote = photoNote;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
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
    
    public TeacherAttendanceRecord getAttendanceRecord() {
        return attendanceRecord;
    }
    
    public void setAttendanceRecord(TeacherAttendanceRecord attendanceRecord) {
        this.attendanceRecord = attendanceRecord;
    }
}




