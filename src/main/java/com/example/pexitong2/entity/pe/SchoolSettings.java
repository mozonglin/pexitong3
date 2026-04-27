package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_settings")
public class SchoolSettings {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "school", unique = true, length = 100)
    private String school;

    @Column(name = "sunshine_run_distance")
    private Integer sunshineRunDistance = 1600;

    @Column(name = "class_selection_max")
    private Integer classSelectionMax = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SchoolSettings() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public Integer getSunshineRunDistance() { return sunshineRunDistance; }
    public void setSunshineRunDistance(Integer sunshineRunDistance) { this.sunshineRunDistance = sunshineRunDistance; }

    public Integer getClassSelectionMax() { return classSelectionMax; }
    public void setClassSelectionMax(Integer classSelectionMax) { this.classSelectionMax = classSelectionMax; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
