package com.example.pexitong2.dto;

/**
 * 本周课后作业提交达标统计（按 homework_scores 行数计提交次数，全项目合计）。
 */
public class HomeworkWeeklySubmissionCompletionResponse {

    private int requiredSubmissionsPerWeek;
    private int submissionSemesterWeeks;
    private String weekStartDate;
    private String weekEndDate;
    private String scopeDescription;
    private int totalStudents;
    private Integer compliantStudents;
    private Double completionRatePercent;
    private boolean requirementConfigured;

    public int getRequiredSubmissionsPerWeek() { return requiredSubmissionsPerWeek; }
    public void setRequiredSubmissionsPerWeek(int requiredSubmissionsPerWeek) {
        this.requiredSubmissionsPerWeek = requiredSubmissionsPerWeek;
    }

    public int getSubmissionSemesterWeeks() { return submissionSemesterWeeks; }
    public void setSubmissionSemesterWeeks(int submissionSemesterWeeks) {
        this.submissionSemesterWeeks = submissionSemesterWeeks;
    }

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
