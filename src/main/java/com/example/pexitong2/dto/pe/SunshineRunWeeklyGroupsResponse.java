package com.example.pexitong2.dto.pe;

import java.util.ArrayList;
import java.util.List;

/**
 * 各院系/各班级「本周」阳光跑完成率（与统计大屏分组维度一致）。
 */
public class SunshineRunWeeklyGroupsResponse {

    /** school=按院系 college=按班级，与阳光跑统计 viewMode 一致 */
    private String view;
    private int requiredRunsPerWeek;
    private int totalWeeksInPlan;
    private String weekStartDate;
    private String weekEndDate;
    private boolean requirementConfigured;

    private List<GroupWeeklyCompletion> groups = new ArrayList<>();

    public static class GroupWeeklyCompletion {
        private String groupName;
        private int totalStudents;
        private Integer compliantStudents;
        private Double completionRatePercent;

        public GroupWeeklyCompletion() {}

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }

        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

        public Integer getCompliantStudents() { return compliantStudents; }
        public void setCompliantStudents(Integer compliantStudents) { this.compliantStudents = compliantStudents; }

        public Double getCompletionRatePercent() { return completionRatePercent; }
        public void setCompletionRatePercent(Double completionRatePercent) { this.completionRatePercent = completionRatePercent; }
    }

    public String getView() { return view; }
    public void setView(String view) { this.view = view; }

    public int getRequiredRunsPerWeek() { return requiredRunsPerWeek; }
    public void setRequiredRunsPerWeek(int requiredRunsPerWeek) { this.requiredRunsPerWeek = requiredRunsPerWeek; }

    public int getTotalWeeksInPlan() { return totalWeeksInPlan; }
    public void setTotalWeeksInPlan(int totalWeeksInPlan) { this.totalWeeksInPlan = totalWeeksInPlan; }

    public String getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(String weekStartDate) { this.weekStartDate = weekStartDate; }

    public String getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(String weekEndDate) { this.weekEndDate = weekEndDate; }

    public boolean isRequirementConfigured() { return requirementConfigured; }
    public void setRequirementConfigured(boolean requirementConfigured) { this.requirementConfigured = requirementConfigured; }

    public List<GroupWeeklyCompletion> getGroups() { return groups; }
    public void setGroups(List<GroupWeeklyCompletion> groups) { this.groups = groups; }
}
