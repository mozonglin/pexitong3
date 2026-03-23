package com.example.pexitong2.entity.sports;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sports_events", indexes = {
        @Index(name = "idx_se_meeting", columnList = "meeting_id"),
        @Index(name = "idx_se_status", columnList = "status"),
        @Index(name = "idx_se_category", columnList = "event_category")
})
public class SportsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "event_category", nullable = false, length = 20)
    private String eventCategory = "track";

    @Column(name = "gender", nullable = false, length = 10)
    private String gender = "male";

    @Column(name = "max_per_team", nullable = false)
    private Integer maxPerTeam = 3;

    @Column(name = "is_relay", nullable = false)
    private Boolean isRelay = false;

    @Column(name = "relay_members", nullable = false)
    private Integer relayMembers = 0;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time", length = 20)
    private String eventTime;

    @Column(name = "venue", length = 200)
    private String venue;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "scoring_type", nullable = false, length = 20)
    private String scoringType = "time";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "pending";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SportsEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getMaxPerTeam() { return maxPerTeam; }
    public void setMaxPerTeam(Integer maxPerTeam) { this.maxPerTeam = maxPerTeam; }

    public Boolean getIsRelay() { return isRelay; }
    public void setIsRelay(Boolean isRelay) { this.isRelay = isRelay; }

    public Integer getRelayMembers() { return relayMembers; }
    public void setRelayMembers(Integer relayMembers) { this.relayMembers = relayMembers; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getScoringType() { return scoringType; }
    public void setScoringType(String scoringType) { this.scoringType = scoringType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
