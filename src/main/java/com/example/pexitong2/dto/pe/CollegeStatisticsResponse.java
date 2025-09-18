package com.example.pexitong2.dto.pe;

import java.util.List;

/**
 * 院级管理员统计响应
 */
public class CollegeStatisticsResponse {
    
    private String school;                          // 学校名称
    private String college;                         // 院系名称
    private Integer totalStudents;                  // 本院学生总数
    private SchoolStatisticsResponse.PeTargets targets;  // PE积分指标（来自校级管理员设置）
    private SchoolStatisticsResponse.ComplianceRates overallCompliance;  // 本院整体达标率
    private List<ClassStatistics> classStats;       // 各班统计
    private List<ClassRanking> classRankings;       // 班级排名
    
    // 内部类：班级统计
    public static class ClassStatistics {
        private String className;                   // 班级名称
        private Integer studentCount;               // 学生数量
        private SchoolStatisticsResponse.ComplianceRates complianceRates;  // 达标率
        
        public ClassStatistics() {}
        
        public ClassStatistics(String className, Integer studentCount, SchoolStatisticsResponse.ComplianceRates complianceRates) {
            this.className = className;
            this.studentCount = studentCount;
            this.complianceRates = complianceRates;
        }
        
        // Getters and Setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public Integer getStudentCount() { return studentCount; }
        public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
        
        public SchoolStatisticsResponse.ComplianceRates getComplianceRates() { return complianceRates; }
        public void setComplianceRates(SchoolStatisticsResponse.ComplianceRates complianceRates) { this.complianceRates = complianceRates; }
    }
    
    // 内部类：班级排名
    public static class ClassRanking {
        private Integer rank;                       // 排名
        private String className;                   // 班级名称
        private Double overallComplianceRate;       // 综合达标率（取三项平均）
        private SchoolStatisticsResponse.ComplianceRates complianceRates;  // 详细达标率
        
        public ClassRanking() {}
        
        public ClassRanking(Integer rank, String className, Double overallComplianceRate, SchoolStatisticsResponse.ComplianceRates complianceRates) {
            this.rank = rank;
            this.className = className;
            this.overallComplianceRate = overallComplianceRate;
            this.complianceRates = complianceRates;
        }
        
        // Getters and Setters
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public Double getOverallComplianceRate() { return overallComplianceRate; }
        public void setOverallComplianceRate(Double overallComplianceRate) { this.overallComplianceRate = overallComplianceRate; }
        
        public SchoolStatisticsResponse.ComplianceRates getComplianceRates() { return complianceRates; }
        public void setComplianceRates(SchoolStatisticsResponse.ComplianceRates complianceRates) { this.complianceRates = complianceRates; }
    }
    
    // 构造函数
    public CollegeStatisticsResponse() {}
    
    // Getters and Setters
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    
    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
    
    public SchoolStatisticsResponse.PeTargets getTargets() { return targets; }
    public void setTargets(SchoolStatisticsResponse.PeTargets targets) { this.targets = targets; }
    
    public SchoolStatisticsResponse.ComplianceRates getOverallCompliance() { return overallCompliance; }
    public void setOverallCompliance(SchoolStatisticsResponse.ComplianceRates overallCompliance) { this.overallCompliance = overallCompliance; }
    
    public List<ClassStatistics> getClassStats() { return classStats; }
    public void setClassStats(List<ClassStatistics> classStats) { this.classStats = classStats; }
    
    public List<ClassRanking> getClassRankings() { return classRankings; }
    public void setClassRankings(List<ClassRanking> classRankings) { this.classRankings = classRankings; }
}


