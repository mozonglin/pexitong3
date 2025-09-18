package com.example.pexitong2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;
    
    @Column(name = "teacher_id", nullable = false, length = 50)
    private String teacherId;
    
    @Column(name = "teacher_name", nullable = false, length = 50)
    private String teacherName;
    
    @Column(name = "class_name", nullable = false, length = 50)
    private String className;
    
    @Column(length = 100)
    private String classroom;
    
    @Column(name = "class_time", length = 20)
    private String classTime;
    
    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private User teacher;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Course() {}
    
    public Course(String courseName, String teacherId, String teacherName, 
                  String className, String classroom, String classTime, 
                  LocalDate classDate) {
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.className = className;
        this.classroom = classroom;
        this.classTime = classTime;
        this.classDate = classDate;
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
    
    public User getTeacher() {
        return teacher;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 