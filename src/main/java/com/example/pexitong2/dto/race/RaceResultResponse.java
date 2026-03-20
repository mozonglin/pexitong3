package com.example.pexitong2.dto.race;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.pexitong2.entity.race.RaceResult;

import java.time.LocalDateTime;

/**
 * 管理端查询单条成绩的响应体
 */
public class RaceResultResponse {

    private Long id;
    private String studentNumber;
    private String name;
    private String school;
    private String gender;
    private Integer totalLaps;
    private String finalTime;
    private Long finalTimeMs;
    private boolean finished;
    private String teacherName;
    private String uploaderId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime uploadedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    public RaceResultResponse() {}

    public static RaceResultResponse from(RaceResult r) {
        RaceResultResponse resp = new RaceResultResponse();
        resp.id            = r.getId();
        resp.studentNumber = r.getStudentNumber();
        resp.name          = r.getName();
        resp.school        = r.getSchool();
        resp.gender        = r.getGender();
        resp.totalLaps     = r.getTotalLaps();
        resp.finalTime     = r.getFinalTime();
        resp.finalTimeMs   = r.getFinalTimeMs();
        resp.finished      = r.getFinalTimeMs() != null;
        resp.teacherName   = r.getTeacherName();
        resp.uploaderId    = r.getUploaderId();
        resp.uploadedAt    = r.getUploadedAt();
        resp.createdAt     = r.getCreatedAt();
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getTotalLaps() { return totalLaps; }
    public void setTotalLaps(Integer totalLaps) { this.totalLaps = totalLaps; }

    public String getFinalTime() { return finalTime; }
    public void setFinalTime(String finalTime) { this.finalTime = finalTime; }

    public Long getFinalTimeMs() { return finalTimeMs; }
    public void setFinalTimeMs(Long finalTimeMs) { this.finalTimeMs = finalTimeMs; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
