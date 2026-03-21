package com.example.pexitong2.dto.training;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TrainingDTO {

    // ========== 体能档案相关 ==========

    /** 体测数据上传请求 */
    public static class TiceDataRequest {
        private Integer vitalCapacity;
        private BigDecimal sitAndReach;
        private BigDecimal standingLongJump;
        private Integer sitUps;
        private BigDecimal sprint50m;
        private Integer longRun;
        private Integer pullUps;
        private BigDecimal bmi;
        private BigDecimal height;
        private BigDecimal weight;
        private BigDecimal ticeTotalScore;
        private String ticeGrade;

        // Getters and Setters
        public Integer getVitalCapacity() { return vitalCapacity; }
        public void setVitalCapacity(Integer vitalCapacity) { this.vitalCapacity = vitalCapacity; }
        public BigDecimal getSitAndReach() { return sitAndReach; }
        public void setSitAndReach(BigDecimal sitAndReach) { this.sitAndReach = sitAndReach; }
        public BigDecimal getStandingLongJump() { return standingLongJump; }
        public void setStandingLongJump(BigDecimal standingLongJump) { this.standingLongJump = standingLongJump; }
        public Integer getSitUps() { return sitUps; }
        public void setSitUps(Integer sitUps) { this.sitUps = sitUps; }
        public BigDecimal getSprint50m() { return sprint50m; }
        public void setSprint50m(BigDecimal sprint50m) { this.sprint50m = sprint50m; }
        public Integer getLongRun() { return longRun; }
        public void setLongRun(Integer longRun) { this.longRun = longRun; }
        public Integer getPullUps() { return pullUps; }
        public void setPullUps(Integer pullUps) { this.pullUps = pullUps; }
        public BigDecimal getBmi() { return bmi; }
        public void setBmi(BigDecimal bmi) { this.bmi = bmi; }
        public BigDecimal getHeight() { return height; }
        public void setHeight(BigDecimal height) { this.height = height; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        public BigDecimal getTiceTotalScore() { return ticeTotalScore; }
        public void setTiceTotalScore(BigDecimal ticeTotalScore) { this.ticeTotalScore = ticeTotalScore; }
        public String getTiceGrade() { return ticeGrade; }
        public void setTiceGrade(String ticeGrade) { this.ticeGrade = ticeGrade; }
    }

    /** 体能档案响应 */
    public static class FitnessProfileResponse {
        private Long id;
        private String userId;
        private String studentId;
        private String realName;
        private String className;
        private String departmentName;
        // 体测
        private Map<String, Object> ticeData;
        // 阳光跑
        private Map<String, Object> runData;
        // 课后作业
        private Map<String, Object> homeworkData;
        // PE积分
        private Map<String, Object> pePointsData;
        // AI分析
        private String aiFitnessLevel;
        private List<String> aiStrengths;
        private List<String> aiWeaknesses;
        private String aiRecommendation;
        private LocalDateTime aiAnalyzedAt;
        private LocalDateTime updatedAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public Map<String, Object> getTiceData() { return ticeData; }
        public void setTiceData(Map<String, Object> ticeData) { this.ticeData = ticeData; }
        public Map<String, Object> getRunData() { return runData; }
        public void setRunData(Map<String, Object> runData) { this.runData = runData; }
        public Map<String, Object> getHomeworkData() { return homeworkData; }
        public void setHomeworkData(Map<String, Object> homeworkData) { this.homeworkData = homeworkData; }
        public Map<String, Object> getPePointsData() { return pePointsData; }
        public void setPePointsData(Map<String, Object> pePointsData) { this.pePointsData = pePointsData; }
        public String getAiFitnessLevel() { return aiFitnessLevel; }
        public void setAiFitnessLevel(String aiFitnessLevel) { this.aiFitnessLevel = aiFitnessLevel; }
        public List<String> getAiStrengths() { return aiStrengths; }
        public void setAiStrengths(List<String> aiStrengths) { this.aiStrengths = aiStrengths; }
        public List<String> getAiWeaknesses() { return aiWeaknesses; }
        public void setAiWeaknesses(List<String> aiWeaknesses) { this.aiWeaknesses = aiWeaknesses; }
        public String getAiRecommendation() { return aiRecommendation; }
        public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }
        public LocalDateTime getAiAnalyzedAt() { return aiAnalyzedAt; }
        public void setAiAnalyzedAt(LocalDateTime aiAnalyzedAt) { this.aiAnalyzedAt = aiAnalyzedAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    // ========== 训练任务相关 ==========

    /** 创建训练任务请求 */
    public static class CreateTaskRequest {
        private String title;
        private String description;
        private String trainingType; // endurance/strength/flexibility/skill/comprehensive
        private String difficulty;   // easy/medium/hard
        private List<String> targetClasses;
        private Long courseId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String requirements; // JSON
        private boolean useAiReference;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTrainingType() { return trainingType; }
        public void setTrainingType(String trainingType) { this.trainingType = trainingType; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public List<String> getTargetClasses() { return targetClasses; }
        public void setTargetClasses(List<String> targetClasses) { this.targetClasses = targetClasses; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getRequirements() { return requirements; }
        public void setRequirements(String requirements) { this.requirements = requirements; }
        public boolean isUseAiReference() { return useAiReference; }
        public void setUseAiReference(boolean useAiReference) { this.useAiReference = useAiReference; }
    }

    /** 训练任务响应 */
    public static class TaskResponse {
        private Long id;
        private String teacherId;
        private String teacherName;
        private String title;
        private String description;
        private String trainingType;
        private String difficulty;
        private List<String> targetClasses;
        private Long courseId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String requirements;
        private Boolean aiGenerated;
        private String aiReference;
        private String status;
        private Integer totalStudents;
        private Integer completedStudents;
        private Double completionRate;
        private LocalDateTime createdAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTeacherId() { return teacherId; }
        public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTrainingType() { return trainingType; }
        public void setTrainingType(String trainingType) { this.trainingType = trainingType; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public List<String> getTargetClasses() { return targetClasses; }
        public void setTargetClasses(List<String> targetClasses) { this.targetClasses = targetClasses; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getRequirements() { return requirements; }
        public void setRequirements(String requirements) { this.requirements = requirements; }
        public Boolean getAiGenerated() { return aiGenerated; }
        public void setAiGenerated(Boolean aiGenerated) { this.aiGenerated = aiGenerated; }
        public String getAiReference() { return aiReference; }
        public void setAiReference(String aiReference) { this.aiReference = aiReference; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getTotalStudents() { return totalStudents; }
        public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
        public Integer getCompletedStudents() { return completedStudents; }
        public void setCompletedStudents(Integer completedStudents) { this.completedStudents = completedStudents; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /** AI建档分析请求 */
    public static class AiAnalysisRequest {
        private String studentId; // 可选，不传则分析全班
        private String className; // 可选

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
    }

    /** AI任务建议请求 */
    public static class AiTaskSuggestionRequest {
        private List<String> targetClasses;
        private String trainingType;
        private Long courseId;

        public List<String> getTargetClasses() { return targetClasses; }
        public void setTargetClasses(List<String> targetClasses) { this.targetClasses = targetClasses; }
        public String getTrainingType() { return trainingType; }
        public void setTrainingType(String trainingType) { this.trainingType = trainingType; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
    }

    /** 班级体能概览 */
    public static class ClassFitnessOverview {
        private String className;
        private int totalStudents;
        private int profiledStudents;
        private Map<String, Integer> fitnessLevelDistribution; // A:5, B:10, C:8...
        private Map<String, Double> avgMetrics; // 各项平均值
        private List<String> commonWeaknesses;

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public int getProfiledStudents() { return profiledStudents; }
        public void setProfiledStudents(int profiledStudents) { this.profiledStudents = profiledStudents; }
        public Map<String, Integer> getFitnessLevelDistribution() { return fitnessLevelDistribution; }
        public void setFitnessLevelDistribution(Map<String, Integer> fitnessLevelDistribution) { this.fitnessLevelDistribution = fitnessLevelDistribution; }
        public Map<String, Double> getAvgMetrics() { return avgMetrics; }
        public void setAvgMetrics(Map<String, Double> avgMetrics) { this.avgMetrics = avgMetrics; }
        public List<String> getCommonWeaknesses() { return commonWeaknesses; }
        public void setCommonWeaknesses(List<String> commonWeaknesses) { this.commonWeaknesses = commonWeaknesses; }
    }
}
