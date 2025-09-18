package com.example.pexitong2.entity.pe;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "location", length = 200)
    private String location;
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;
    
    @Column(name = "registration_start_time")
    private LocalDateTime registrationStartTime;
    
    @Column(name = "registration_end_time")
    private LocalDateTime registrationEndTime;
    
    @Column(name = "activity_start_time")
    private LocalDateTime activityStartTime;
    
    @Column(name = "activity_end_time")
    private LocalDateTime activityEndTime;
    
    @Column(name = "organizer", length = 100)
    private String organizer;
    
    @Column(name = "organizer_id", length = 36)
    private String organizerId;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "points", nullable = false)
    private Integer points = 0;
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Activity() {}
    
    public Activity(String id, String title, String organizerId, String organizer) {
        this.id = id;
        this.title = title;
        this.organizerId = organizerId;
        this.organizer = organizer;
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
    
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { 
        this.approvalStatus = approvalStatus != null ? approvalStatus : ApprovalStatus.PENDING; 
    }
    
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
    
    // 枚举定义
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED, DRAFT
    }
}
