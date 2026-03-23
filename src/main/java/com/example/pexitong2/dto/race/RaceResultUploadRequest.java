package com.example.pexitong2.dto.race;

import java.time.Instant;
import java.util.List;

/**
 * 上位机上传比赛成绩的请求体
 */
public class RaceResultUploadRequest {

    private Instant uploadedAt;

    private List<RaceResultItemDto> results;

    public RaceResultUploadRequest() {}

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    public List<RaceResultItemDto> getResults() { return results; }
    public void setResults(List<RaceResultItemDto> results) { this.results = results; }
}
