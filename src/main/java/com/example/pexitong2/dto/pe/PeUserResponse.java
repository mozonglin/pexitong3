package com.example.pexitong2.dto.pe;

import com.example.pexitong2.entity.pe.PeUser;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PeUserResponse {
    
    private String id;
    private String name;
    private String studentId;
    private String school;
    private String college;
    private String className;
    private String phoneNumber;
    private String avatar;
    private Integer points;
    private Integer peActivityPoints;
    private Integer morningExercisePoints;
    private Integer sunshineTotalRuns;
    private Double sunshineTotalDistance;
    private Long sunshineTotalDuration;
    private Integer studyHours;
    private Integer integrityScore;
    private String role;
    private Boolean isLoggedIn;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime pointsLastUpdated;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
    
    public PeUserResponse() {}
    
    public PeUserResponse(PeUser user) {
        this.id = user.getId();
        this.name = user.getName();
        this.studentId = user.getStudentId();
        this.school = user.getSchool();
        this.college = user.getCollege();
        this.className = user.getClassName();
        this.phoneNumber = user.getPhoneNumber();
        this.avatar = user.getAvatar();
        this.points = user.getPoints();
        this.peActivityPoints = user.getPeActivityPoints();
        this.morningExercisePoints = user.getMorningExercisePoints();
        this.sunshineTotalRuns = user.getSunshineTotalRuns();
        this.sunshineTotalDistance = user.getSunshineTotalDistance();
        this.sunshineTotalDuration = user.getSunshineTotalDuration();
        this.studyHours = user.getStudyHours();
        this.integrityScore = user.getIntegrityScore();
        this.role = user.getRole() != null ? user.getRole().name() : null;
        this.isLoggedIn = user.getIsLoggedIn();
        this.pointsLastUpdated = user.getPointsLastUpdated();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public Integer getPeActivityPoints() { return peActivityPoints; }
    public void setPeActivityPoints(Integer peActivityPoints) { this.peActivityPoints = peActivityPoints; }
    
    public Integer getMorningExercisePoints() { return morningExercisePoints; }
    public void setMorningExercisePoints(Integer morningExercisePoints) { this.morningExercisePoints = morningExercisePoints; }

    public Integer getSunshineTotalRuns() { return sunshineTotalRuns; }
    public void setSunshineTotalRuns(Integer sunshineTotalRuns) { this.sunshineTotalRuns = sunshineTotalRuns; }

    public Double getSunshineTotalDistance() { return sunshineTotalDistance; }
    public void setSunshineTotalDistance(Double sunshineTotalDistance) { this.sunshineTotalDistance = sunshineTotalDistance; }

    public Long getSunshineTotalDuration() { return sunshineTotalDuration; }
    public void setSunshineTotalDuration(Long sunshineTotalDuration) { this.sunshineTotalDuration = sunshineTotalDuration; }

    public Integer getStudyHours() { return studyHours; }
    public void setStudyHours(Integer studyHours) { this.studyHours = studyHours; }
    
    public Integer getIntegrityScore() { return integrityScore; }
    public void setIntegrityScore(Integer integrityScore) { this.integrityScore = integrityScore; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Boolean getIsLoggedIn() { return isLoggedIn; }
    public void setIsLoggedIn(Boolean isLoggedIn) { this.isLoggedIn = isLoggedIn; }
    
    public LocalDateTime getPointsLastUpdated() { return pointsLastUpdated; }
    public void setPointsLastUpdated(LocalDateTime pointsLastUpdated) { this.pointsLastUpdated = pointsLastUpdated; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}




