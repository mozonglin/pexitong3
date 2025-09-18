package com.example.pexitong2.dto.pe;

import com.example.pexitong2.entity.pe.MorningExerciseAttendance;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class MorningExerciseAttendanceResponse {
    
    private String id;
    private String exerciseId;
    private String studentId;
    private String studentName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime checkInTime;
    
    private String checkInLocation;
    private String checkedBy;
    private String checkedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime checkOutTime;
    
    private String checkOutLocation;
    private String checkedOutBy;
    private String checkedOutByName;
    private Boolean isCheckedOut;
    private Integer pointsEarned;
    private String qrCodeData;
    private Boolean isValid;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
    
    public MorningExerciseAttendanceResponse() {}
    
    public MorningExerciseAttendanceResponse(MorningExerciseAttendance attendance) {
        this.id = attendance.getId();
        this.exerciseId = attendance.getExerciseId();
        this.studentId = attendance.getStudentId();
        this.studentName = attendance.getStudentName();
        this.checkInTime = attendance.getCheckInTime();
        this.checkInLocation = attendance.getCheckInLocation();
        this.checkedBy = attendance.getCheckedBy();
        this.checkedByName = attendance.getCheckedByName();
        this.checkOutTime = attendance.getCheckOutTime();
        this.checkOutLocation = attendance.getCheckOutLocation();
        this.checkedOutBy = attendance.getCheckedOutBy();
        this.checkedOutByName = attendance.getCheckedOutByName();
        this.isCheckedOut = attendance.getIsCheckedOut();
        this.pointsEarned = attendance.getPointsEarned();
        this.qrCodeData = attendance.getQrCodeData();
        this.isValid = attendance.getIsValid();
        this.createdAt = attendance.getCreatedAt();
        this.updatedAt = attendance.getUpdatedAt();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    
    public String getCheckInLocation() { return checkInLocation; }
    public void setCheckInLocation(String checkInLocation) { this.checkInLocation = checkInLocation; }
    
    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }
    
    public String getCheckedByName() { return checkedByName; }
    public void setCheckedByName(String checkedByName) { this.checkedByName = checkedByName; }
    
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    
    public String getCheckOutLocation() { return checkOutLocation; }
    public void setCheckOutLocation(String checkOutLocation) { this.checkOutLocation = checkOutLocation; }
    
    public String getCheckedOutBy() { return checkedOutBy; }
    public void setCheckedOutBy(String checkedOutBy) { this.checkedOutBy = checkedOutBy; }
    
    public String getCheckedOutByName() { return checkedOutByName; }
    public void setCheckedOutByName(String checkedOutByName) { this.checkedOutByName = checkedOutByName; }
    
    public Boolean getIsCheckedOut() { return isCheckedOut; }
    public void setIsCheckedOut(Boolean isCheckedOut) { this.isCheckedOut = isCheckedOut; }
    
    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }
    
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    
    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}




