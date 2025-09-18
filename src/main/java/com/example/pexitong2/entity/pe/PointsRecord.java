package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_records")
public class PointsRecord {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    @Column(name = "activity_id", nullable = false, length = 36)
    private String activityId;
    
    @Column(name = "activity_name", nullable = false, length = 200)
    private String activityName;
    
    @Column(name = "activity_type", nullable = false, length = 20)
    private String activityType;
    
    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;
    
    @Column(name = "earned_reason", length = 200)
    private String earnedReason;
    
    @Column(name = "calculation_rule", length = 200)
    private String calculationRule;
    
    @Column(name = "participation_duration", nullable = false)
    private Integer participationDuration = 0;
    
    @CreationTimestamp
    @Column(name = "earned_at", nullable = false, updatable = false)
    private LocalDateTime earnedAt;
    
    // 构造函数
    public PointsRecord() {}
    
    public PointsRecord(String id, String userId, String activityId, String activityName, 
                       String activityType, Integer pointsEarned) {
        this.id = id;
        this.userId = userId;
        this.activityId = activityId;
        this.activityName = activityName;
        this.activityType = activityType;
        this.pointsEarned = pointsEarned;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    
    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }
    
    public String getEarnedReason() { return earnedReason; }
    public void setEarnedReason(String earnedReason) { this.earnedReason = earnedReason; }
    
    public String getCalculationRule() { return calculationRule; }
    public void setCalculationRule(String calculationRule) { this.calculationRule = calculationRule; }
    
    public Integer getParticipationDuration() { return participationDuration; }
    public void setParticipationDuration(Integer participationDuration) { this.participationDuration = participationDuration; }
    
    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
}




