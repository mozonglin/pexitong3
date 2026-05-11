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

    @Column(name = "sunshine_run_distance_male")
    private Integer sunshineRunDistanceMale;

    @Column(name = "sunshine_run_distance_female")
    private Integer sunshineRunDistanceFemale;

    @Column(name = "sunshine_run_pace_min_male")
    private Double sunshineRunPaceMinMale;

    @Column(name = "sunshine_run_pace_max_male")
    private Double sunshineRunPaceMaxMale;

    @Column(name = "sunshine_run_pace_min_female")
    private Double sunshineRunPaceMinFemale;

    @Column(name = "sunshine_run_pace_max_female")
    private Double sunshineRunPaceMaxFemale;

    @Column(name = "sunshine_run_runs_per_week")
    private Integer sunshineRunRunsPerWeek = 3;

    @Column(name = "sunshine_run_total_weeks")
    private Integer sunshineRunTotalWeeks = 16;

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

    public Integer getSunshineRunDistanceMale() { return sunshineRunDistanceMale; }
    public void setSunshineRunDistanceMale(Integer sunshineRunDistanceMale) { this.sunshineRunDistanceMale = sunshineRunDistanceMale; }

    public Integer getSunshineRunDistanceFemale() { return sunshineRunDistanceFemale; }
    public void setSunshineRunDistanceFemale(Integer sunshineRunDistanceFemale) { this.sunshineRunDistanceFemale = sunshineRunDistanceFemale; }

    public Double getSunshineRunPaceMinMale() { return sunshineRunPaceMinMale; }
    public void setSunshineRunPaceMinMale(Double sunshineRunPaceMinMale) { this.sunshineRunPaceMinMale = sunshineRunPaceMinMale; }

    public Double getSunshineRunPaceMaxMale() { return sunshineRunPaceMaxMale; }
    public void setSunshineRunPaceMaxMale(Double sunshineRunPaceMaxMale) { this.sunshineRunPaceMaxMale = sunshineRunPaceMaxMale; }

    public Double getSunshineRunPaceMinFemale() { return sunshineRunPaceMinFemale; }
    public void setSunshineRunPaceMinFemale(Double sunshineRunPaceMinFemale) { this.sunshineRunPaceMinFemale = sunshineRunPaceMinFemale; }

    public Double getSunshineRunPaceMaxFemale() { return sunshineRunPaceMaxFemale; }
    public void setSunshineRunPaceMaxFemale(Double sunshineRunPaceMaxFemale) { this.sunshineRunPaceMaxFemale = sunshineRunPaceMaxFemale; }

    public Integer getSunshineRunRunsPerWeek() { return sunshineRunRunsPerWeek; }
    public void setSunshineRunRunsPerWeek(Integer sunshineRunRunsPerWeek) { this.sunshineRunRunsPerWeek = sunshineRunRunsPerWeek; }

    public Integer getSunshineRunTotalWeeks() { return sunshineRunTotalWeeks; }
    public void setSunshineRunTotalWeeks(Integer sunshineRunTotalWeeks) { this.sunshineRunTotalWeeks = sunshineRunTotalWeeks; }

    public Integer getClassSelectionMax() { return classSelectionMax; }
    public void setClassSelectionMax(Integer classSelectionMax) { this.classSelectionMax = classSelectionMax; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
