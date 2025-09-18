package com.example.pexitong2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateObservationRequest {
    
    @JsonProperty("class_date")
    @NotNull(message = "上课日期不能为空")
    private LocalDate classDate;
    
    @JsonProperty("course_id")
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    // 构造函数
    public CreateObservationRequest() {}
    
    public CreateObservationRequest(LocalDate classDate, Long courseId) {
        this.classDate = classDate;
        this.courseId = courseId;
    }
    
    // Getters and Setters
    public LocalDate getClassDate() {
        return classDate;
    }
    
    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
} 