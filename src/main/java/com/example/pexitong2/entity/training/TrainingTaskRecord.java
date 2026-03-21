package com.example.pexitong2.entity.training;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_task_records")
public class TrainingTaskRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "student_id", nullable = false, length = 30)
    private String studentId;

    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Column(name = "class_name", length = 100)
    private String className;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status = RecordStatus.pending;

    @Column(name = "completion_data", columnDefinition = "TEXT")
    private String completionData; // JSON

    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(precision = 5, scale = 1)
    private BigDecimal score;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private TrainingTask task;

    public TrainingTaskRecord() {}

    public enum RecordStatus {
        pending, in_progress, completed, overdue
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public RecordStatus getStatus() { return status; }
    public void setStatus(RecordStatus status) { this.status = status; }

    public String getCompletionData() { return completionData; }
    public void setCompletionData(String completionData) { this.completionData = completionData; }

    public BigDecimal getCompletionRate() { return completionRate; }
    public void setCompletionRate(BigDecimal completionRate) { this.completionRate = completionRate; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public TrainingTask getTask() { return task; }
    public void setTask(TrainingTask task) { this.task = task; }
}
