package com.example.pexitong2.dto;

import com.example.pexitong2.entity.ListeningObservation;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ObservationResponse {
    
    private Long id;
    
    @JsonProperty("observer_name")
    private String observerName;
    
    @JsonProperty("observer_type")
    private String observerType;
    
    @JsonProperty("observer_id")
    private String observerId;
    
    @JsonProperty("course_name")
    private String courseName;
    
    @JsonProperty("teacher_name")
    private String teacherName;
    
    @JsonProperty("teacher_id")
    private String teacherId;
    
    @JsonProperty("class_name")
    private String className;
    
    @JsonProperty("class_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate classDate;
    
    private String classroom;
    
    @JsonProperty("evaluation_file")
    private FileInfoDTO evaluationFile;
    
    @JsonProperty("video_file")
    private FileInfoDTO videoFile;
    
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    // 构造函数
    public ObservationResponse() {}
    
    // 从实体转换
    public static ObservationResponse fromEntity(ListeningObservation observation) {
        ObservationResponse response = new ObservationResponse();
        response.setId(observation.getId());
        response.setObserverId(observation.getObserverId());
        response.setClassDate(observation.getClassDate());
        response.setCreatedAt(observation.getCreatedAt());
        
        // 设置听课者信息
        if (observation.getObserver() != null) {
            response.setObserverName(observation.getObserver().getRealName());
            response.setObserverType(observation.getObserver().getUserType().name());
        }
        
        // 设置课程信息
        if (observation.getCourse() != null) {
            response.setCourseName(observation.getCourse().getCourseName());
            response.setTeacherName(observation.getCourse().getTeacherName());
            response.setTeacherId(observation.getCourse().getTeacherId());
            response.setClassName(observation.getCourse().getClassName());
            response.setClassroom(observation.getCourse().getClassroom());
        }
        
        // 设置文件信息
        response.setEvaluationFile(FileInfoDTO.fromEntity(observation.getEvaluationFile()));
        response.setVideoFile(FileInfoDTO.fromEntity(observation.getVideoFile()));
        
        return response;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getObserverName() {
        return observerName;
    }
    
    public void setObserverName(String observerName) {
        this.observerName = observerName;
    }
    
    public String getObserverType() {
        return observerType;
    }
    
    public void setObserverType(String observerType) {
        this.observerType = observerType;
    }
    
    public String getObserverId() {
        return observerId;
    }
    
    public void setObserverId(String observerId) {
        this.observerId = observerId;
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
    
    public LocalDate getClassDate() {
        return classDate;
    }
    
    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }
    
    public String getClassroom() {
        return classroom;
    }
    
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
    
    public FileInfoDTO getEvaluationFile() {
        return evaluationFile;
    }
    
    public void setEvaluationFile(FileInfoDTO evaluationFile) {
        this.evaluationFile = evaluationFile;
    }
    
    public FileInfoDTO getVideoFile() {
        return videoFile;
    }
    
    public void setVideoFile(FileInfoDTO videoFile) {
        this.videoFile = videoFile;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 