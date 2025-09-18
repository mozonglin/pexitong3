package com.example.pexitong2.dto;

import com.example.pexitong2.entity.Course;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class CourseSearchResponse {
    
    private Long id;
    
    @JsonProperty("course_name")
    private String courseName;
    
    @JsonProperty("teacher_name")
    private String teacherName;
    
    @JsonProperty("teacher_id")
    private String teacherId;
    
    @JsonProperty("class_name")
    private String className;
    
    private String classroom;
    
    @JsonProperty("class_time")
    private String classTime;
    
    @JsonProperty("class_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate classDate;
    
    // 构造函数
    public CourseSearchResponse() {}
    
    // 从实体转换
    public static CourseSearchResponse fromEntity(Course course) {
        CourseSearchResponse response = new CourseSearchResponse();
        response.setId(course.getId());
        response.setCourseName(course.getCourseName());
        response.setTeacherName(course.getTeacherName());
        response.setTeacherId(course.getTeacherId());
        response.setClassName(course.getClassName());
        response.setClassroom(course.getClassroom());
        response.setClassTime(course.getClassTime());
        response.setClassDate(course.getClassDate());
        return response;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getClassroom() {
        return classroom;
    }
    
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
    
    public String getClassTime() {
        return classTime;
    }
    
    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }
    
    public LocalDate getClassDate() {
        return classDate;
    }
    
    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }
} 