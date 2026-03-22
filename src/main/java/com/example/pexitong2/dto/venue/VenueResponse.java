package com.example.pexitong2.dto.venue;

import com.example.pexitong2.entity.Venue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VenueResponse {

    private String id;
    private String name;
    private String type;
    private Integer capacity;
    private String location;
    private BigDecimal price;

    @JsonProperty("open_time")
    private String openTime;

    private String description;
    private String status;

    private String school;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public VenueResponse() {}

    public VenueResponse(Venue venue) {
        this.id = venue.getId();
        this.name = venue.getName();
        this.type = venue.getType();
        this.capacity = venue.getCapacity();
        this.location = venue.getLocation();
        this.price = venue.getPrice();
        this.openTime = venue.getOpenTime();
        this.description = venue.getDescription();
        this.status = venue.getStatus();
        this.school = venue.getSchool();
        this.createdBy = venue.getCreatedBy();
        this.createdAt = venue.getCreatedAt();
        this.updatedAt = venue.getUpdatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
