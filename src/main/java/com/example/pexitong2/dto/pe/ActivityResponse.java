package com.example.pexitong2.dto.pe;

import com.example.pexitong2.entity.pe.Activity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ActivityResponse {
    
    private String id;
    private String title;
    private String description;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime registrationStartTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime registrationEndTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime activityStartTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime activityEndTime;
    
    private String organizer;
    private String organizerId;
    private String category;
    private Integer points;
    private String imageUrl;
    private String approvalStatus;
    private String reviewedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime reviewedAt;
    
    private String reviewComment;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
    
    public ActivityResponse() {}
    
    public ActivityResponse(Activity activity) {
        this.id = activity.getId();
        this.title = activity.getTitle();
        this.description = activity.getDescription();
        this.location = activity.getLocation();
        this.maxParticipants = activity.getMaxParticipants();
        this.currentParticipants = activity.getCurrentParticipants();
        this.registrationStartTime = activity.getRegistrationStartTime();
        this.registrationEndTime = activity.getRegistrationEndTime();
        this.activityStartTime = activity.getActivityStartTime();
        this.activityEndTime = activity.getActivityEndTime();
        this.organizer = activity.getOrganizer();
        this.organizerId = activity.getOrganizerId();
        this.category = activity.getCategory();
        this.points = activity.getPoints();
        this.imageUrl = activity.getImageUrl();
        this.approvalStatus = activity.getApprovalStatus() != null ? activity.getApprovalStatus().name() : null;
        this.reviewedBy = activity.getReviewedBy();
        this.reviewedAt = activity.getReviewedAt();
        this.reviewComment = activity.getReviewComment();
        this.createdAt = activity.getCreatedAt();
        this.updatedAt = activity.getUpdatedAt();
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
    
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    
    public Integer getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(Integer currentParticipants) { this.currentParticipants = currentParticipants; }
    
    public LocalDateTime getRegistrationStartTime() { return registrationStartTime; }
    public void setRegistrationStartTime(LocalDateTime registrationStartTime) { this.registrationStartTime = registrationStartTime; }
    
    public LocalDateTime getRegistrationEndTime() { return registrationEndTime; }
    public void setRegistrationEndTime(LocalDateTime registrationEndTime) { this.registrationEndTime = registrationEndTime; }
    
    public LocalDateTime getActivityStartTime() { return activityStartTime; }
    public void setActivityStartTime(LocalDateTime activityStartTime) { this.activityStartTime = activityStartTime; }
    
    public LocalDateTime getActivityEndTime() { return activityEndTime; }
    public void setActivityEndTime(LocalDateTime activityEndTime) { this.activityEndTime = activityEndTime; }
    
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}




