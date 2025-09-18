package com.example.pexitong2.dto.teaching;

import com.example.pexitong2.entity.teaching.TeacherAttendanceStatistics;

import java.math.BigDecimal;

/**
 * 教学统计响应DTO
 */
public class TeachingStatisticsResponse {
    
    private Integer totalCourses;
    private Integer todayAttendance;
    private BigDecimal attendanceRate;
    private Integer totalTeachers;
    private Integer onTimeCount;
    private Integer lateCount;
    private Integer missedCount;
    
    // 构造函数
    public TeachingStatisticsResponse() {}
    
    public TeachingStatisticsResponse(TeacherAttendanceStatistics statistics) {
        this.totalCourses = statistics.getTotalCourses();
        this.todayAttendance = statistics.getCompletedCount() + statistics.getLateCount();
        this.attendanceRate = statistics.getAttendanceRate();
        this.totalTeachers = statistics.getTotalTeachers();
        this.onTimeCount = statistics.getCompletedCount();
        this.lateCount = statistics.getLateCount();
        this.missedCount = statistics.getPendingCount() + statistics.getMissedCount();
    }
    
    // 手动构建统计数据的构造函数
    public TeachingStatisticsResponse(int totalCourses, int completedCount, int lateCount, 
                                      int pendingCount, int missedCount, int totalTeachers) {
        this.totalCourses = totalCourses;
        this.todayAttendance = completedCount + lateCount;
        this.totalTeachers = totalTeachers;
        this.onTimeCount = completedCount;
        this.lateCount = lateCount;
        this.missedCount = pendingCount + missedCount;
        
        // 计算签到率
        if (totalCourses > 0) {
            this.attendanceRate = BigDecimal.valueOf((double)(completedCount + lateCount) / totalCourses * 100)
                    .setScale(1, java.math.RoundingMode.HALF_UP);
        } else {
            this.attendanceRate = BigDecimal.ZERO;
        }
    }
    
    // Getter 和 Setter
    public Integer getTotalCourses() {
        return totalCourses;
    }
    
    public void setTotalCourses(Integer totalCourses) {
        this.totalCourses = totalCourses;
    }
    
    public Integer getTodayAttendance() {
        return todayAttendance;
    }
    
    public void setTodayAttendance(Integer todayAttendance) {
        this.todayAttendance = todayAttendance;
    }
    
    public BigDecimal getAttendanceRate() {
        return attendanceRate;
    }
    
    public void setAttendanceRate(BigDecimal attendanceRate) {
        this.attendanceRate = attendanceRate;
    }
    
    public Integer getTotalTeachers() {
        return totalTeachers;
    }
    
    public void setTotalTeachers(Integer totalTeachers) {
        this.totalTeachers = totalTeachers;
    }
    
    public Integer getOnTimeCount() {
        return onTimeCount;
    }
    
    public void setOnTimeCount(Integer onTimeCount) {
        this.onTimeCount = onTimeCount;
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
}
