package com.example.pexitong2.dto.teaching;

import com.example.pexitong2.entity.teaching.TeacherAttendanceRecord;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 签到历史记录响应DTO
 */
public class AttendanceHistoryResponse {
    
    private String id;
    private String courseId;
    private String courseName;
    private String teacherId;
    private String teacherName;
    private String date;
    private String startTime;
    private String endTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime attendanceTime;
    
    private String status;
    private String note;
    private String location;
    
    // 构造函数
    public AttendanceHistoryResponse() {}
    
    public AttendanceHistoryResponse(TeacherAttendanceRecord record) {
        this.id = "attendance_" + record.getId();
        this.courseId = "course_" + record.getCourseId();
        this.courseName = record.getCourseName();
        this.teacherId = record.getTeacherId();
        this.teacherName = record.getTeacherName();
        this.date = record.getClassDate().toString();
        this.startTime = record.getStartTime().toString();
        this.endTime = record.getEndTime().toString();
        this.attendanceTime = record.getAttendanceTime();
        this.status = record.getAttendanceStatus().name();
        this.note = record.getAttendanceNote();
        this.location = record.getClassroom();
    }
    
    // Getter 和 Setter
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
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
    
    public LocalDateTime getAttendanceTime() {
        return attendanceTime;
    }
    
    public void setAttendanceTime(LocalDateTime attendanceTime) {
        this.attendanceTime = attendanceTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
}




