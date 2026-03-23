package com.example.pexitong2.entity.sports;

import jakarta.persistence.*;

@Entity
@Table(name = "sports_heat_lanes", indexes = {
        @Index(name = "idx_shl_heat", columnList = "heat_id"),
        @Index(name = "idx_shl_reg", columnList = "registration_id")
})
public class SportsHeatLane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heat_id", nullable = false)
    private Long heatId;

    @Column(name = "registration_id", nullable = false)
    private Long registrationId;

    @Column(name = "lane_number", nullable = false)
    private Integer laneNumber = 0;

    public SportsHeatLane() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHeatId() { return heatId; }
    public void setHeatId(Long heatId) { this.heatId = heatId; }

    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }

    public Integer getLaneNumber() { return laneNumber; }
    public void setLaneNumber(Integer laneNumber) { this.laneNumber = laneNumber; }
}
