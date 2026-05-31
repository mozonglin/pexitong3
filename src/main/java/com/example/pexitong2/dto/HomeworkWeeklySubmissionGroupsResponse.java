package com.example.pexitong2.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 各院系/班级本周课后作业提交达标率。
 */
public class HomeworkWeeklySubmissionGroupsResponse {

    private String view;
    private int requiredSubmissionsPerWeek;
    private int submissionSemesterWeeks;
    private String weekStartDate;
    private String weekEndDate;
    private boolean requirementConfigured;

    private List<HomeworkWeeklyGroupRow> groups = new ArrayList<>();

    public static class HomeworkWeeklyGroupRow {
        private String groupName;
        private String departmentName;
        private int totalStudents;
        private Integer compliantStudents;
        private Double completionRatePercent;

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }

        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

        public Integer getCompliantStudents() { return compliantStudents; }
        public void setCompliantStudents(Integer compliantStudents) { this.compliantStudents = compliantStudents; }

        public Double getCompletionRatePercent() { return completionRatePercent; }
        public void setCompletionRatePercent(Double completionRatePercent) { this.completionRatePercent = completionRatePercent; }
    }

    public String getView() { return view; }
    public void setView(String view) { this.view = view; }

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

    public boolean isRequirementConfigured() { return requirementConfigured; }
    public void setRequirementConfigured(boolean requirementConfigured) { this.requirementConfigured = requirementConfigured; }

    public List<HomeworkWeeklyGroupRow> getGroups() { return groups; }
    public void setGroups(List<HomeworkWeeklyGroupRow> groups) { this.groups = groups; }
}
