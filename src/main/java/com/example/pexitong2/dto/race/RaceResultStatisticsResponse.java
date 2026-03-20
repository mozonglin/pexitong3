package com.example.pexitong2.dto.race;

/**
 * 管理端成绩统计响应体
 */
public class RaceResultStatisticsResponse {

    private long totalCount;
    private long finishedCount;
    private long unfinishedCount;
    private String school;

    public RaceResultStatisticsResponse() {}

    public RaceResultStatisticsResponse(long totalCount, long finishedCount, long unfinishedCount, String school) {
        this.totalCount      = totalCount;
        this.finishedCount   = finishedCount;
        this.unfinishedCount = unfinishedCount;
        this.school          = school;
    }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public long getFinishedCount() { return finishedCount; }
    public void setFinishedCount(long finishedCount) { this.finishedCount = finishedCount; }

    public long getUnfinishedCount() { return unfinishedCount; }
    public void setUnfinishedCount(long unfinishedCount) { this.unfinishedCount = unfinishedCount; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
}
