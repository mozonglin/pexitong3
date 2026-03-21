package com.example.pexitong2.entity.training;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_tasks")
public class TrainingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false, length = 50)
    private String teacherId;

    @Column(name = "teacher_name", nullable = false, length = 50)
    private String teacherName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", nullable = false)
    private TrainingType trainingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.medium;

    @Column(name = "target_classes", nullable = false, columnDefinition = "TEXT")
    private String targetClasses; // JSON数组

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String requirements; // JSON

    @Column(name = "ai_generated")
    private Boolean aiGenerated = false;

    @Column(name = "ai_reference", columnDefinition = "TEXT")
    private String aiReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.active;

    @Column(name = "total_students")
    private Integer totalStudents = 0;

    @Column(name = "completed_students")
    private Integer completedStudents = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TrainingTask() {}

    // Enums
    public enum TrainingType {
        endurance, strength, flexibility, skill, comprehensive
    }

    public enum Difficulty {
        easy, medium, hard
    }

    public enum TaskStatus {
        draft, active, completed, cancelled
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TrainingType getTrainingType() { return trainingType; }
    public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public String getTargetClasses() { return targetClasses; }
    public void setTargetClasses(String targetClasses) { this.targetClasses = targetClasses; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public Boolean getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(Boolean aiGenerated) { this.aiGenerated = aiGenerated; }

    public String getAiReference() { return aiReference; }
    public void setAiReference(String aiReference) { this.aiReference = aiReference; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public Integer getCompletedStudents() { return completedStudents; }
    public void setCompletedStudents(Integer completedStudents) { this.completedStudents = completedStudents; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
