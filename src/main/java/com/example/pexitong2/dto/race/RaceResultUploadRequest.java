package com.example.pexitong2.dto.race;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上位机上传比赛成绩的请求体
 */
public class RaceResultUploadRequest {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime uploadedAt;

    private List<RaceResultItemDto> results;

    public RaceResultUploadRequest() {}

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public List<RaceResultItemDto> getResults() { return results; }
    public void setResults(List<RaceResultItemDto> results) { this.results = results; }
}
