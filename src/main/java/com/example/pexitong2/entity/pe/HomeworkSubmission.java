package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "homework_submissions")
public class HomeworkSubmission {

    public enum SubmissionStatus {
        pending, submitted, approved, rejected
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "assignment_id", length = 36)
    private String assignmentId;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "completed_count")
    private Integer completedCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SubmissionStatus status = SubmissionStatus.pending;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public HomeworkSubmission() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
