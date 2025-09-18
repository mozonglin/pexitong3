package com.example.pexitong2.entity.teaching;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_attendance_statistics")
public class TeacherAttendanceStatistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type", nullable = false)
    private StatType statType;
    
    @Column(name = "school", nullable = false, length = 100)
    private String school;
    
    // 统计数据
    @Column(name = "total_courses")
    private Integer totalCourses = 0;
    
    @Column(name = "completed_count")
    private Integer completedCount = 0;
    
    @Column(name = "pending_count")
    private Integer pendingCount = 0;
    
    @Column(name = "late_count")
    private Integer lateCount = 0;
    
    @Column(name = "missed_count")
    private Integer missedCount = 0;
    
    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate = BigDecimal.ZERO;
    
    @Column(name = "on_time_rate", precision = 5, scale = 2)
    private BigDecimal onTimeRate = BigDecimal.ZERO;
    
    // 教师数据
    @Column(name = "total_teachers")
    private Integer totalTeachers = 0;
    
    @Column(name = "active_teachers")
    private Integer activeTeachers = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 枚举类型
    public enum StatType {
        day("日统计"),
        week("周统计"),
        month("月统计");
        
        private final String description;
        
        StatType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public TeacherAttendanceStatistics() {}
    
    public TeacherAttendanceStatistics(LocalDate statDate, StatType statType, String school) {
        this.statDate = statDate;
        this.statType = statType;
        this.school = school;
    }
    
    // Getter 和 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getStatDate() {
        return statDate;
    }
    
    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }
    
    public StatType getStatType() {
        return statType;
    }
    
    public void setStatType(StatType statType) {
        this.statType = statType;
    }
    
    public String getSchool() {
        return school;
    }
    
    public void setSchool(String school) {
        this.school = school;
    }
    
    public Integer getTotalCourses() {
        return totalCourses;
    }
    
    public void setTotalCourses(Integer totalCourses) {
        this.totalCourses = totalCourses;
    }
    
    public Integer getCompletedCount() {
        return completedCount;
    }
    
    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }
    
    public Integer getPendingCount() {
        return pendingCount;
    }
    
    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }
    
    public Integer getLateCount() {
        return lateCount;
    }
    
    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }
    
    public Integer getMissedCount() {
        return missedCount;
    }
    
    public void setMissedCount(Integer missedCount) {
        this.missedCount = missedCount;
    }
    
    public BigDecimal getAttendanceRate() {
        return attendanceRate;
    }
    
    public void setAttendanceRate(BigDecimal attendanceRate) {
        this.attendanceRate = attendanceRate;
    }
    
    public BigDecimal getOnTimeRate() {
        return onTimeRate;
    }
    
    public void setOnTimeRate(BigDecimal onTimeRate) {
        this.onTimeRate = onTimeRate;
    }
    
    public Integer getTotalTeachers() {
        return totalTeachers;
    }
    
    public void setTotalTeachers(Integer totalTeachers) {
        this.totalTeachers = totalTeachers;
    }
    
    public Integer getActiveTeachers() {
        return activeTeachers;
    }
    
    public void setActiveTeachers(Integer activeTeachers) {
        this.activeTeachers = activeTeachers;
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
