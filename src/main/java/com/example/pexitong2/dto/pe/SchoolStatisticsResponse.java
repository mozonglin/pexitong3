package com.example.pexitong2.dto.pe;

import java.util.List;

/**
 * 校级管理员统计响应
 */
public class SchoolStatisticsResponse {
    
    private String school;                          // 学校名称
    private Integer totalStudents;                  // 学生总数
    private PeTargets targets;                      // PE积分指标
    private ComplianceRates overallCompliance;      // 整体达标率
    private List<CollegeStatistics> collegeStats;   // 各院系统计
    private List<CollegeRanking> collegeRankings;   // 院系排名
    
    // 内部类：PE积分指标
    public static class PeTargets {
        private Integer weeklyTarget;
        private Integer monthlyTarget;
        private Integer totalTarget;
        
        public PeTargets() {}
        
        public PeTargets(Integer weeklyTarget, Integer monthlyTarget, Integer totalTarget) {
            this.weeklyTarget = weeklyTarget;
            this.monthlyTarget = monthlyTarget;
            this.totalTarget = totalTarget;
        }
        
        // Getters and Setters
        public Integer getWeeklyTarget() { return weeklyTarget; }
        public void setWeeklyTarget(Integer weeklyTarget) { this.weeklyTarget = weeklyTarget; }
        
        public Integer getMonthlyTarget() { return monthlyTarget; }
        public void setMonthlyTarget(Integer monthlyTarget) { this.monthlyTarget = monthlyTarget; }
        
        public Integer getTotalTarget() { return totalTarget; }
        public void setTotalTarget(Integer totalTarget) { this.totalTarget = totalTarget; }
    }
    
    // 内部类：达标率
    public static class ComplianceRates {
        private Double weeklyComplianceRate;    // 周达标率
        private Double monthlyComplianceRate;   // 月达标率
        private Double totalComplianceRate;     // 总达标率
        
        public ComplianceRates() {}
        
        public ComplianceRates(Double weeklyComplianceRate, Double monthlyComplianceRate, Double totalComplianceRate) {
            this.weeklyComplianceRate = weeklyComplianceRate;
            this.monthlyComplianceRate = monthlyComplianceRate;
            this.totalComplianceRate = totalComplianceRate;
        }
        
        // Getters and Setters
        public Double getWeeklyComplianceRate() { return weeklyComplianceRate; }
        public void setWeeklyComplianceRate(Double weeklyComplianceRate) { this.weeklyComplianceRate = weeklyComplianceRate; }
        
        public Double getMonthlyComplianceRate() { return monthlyComplianceRate; }
        public void setMonthlyComplianceRate(Double monthlyComplianceRate) { this.monthlyComplianceRate = monthlyComplianceRate; }
        
        public Double getTotalComplianceRate() { return totalComplianceRate; }
        public void setTotalComplianceRate(Double totalComplianceRate) { this.totalComplianceRate = totalComplianceRate; }
    }
    
    // 内部类：院系统计
    public static class CollegeStatistics {
        private String collegeName;                 // 院系名称
        private Integer studentCount;               // 学生数量
        private ComplianceRates complianceRates;    // 达标率
        
        public CollegeStatistics() {}
        
        public CollegeStatistics(String collegeName, Integer studentCount, ComplianceRates complianceRates) {
            this.collegeName = collegeName;
            this.studentCount = studentCount;
            this.complianceRates = complianceRates;
        }
        
        // Getters and Setters
        public String getCollegeName() { return collegeName; }
        public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
        
        public Integer getStudentCount() { return studentCount; }
        public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
        
        public ComplianceRates getComplianceRates() { return complianceRates; }
        public void setComplianceRates(ComplianceRates complianceRates) { this.complianceRates = complianceRates; }
    }
    
    // 内部类：院系排名
    public static class CollegeRanking {
        private Integer rank;                       // 排名
        private String collegeName;                 // 院系名称
        private Double overallComplianceRate;       // 综合达标率（取三项平均）
        private ComplianceRates complianceRates;    // 详细达标率
        
        public CollegeRanking() {}
        
        public CollegeRanking(Integer rank, String collegeName, Double overallComplianceRate, ComplianceRates complianceRates) {
            this.rank = rank;
            this.collegeName = collegeName;
            this.overallComplianceRate = overallComplianceRate;
            this.complianceRates = complianceRates;
        }
        
        // Getters and Setters
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        
        public String getCollegeName() { return collegeName; }
        public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
        
        public Double getOverallComplianceRate() { return overallComplianceRate; }
        public void setOverallComplianceRate(Double overallComplianceRate) { this.overallComplianceRate = overallComplianceRate; }
        
        public ComplianceRates getComplianceRates() { return complianceRates; }
        public void setComplianceRates(ComplianceRates complianceRates) { this.complianceRates = complianceRates; }
    }
    
    // 构造函数
    public SchoolStatisticsResponse() {}
    
    // Getters and Setters
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
    
    public PeTargets getTargets() { return targets; }
    public void setTargets(PeTargets targets) { this.targets = targets; }
    
    public ComplianceRates getOverallCompliance() { return overallCompliance; }
    public void setOverallCompliance(ComplianceRates overallCompliance) { this.overallCompliance = overallCompliance; }
    
    public List<CollegeStatistics> getCollegeStats() { return collegeStats; }
    public void setCollegeStats(List<CollegeStatistics> collegeStats) { this.collegeStats = collegeStats; }
    
    public List<CollegeRanking> getCollegeRankings() { return collegeRankings; }
    public void setCollegeRankings(List<CollegeRanking> collegeRankings) { this.collegeRankings = collegeRankings; }
}


