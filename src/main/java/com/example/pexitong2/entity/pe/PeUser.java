package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users1")
public class PeUser {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "student_id", unique = true, nullable = false, length = 20)
    private String studentId;
    
    @Column(name = "school", length = 100)
    private String school;
    
    @Column(name = "college", length = 100)
    private String college;
    
    @Column(name = "class_name", length = 100)
    private String className;
    
    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar;
    
    @Column(name = "points", nullable = false)
    private Integer points = 0;
    
    @Column(name = "pe_activity_points", nullable = false)
    private Integer peActivityPoints = 0;
    
    @Column(name = "morning_exercise_points", nullable = false)
    private Integer morningExercisePoints = 0;
    
    @Column(name = "sunshine_total_runs", nullable = false)
    private Integer sunshineTotalRuns = 0;
    
    @Column(name = "sunshine_total_distance")
    private Double sunshineTotalDistance = 0.0;
    
    @Column(name = "sunshine_total_duration")
    private Long sunshineTotalDuration = 0L;
    
    @Column(name = "study_hours", nullable = false)
    private Integer studyHours = 0;
    
    @Column(name = "integrity_score", nullable = false)
    private Integer integrityScore = 100;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.STUDENT;
    
    @Column(name = "is_logged_in", nullable = false)
    private Boolean isLoggedIn = false;
    
    @Column(name = "points_last_updated", nullable = false)
    private LocalDateTime pointsLastUpdated = LocalDateTime.now();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 构造函数
    public PeUser() {}
    
    public PeUser(String id, String name, String studentId, String school, String college, String className, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.studentId = studentId;
        this.school = school;
        this.college = college;
        this.className = className;
        this.phoneNumber = phoneNumber;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public Integer getPeActivityPoints() { return peActivityPoints; }
    public void setPeActivityPoints(Integer peActivityPoints) { this.peActivityPoints = peActivityPoints; }
    
    public Integer getMorningExercisePoints() { return morningExercisePoints; }
    public void setMorningExercisePoints(Integer morningExercisePoints) { this.morningExercisePoints = morningExercisePoints; }
    
    public Integer getSunshineTotalRuns() { return sunshineTotalRuns; }
    public void setSunshineTotalRuns(Integer sunshineTotalRuns) { this.sunshineTotalRuns = sunshineTotalRuns; }
    
    public Double getSunshineTotalDistance() { return sunshineTotalDistance; }
    public void setSunshineTotalDistance(Double sunshineTotalDistance) { this.sunshineTotalDistance = sunshineTotalDistance; }
    
    public Long getSunshineTotalDuration() { return sunshineTotalDuration; }
    public void setSunshineTotalDuration(Long sunshineTotalDuration) { this.sunshineTotalDuration = sunshineTotalDuration; }
    
    public Integer getStudyHours() { return studyHours; }
    public void setStudyHours(Integer studyHours) { this.studyHours = studyHours; }
    
    public Integer getIntegrityScore() { return integrityScore; }
    public void setIntegrityScore(Integer integrityScore) { this.integrityScore = integrityScore; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { 
        this.role = role != null ? role : Role.STUDENT; 
    }
    
    public Boolean getIsLoggedIn() { return isLoggedIn; }
    public void setIsLoggedIn(Boolean isLoggedIn) { this.isLoggedIn = isLoggedIn; }
    
    public LocalDateTime getPointsLastUpdated() { return pointsLastUpdated; }
    public void setPointsLastUpdated(LocalDateTime pointsLastUpdated) { this.pointsLastUpdated = pointsLastUpdated; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 枚举定义
    public enum Role {
        STUDENT, CHECKER, SUB_CHECKER, ADMIN
    }
}
