package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "homework_exercise_standards",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_exercise", columnNames = {"school", "exercise_type"}))
public class HomeworkExerciseStandard {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "school", nullable = false, length = 100)
    private String school;

    @Column(name = "exercise_type", nullable = false, length = 20)
    private String exerciseType;

    @Column(name = "male_standard", nullable = false)
    private int maleStandard;

    @Column(name = "female_standard", nullable = false)
    private int femaleStandard;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public HomeworkExerciseStandard() {}

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }

    public int getMaleStandard() { return maleStandard; }
    public void setMaleStandard(int maleStandard) { this.maleStandard = maleStandard; }

    public int getFemaleStandard() { return femaleStandard; }
    public void setFemaleStandard(int femaleStandard) { this.femaleStandard = femaleStandard; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
