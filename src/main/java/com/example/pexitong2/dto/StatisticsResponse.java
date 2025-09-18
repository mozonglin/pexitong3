package com.example.pexitong2.dto;

import java.util.List;

public class StatisticsResponse {
    
    private Long totalObservations;
    private Long thisMonthCount;
    private Long teacherCount;
    private Long evaluationFiles;
    private Long videoFiles;
    private List<DepartmentStatistics> departmentStats;
    
    // 构造函数
    public StatisticsResponse() {}
    
    // Getters and Setters
    public Long getTotalObservations() {
        return totalObservations;
    }
    
    public void setTotalObservations(Long totalObservations) {
        this.totalObservations = totalObservations;
    }
    
    public Long getThisMonthCount() {
        return thisMonthCount;
    }
    
    public void setThisMonthCount(Long thisMonthCount) {
        this.thisMonthCount = thisMonthCount;
    }
    
    public Long getTeacherCount() {
        return teacherCount;
    }
    
    public void setTeacherCount(Long teacherCount) {
        this.teacherCount = teacherCount;
    }
    
    public Long getEvaluationFiles() {
        return evaluationFiles;
    }
    
    public void setEvaluationFiles(Long evaluationFiles) {
        this.evaluationFiles = evaluationFiles;
    }
    
    public Long getVideoFiles() {
        return videoFiles;
    }
    
    public void setVideoFiles(Long videoFiles) {
        this.videoFiles = videoFiles;
    }
    
    public List<DepartmentStatistics> getDepartmentStats() {
        return departmentStats;
    }
    
    public void setDepartmentStats(List<DepartmentStatistics> departmentStats) {
        this.departmentStats = departmentStats;
    }
    
    // 内部类：院系统计
    public static class DepartmentStatistics {
        private Long departmentId;
        private String departmentName;
        private Long observationCount;
        private Long teacherCount;
        private Long evaluationFiles;
        private Long videoFiles;
        
        // 构造函数
        public DepartmentStatistics() {}
        
        public DepartmentStatistics(Long departmentId, String departmentName, 
                                   Long observationCount, Long teacherCount, 
                                   Long evaluationFiles, Long videoFiles) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.observationCount = observationCount;
            this.teacherCount = teacherCount;
            this.evaluationFiles = evaluationFiles;
            this.videoFiles = videoFiles;
        }
        
        // Getters and Setters
        public Long getDepartmentId() {
            return departmentId;
        }
        
        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }
        
        public String getDepartmentName() {
            return departmentName;
        }
        
        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }
        
        public Long getObservationCount() {
            return observationCount;
        }
        
        public void setObservationCount(Long observationCount) {
            this.observationCount = observationCount;
        }
        
        public Long getTeacherCount() {
            return teacherCount;
        }
        
        public void setTeacherCount(Long teacherCount) {
            this.teacherCount = teacherCount;
        }
        
        public Long getEvaluationFiles() {
            return evaluationFiles;
        }
        
        public void setEvaluationFiles(Long evaluationFiles) {
            this.evaluationFiles = evaluationFiles;
        }
        
        public Long getVideoFiles() {
            return videoFiles;
        }
        
        public void setVideoFiles(Long videoFiles) {
            this.videoFiles = videoFiles;
        }
    }
} 