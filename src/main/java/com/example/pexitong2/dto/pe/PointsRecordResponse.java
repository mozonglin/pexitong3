package com.example.pexitong2.dto.pe;

import com.example.pexitong2.entity.pe.PointsRecord;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PointsRecordResponse {
    
    private String id;
    private String userId;
    private String activityId;
    private String activityName;
    private String activityType;
    private Integer pointsEarned;
    private String earnedReason;
    private String calculationRule;
    private Integer participationDuration;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime earnedAt;
    
    public PointsRecordResponse() {}
    
    public PointsRecordResponse(PointsRecord record) {
        this.id = record.getId();
        this.userId = record.getUserId();
        this.activityId = record.getActivityId();
        this.activityName = record.getActivityName();
        this.activityType = record.getActivityType();
        this.pointsEarned = record.getPointsEarned();
        this.earnedReason = record.getEarnedReason();
        this.calculationRule = record.getCalculationRule();
        this.participationDuration = record.getParticipationDuration();
        this.earnedAt = record.getEarnedAt();
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




