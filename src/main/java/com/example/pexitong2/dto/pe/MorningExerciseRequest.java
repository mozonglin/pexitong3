package com.example.pexitong2.dto.pe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.example.pexitong2.util.FlexibleDateTimeDeserializer;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MorningExerciseRequest {
    
    private String title;
    private String description;
    private String location;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonDeserialize(using = FlexibleDateTimeDeserializer.class)
    private LocalDateTime startTime;
    
    @JsonDeserialize(using = FlexibleDateTimeDeserializer.class)
    private LocalDateTime endTime;
    
    public MorningExerciseRequest() {}
    
    public MorningExerciseRequest(String title, String description, String location, 
                                LocalDate date, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
