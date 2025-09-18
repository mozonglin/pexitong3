package com.example.pexitong2.dto.pe;

import com.example.pexitong2.entity.pe.AttendanceRecord;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AttendanceRecordResponse {
    
    private String id;
    private String activityId;
    private String userId;
    private String userName;
    private String studentId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime checkInTime;
    
    private String checkInLocation;
    private String checkedInBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime checkOutTime;
    
    private String checkOutLocation;
    private String checkedOutBy;
    private Boolean isCheckedOut;
    private Integer duration;
    private Integer pointsEarned;
    private String qrCodeData;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
    
    // 可选的关联数据
    private ActivitySummary activity;
    private UserSummary user;
    
    public AttendanceRecordResponse() {}
    
    public AttendanceRecordResponse(AttendanceRecord record) {
        this.id = record.getId();
        this.activityId = record.getActivityId();
        this.userId = record.getUserId();
        this.userName = record.getUserName();
        this.studentId = record.getStudentId();
        this.checkInTime = record.getCheckInTime();
        this.checkInLocation = record.getCheckInLocation();
        this.checkedInBy = record.getCheckedInBy();
        this.checkOutTime = record.getCheckOutTime();
        this.checkOutLocation = record.getCheckOutLocation();
        this.checkedOutBy = record.getCheckedOutBy();
        this.isCheckedOut = record.getIsCheckedOut();
        this.duration = record.getDuration();
        this.pointsEarned = record.getPointsEarned();
        this.qrCodeData = record.getQrCodeData();
        this.createdAt = record.getCreatedAt();
        this.updatedAt = record.getUpdatedAt();
    }
    
    // 内部类用于表示关联数据的摘要信息
    public static class ActivitySummary {
        private String id;
        private String title;
        private String category;
        
        public ActivitySummary() {}
        
        public ActivitySummary(String id, String title, String category) {
            this.id = id;
            this.title = title;
            this.category = category;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    
    public static class UserSummary {
        private String id;
        private String name;
        private String studentId;
        private String school;
        private String college;
        
        public UserSummary() {}
        
        public UserSummary(String id, String name, String studentId, String school, String college) {
            this.id = id;
            this.name = name;
            this.studentId = studentId;
            this.school = school;
            this.college = college;
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
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    
    public String getCheckInLocation() { return checkInLocation; }
    public void setCheckInLocation(String checkInLocation) { this.checkInLocation = checkInLocation; }
    
    public String getCheckedInBy() { return checkedInBy; }
    public void setCheckedInBy(String checkedInBy) { this.checkedInBy = checkedInBy; }
    
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    
    public String getCheckOutLocation() { return checkOutLocation; }
    public void setCheckOutLocation(String checkOutLocation) { this.checkOutLocation = checkOutLocation; }
    
    public String getCheckedOutBy() { return checkedOutBy; }
    public void setCheckedOutBy(String checkedOutBy) { this.checkedOutBy = checkedOutBy; }
    
    public Boolean getIsCheckedOut() { return isCheckedOut; }
    public void setIsCheckedOut(Boolean isCheckedOut) { this.isCheckedOut = isCheckedOut; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }
    
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public ActivitySummary getActivity() { return activity; }
    public void setActivity(ActivitySummary activity) { this.activity = activity; }
    
    public UserSummary getUser() { return user; }
    public void setUser(UserSummary user) { this.user = user; }
}




