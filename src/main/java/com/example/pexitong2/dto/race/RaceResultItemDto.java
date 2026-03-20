package com.example.pexitong2.dto.race;

/**
 * 单条运动员成绩数据（上传请求体中 results 数组的元素）
 */
public class RaceResultItemDto {

    private String studentNumber;
    private String name;
    private String school;
    private String gender;
    private Integer totalLaps;
    private String finalTime;
    private Long finalTimeMs;
    private String teacherName;

    public RaceResultItemDto() {}

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

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
}
