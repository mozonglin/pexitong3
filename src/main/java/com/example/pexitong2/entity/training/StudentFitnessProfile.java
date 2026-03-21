package com.example.pexitong2.entity.training;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_fitness_profiles")
public class StudentFitnessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 50, unique = true)
    private String userId;

    @Column(name = "student_id", nullable = false, length = 30, unique = true)
    private String studentId;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    // 体测数据
    @Column(name = "vital_capacity")
    private Integer vitalCapacity;

    @Column(name = "sit_and_reach", precision = 5, scale = 1)
    private BigDecimal sitAndReach;

    @Column(name = "standing_long_jump", precision = 5, scale = 1)
    private BigDecimal standingLongJump;

    @Column(name = "sit_ups")
    private Integer sitUps;

    @Column(name = "sprint_50m", precision = 5, scale = 2)
    private BigDecimal sprint50m;

    @Column(name = "long_run")
    private Integer longRun;

    @Column(name = "pull_ups")
    private Integer pullUps;

    @Column(precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(precision = 5, scale = 1)
    private BigDecimal height;

    @Column(precision = 5, scale = 1)
    private BigDecimal weight;

    @Column(name = "tice_total_score", precision = 5, scale = 1)
    private BigDecimal ticeTotalScore;

    @Column(name = "tice_grade", length = 20)
    private String ticeGrade;

    @Column(name = "tice_updated_at")
    private LocalDateTime ticeUpdatedAt;

    // 阳光跑汇总
    @Column(name = "run_total_count")
    private Integer runTotalCount = 0;

    @Column(name = "run_total_distance")
    private Double runTotalDistance = 0.0;

    @Column(name = "run_avg_pace")
    private Double runAvgPace;

    @Column(name = "run_best_pace")
    private Double runBestPace;

    @Column(name = "run_updated_at")
    private LocalDateTime runUpdatedAt;

    // 课后作业汇总
    @Column(name = "homework_total_count")
    private Integer homeworkTotalCount = 0;

    @Column(name = "homework_squat_best")
    private Integer homeworkSquatBest;

    @Column(name = "homework_situp_best")
    private Integer homeworkSitupBest;

    @Column(name = "homework_pushup_best")
    private Integer homeworkPushupBest;

    @Column(name = "homework_pullup_best")
    private Integer homeworkPullupBest;

    @Column(name = "homework_jumprope_best")
    private Integer homeworkJumpropeBest;

    @Column(name = "homework_updated_at")
    private LocalDateTime homeworkUpdatedAt;

    // PE积分汇总
    @Column(name = "pe_total_points")
    private Integer peTotalPoints = 0;

    @Column(name = "pe_activity_points")
    private Integer peActivityPoints = 0;

    @Column(name = "pe_morning_points")
    private Integer peMorningPoints = 0;

    @Column(name = "pe_points_updated_at")
    private LocalDateTime pePointsUpdatedAt;

    // AI分析结果
    @Column(name = "ai_fitness_level", length = 20)
    private String aiFitnessLevel;

    @Column(name = "ai_strengths", columnDefinition = "TEXT")
    private String aiStrengths;

    @Column(name = "ai_weaknesses", columnDefinition = "TEXT")
    private String aiWeaknesses;

    @Column(name = "ai_recommendation", columnDefinition = "TEXT")
    private String aiRecommendation;

    @Column(name = "ai_analyzed_at")
    private LocalDateTime aiAnalyzedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentFitnessProfile() {}

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

    public LocalDateTime getTiceUpdatedAt() { return ticeUpdatedAt; }
    public void setTiceUpdatedAt(LocalDateTime ticeUpdatedAt) { this.ticeUpdatedAt = ticeUpdatedAt; }

    public Integer getRunTotalCount() { return runTotalCount; }
    public void setRunTotalCount(Integer runTotalCount) { this.runTotalCount = runTotalCount; }

    public Double getRunTotalDistance() { return runTotalDistance; }
    public void setRunTotalDistance(Double runTotalDistance) { this.runTotalDistance = runTotalDistance; }

    public Double getRunAvgPace() { return runAvgPace; }
    public void setRunAvgPace(Double runAvgPace) { this.runAvgPace = runAvgPace; }

    public Double getRunBestPace() { return runBestPace; }
    public void setRunBestPace(Double runBestPace) { this.runBestPace = runBestPace; }

    public LocalDateTime getRunUpdatedAt() { return runUpdatedAt; }
    public void setRunUpdatedAt(LocalDateTime runUpdatedAt) { this.runUpdatedAt = runUpdatedAt; }

    public Integer getHomeworkTotalCount() { return homeworkTotalCount; }
    public void setHomeworkTotalCount(Integer homeworkTotalCount) { this.homeworkTotalCount = homeworkTotalCount; }

    public Integer getHomeworkSquatBest() { return homeworkSquatBest; }
    public void setHomeworkSquatBest(Integer homeworkSquatBest) { this.homeworkSquatBest = homeworkSquatBest; }

    public Integer getHomeworkSitupBest() { return homeworkSitupBest; }
    public void setHomeworkSitupBest(Integer homeworkSitupBest) { this.homeworkSitupBest = homeworkSitupBest; }

    public Integer getHomeworkPushupBest() { return homeworkPushupBest; }
    public void setHomeworkPushupBest(Integer homeworkPushupBest) { this.homeworkPushupBest = homeworkPushupBest; }

    public Integer getHomeworkPullupBest() { return homeworkPullupBest; }
    public void setHomeworkPullupBest(Integer homeworkPullupBest) { this.homeworkPullupBest = homeworkPullupBest; }

    public Integer getHomeworkJumpropeBest() { return homeworkJumpropeBest; }
    public void setHomeworkJumpropeBest(Integer homeworkJumpropeBest) { this.homeworkJumpropeBest = homeworkJumpropeBest; }

    public LocalDateTime getHomeworkUpdatedAt() { return homeworkUpdatedAt; }
    public void setHomeworkUpdatedAt(LocalDateTime homeworkUpdatedAt) { this.homeworkUpdatedAt = homeworkUpdatedAt; }

    public Integer getPeTotalPoints() { return peTotalPoints; }
    public void setPeTotalPoints(Integer peTotalPoints) { this.peTotalPoints = peTotalPoints; }

    public Integer getPeActivityPoints() { return peActivityPoints; }
    public void setPeActivityPoints(Integer peActivityPoints) { this.peActivityPoints = peActivityPoints; }

    public Integer getPeMorningPoints() { return peMorningPoints; }
    public void setPeMorningPoints(Integer peMorningPoints) { this.peMorningPoints = peMorningPoints; }

    public LocalDateTime getPePointsUpdatedAt() { return pePointsUpdatedAt; }
    public void setPePointsUpdatedAt(LocalDateTime pePointsUpdatedAt) { this.pePointsUpdatedAt = pePointsUpdatedAt; }

    public String getAiFitnessLevel() { return aiFitnessLevel; }
    public void setAiFitnessLevel(String aiFitnessLevel) { this.aiFitnessLevel = aiFitnessLevel; }

    public String getAiStrengths() { return aiStrengths; }
    public void setAiStrengths(String aiStrengths) { this.aiStrengths = aiStrengths; }

    public String getAiWeaknesses() { return aiWeaknesses; }
    public void setAiWeaknesses(String aiWeaknesses) { this.aiWeaknesses = aiWeaknesses; }

    public String getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }

    public LocalDateTime getAiAnalyzedAt() { return aiAnalyzedAt; }
    public void setAiAnalyzedAt(LocalDateTime aiAnalyzedAt) { this.aiAnalyzedAt = aiAnalyzedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
