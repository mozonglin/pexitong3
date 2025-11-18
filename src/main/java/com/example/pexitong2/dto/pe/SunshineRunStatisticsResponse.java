package com.example.pexitong2.dto.pe;

import java.util.List;

/**
 * 阳光跑统计响应
 */
public class SunshineRunStatisticsResponse {
    
    private String school;                      // 学校名称
    private String scope;                       // 统计范围（"全校" 或 院系名称）
    private Integer totalStudents;              // 学生总数
    private SunshineRunAggregate overall;       // 整体统计
    private List<GroupStatistics> groupStats;   // 分组统计（院系或班级）
    private List<GroupRanking> groupRankings;   // 分组排名（院系或班级）
    
    // 内部类：阳光跑汇总数据
    public static class SunshineRunAggregate {
        private Long totalRuns;         // 总跑步次数
        private Long totalDistance;     // 总跑步距离（米）
        private Long totalDuration;     // 总跑步时长（秒）
        private Double avgRunsPerStudent;       // 人均跑步次数
        private Double avgDistancePerStudent;   // 人均跑步距离（米）
        private Double avgDurationPerStudent;   // 人均跑步时长（秒）
        
        public SunshineRunAggregate() {}
        
        public SunshineRunAggregate(Long totalRuns, Long totalDistance, Long totalDuration, 
                                   Double avgRunsPerStudent, Double avgDistancePerStudent, Double avgDurationPerStudent) {
            this.totalRuns = totalRuns;
            this.totalDistance = totalDistance;
            this.totalDuration = totalDuration;
            this.avgRunsPerStudent = avgRunsPerStudent;
            this.avgDistancePerStudent = avgDistancePerStudent;
            this.avgDurationPerStudent = avgDurationPerStudent;
        }
        
        // Getters and Setters
        public Long getTotalRuns() { return totalRuns; }
        public void setTotalRuns(Long totalRuns) { this.totalRuns = totalRuns; }
        
        public Long getTotalDistance() { return totalDistance; }
        public void setTotalDistance(Long totalDistance) { this.totalDistance = totalDistance; }
        
        public Long getTotalDuration() { return totalDuration; }
        public void setTotalDuration(Long totalDuration) { this.totalDuration = totalDuration; }
        
        public Double getAvgRunsPerStudent() { return avgRunsPerStudent; }
        public void setAvgRunsPerStudent(Double avgRunsPerStudent) { this.avgRunsPerStudent = avgRunsPerStudent; }
        
        public Double getAvgDistancePerStudent() { return avgDistancePerStudent; }
        public void setAvgDistancePerStudent(Double avgDistancePerStudent) { this.avgDistancePerStudent = avgDistancePerStudent; }
        
        public Double getAvgDurationPerStudent() { return avgDurationPerStudent; }
        public void setAvgDurationPerStudent(Double avgDurationPerStudent) { this.avgDurationPerStudent = avgDurationPerStudent; }
    }
    
    // 内部类：分组统计（院系或班级）
    public static class GroupStatistics {
        private String groupName;                   // 组名（院系名或班级名）
        private Integer studentCount;               // 学生数量
        private SunshineRunAggregate aggregate;     // 该组的阳光跑汇总数据
        
        public GroupStatistics() {}
        
        public GroupStatistics(String groupName, Integer studentCount, SunshineRunAggregate aggregate) {
            this.groupName = groupName;
            this.studentCount = studentCount;
            this.aggregate = aggregate;
        }
        
        // Getters and Setters
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public Integer getStudentCount() { return studentCount; }
        public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
        
        public SunshineRunAggregate getAggregate() { return aggregate; }
        public void setAggregate(SunshineRunAggregate aggregate) { this.aggregate = aggregate; }
    }
    
    // 内部类：分组排名（院系或班级）
    public static class GroupRanking {
        private Integer rank;                       // 排名
        private String groupName;                   // 组名（院系名或班级名）
        private Double avgDistancePerStudent;       // 人均跑步距离（用于排名）
        private SunshineRunAggregate aggregate;     // 该组的阳光跑汇总数据
        
        public GroupRanking() {}
        
        public GroupRanking(Integer rank, String groupName, Double avgDistancePerStudent, SunshineRunAggregate aggregate) {
            this.rank = rank;
            this.groupName = groupName;
            this.avgDistancePerStudent = avgDistancePerStudent;
            this.aggregate = aggregate;
        }
        
        // Getters and Setters
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public Double getAvgDistancePerStudent() { return avgDistancePerStudent; }
        public void setAvgDistancePerStudent(Double avgDistancePerStudent) { this.avgDistancePerStudent = avgDistancePerStudent; }
        
        public SunshineRunAggregate getAggregate() { return aggregate; }
        public void setAggregate(SunshineRunAggregate aggregate) { this.aggregate = aggregate; }
    }
    
    // 构造函数
    public SunshineRunStatisticsResponse() {}
    
    // Getters and Setters
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    
    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
    
    public SunshineRunAggregate getOverall() { return overall; }
    public void setOverall(SunshineRunAggregate overall) { this.overall = overall; }
    
    public List<GroupStatistics> getGroupStats() { return groupStats; }
    public void setGroupStats(List<GroupStatistics> groupStats) { this.groupStats = groupStats; }
    
    public List<GroupRanking> getGroupRankings() { return groupRankings; }
    public void setGroupRankings(List<GroupRanking> groupRankings) { this.groupRankings = groupRankings; }
}

