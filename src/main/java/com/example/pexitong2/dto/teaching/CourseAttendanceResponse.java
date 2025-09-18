package com.example.pexitong2.dto.teaching;

import com.example.pexitong2.entity.teaching.TeacherAttendanceRecord;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 课程签到响应DTO
 */
public class CourseAttendanceResponse {
    
    private String id;
    private String name;
    private String teacherId;
    private String teacherName;
    private String date;
    private String startTime;
    private String endTime;
    private String location;
    private String attendanceStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime attendanceTime;
    
    private String attendanceLocation;
    private String attendanceNote;
    private String attendancePhotoUrl;
    
    // 构造函数
    public CourseAttendanceResponse() {}
    
    public CourseAttendanceResponse(TeacherAttendanceRecord record) {
        this.id = "course_" + record.getCourseId();
        this.name = record.getCourseName();
        this.teacherId = record.getTeacherId();
        this.teacherName = record.getTeacherName();
        this.date = record.getClassDate().toString();
        this.startTime = record.getStartTime().toString();
        this.endTime = record.getEndTime().toString();
        this.location = record.getClassroom();
        this.attendanceStatus = record.getAttendanceStatus().name();
        this.attendanceTime = record.getAttendanceTime();
        this.attendanceLocation = record.getAttendanceLocation();
        this.attendanceNote = record.getAttendanceNote();
        
        // 照片URL将在Service层单独设置，避免懒加载问题
        this.attendancePhotoUrl = null;
    }
    
    // Getter 和 Setter
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getAttendanceStatus() {
        return attendanceStatus;
    }
    
    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
    
    public LocalDateTime getAttendanceTime() {
        return attendanceTime;
    }
    
    public void setAttendanceTime(LocalDateTime attendanceTime) {
        this.attendanceTime = attendanceTime;
    }
    
    public String getAttendanceLocation() {
        return attendanceLocation;
    }
    
    public void setAttendanceLocation(String attendanceLocation) {
        this.attendanceLocation = attendanceLocation;
    }
    
    public String getAttendanceNote() {
        return attendanceNote;
    }
    
    public void setAttendanceNote(String attendanceNote) {
        this.attendanceNote = attendanceNote;
    }
    
    public String getAttendancePhotoUrl() {
        return attendancePhotoUrl;
    }
    
    public void setAttendancePhotoUrl(String attendancePhotoUrl) {
        this.attendancePhotoUrl = attendancePhotoUrl;
    }
}
