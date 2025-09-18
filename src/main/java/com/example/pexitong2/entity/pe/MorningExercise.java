package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "morning_exercises")
public class MorningExercise {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title = "早操考勤";
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "location", length = 200)
    private String location = "操场";
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ExerciseType type = ExerciseType.MORNING_EXERCISE;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "total_participants")
    private Integer totalParticipants = 0;
    
    @Column(name = "checked_in_count")
    private Integer checkedInCount = 0;
    
    @Column(name = "checked_out_count")
    private Integer checkedOutCount = 0;
    
    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 构造函数
    public MorningExercise() {}
    
    public MorningExercise(String id, LocalDate date, LocalDateTime startTime, LocalDateTime endTime, String createdBy) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public ExerciseType getType() { return type; }
    public void setType(ExerciseType type) { this.type = type; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Integer getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(Integer totalParticipants) { this.totalParticipants = totalParticipants; }
    
    public Integer getCheckedInCount() { return checkedInCount; }
    public void setCheckedInCount(Integer checkedInCount) { this.checkedInCount = checkedInCount; }
    
    public Integer getCheckedOutCount() { return checkedOutCount; }
    public void setCheckedOutCount(Integer checkedOutCount) { this.checkedOutCount = checkedOutCount; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 枚举定义
    public enum ExerciseType {
        MORNING_EXERCISE
    }
}




