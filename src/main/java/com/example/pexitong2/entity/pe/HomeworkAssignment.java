package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "homework_assignments")
public class HomeworkAssignment {

    public enum AssignmentStatus {
        active, expired, cancelled
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "teacher_id", length = 50)
    private String teacherId;

    @Column(name = "temp_class_id", length = 36)
    private String tempClassId;

    @Column(length = 100)
    private String school;

    @Column(length = 200)
    private String title;

    @Column(name = "exercise_type", length = 100)
    private String exerciseType;

    @Column(name = "required_count")
    private Integer requiredCount;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AssignmentStatus status = AssignmentStatus.active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public HomeworkAssignment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTempClassId() { return tempClassId; }
    public void setTempClassId(String tempClassId) { this.tempClassId = tempClassId; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }

    public Integer getRequiredCount() { return requiredCount; }
    public void setRequiredCount(Integer requiredCount) { this.requiredCount = requiredCount; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
