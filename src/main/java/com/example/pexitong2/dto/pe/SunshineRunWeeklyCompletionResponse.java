package com.example.pexitong2.dto.pe;

/**
 * 管理员查看「本周」阳光跑达标率（按学校设置的每周次数指标）。
 */
public class SunshineRunWeeklyCompletionResponse {

    private int requiredRunsPerWeek;
    private int totalWeeksInPlan;
    private String weekStartDate;
    private String weekEndDate;
    /** 如：全校、某院系、辅导员管辖班级 */
    private String scopeDescription;
    private int totalStudents;
    /** 当周有效跑步次数达到要求的学生数；未配置指标时为 null */
    private Integer compliantStudents;
    /** 完成率 0–100；未配置指标时为 null */
    private Double completionRatePercent;
    private boolean requirementConfigured;

    public SunshineRunWeeklyCompletionResponse() {}

    public int getRequiredRunsPerWeek() { return requiredRunsPerWeek; }
    public void setRequiredRunsPerWeek(int requiredRunsPerWeek) { this.requiredRunsPerWeek = requiredRunsPerWeek; }

    public int getTotalWeeksInPlan() { return totalWeeksInPlan; }
    public void setTotalWeeksInPlan(int totalWeeksInPlan) { this.totalWeeksInPlan = totalWeeksInPlan; }

    public String getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(String weekStartDate) { this.weekStartDate = weekStartDate; }

    public String getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(String weekEndDate) { this.weekEndDate = weekEndDate; }

    public String getScopeDescription() { return scopeDescription; }
    public void setScopeDescription(String scopeDescription) { this.scopeDescription = scopeDescription; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public Integer getCompliantStudents() { return compliantStudents; }
    public void setCompliantStudents(Integer compliantStudents) { this.compliantStudents = compliantStudents; }

    public Double getCompletionRatePercent() { return completionRatePercent; }
    public void setCompletionRatePercent(Double completionRatePercent) { this.completionRatePercent = completionRatePercent; }

    public boolean isRequirementConfigured() { return requirementConfigured; }
    public void setRequirementConfigured(boolean requirementConfigured) { this.requirementConfigured = requirementConfigured; }
}
