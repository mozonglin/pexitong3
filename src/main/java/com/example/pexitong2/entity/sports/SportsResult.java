package com.example.pexitong2.entity.sports;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sports_results", indexes = {
        @Index(name = "idx_sres_event", columnList = "event_id"),
        @Index(name = "idx_sres_reg", columnList = "registration_id")
})
public class SportsResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "heat_id")
    private Long heatId;

    @Column(name = "registration_id", nullable = false)
    private Long registrationId;

    @Column(name = "result_value", length = 50)
    private String resultValue;

    @Column(name = "result_ms", nullable = false)
    private Long resultMs = 0L;

    @Column(name = "result_cm", nullable = false)
    private Integer resultCm = 0;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Column(name = "remark", length = 200)
    private String remark;

    @Column(name = "recorded_by", length = 50)
    private String recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SportsResult() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getHeatId() { return heatId; }
    public void setHeatId(Long heatId) { this.heatId = heatId; }

    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }

    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }

    public Long getResultMs() { return resultMs; }
    public void setResultMs(Long resultMs) { this.resultMs = resultMs; }

    public Integer getResultCm() { return resultCm; }
    public void setResultCm(Integer resultCm) { this.resultCm = resultCm; }

    public Integer getRanking() { return ranking; }
    public void setRanking(Integer ranking) { this.ranking = ranking; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
