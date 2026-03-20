package com.example.pexitong2.entity.race;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 运动员比赛成绩实体（独立模块，不关联其它用户表）
 */
@Entity
@Table(name = "race_results", indexes = {
        @Index(name = "idx_student_number", columnList = "student_number"),
        @Index(name = "idx_school",         columnList = "school"),
        @Index(name = "idx_uploaded_at",    columnList = "uploaded_at"),
        @Index(name = "idx_uploader_id",    columnList = "uploader_id")
})
public class RaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", nullable = false, length = 50)
    private String studentNumber;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "school", nullable = false, length = 200)
    private String school;

    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    @Column(name = "total_laps", nullable = false)
    private Integer totalLaps;

    @Column(name = "final_time", length = 20)
    private String finalTime;

    @Column(name = "final_time_ms")
    private Long finalTimeMs;

    @Column(name = "teacher_name", nullable = false, length = 100)
    private String teacherName;

    @Column(name = "uploader_id", nullable = false, length = 50)
    private String uploaderId;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RaceResult() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getTotalLaps() { return totalLaps; }
    public void setTotalLaps(Integer totalLaps) { this.totalLaps = totalLaps; }

    public String getFinalTime() { return finalTime; }
    public void setFinalTime(String finalTime) { this.finalTime = finalTime; }

    public Long getFinalTimeMs() { return finalTimeMs; }
    public void setFinalTimeMs(Long finalTimeMs) { this.finalTimeMs = finalTimeMs; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
