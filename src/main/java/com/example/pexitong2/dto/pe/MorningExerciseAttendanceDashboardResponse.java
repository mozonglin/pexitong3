package com.example.pexitong2.dto.pe;

import java.util.ArrayList;
import java.util.List;

/**
 * 早操出勤率统计大屏：以「签到且签退」为出勤，其余（未签到、只签未退）为未出勤。
 */
public class MorningExerciseAttendanceDashboardResponse {

    private String startDate;
    private String endDate;
    private boolean showCollegeStats;
    private int exerciseCount;
    /** 固定提示文案，前端可直接展示 */
    private String notice;
    private List<CollegeStatRow> colleges = new ArrayList<>();
    private List<ClassStatRow> classes = new ArrayList<>();

    public MorningExerciseAttendanceDashboardResponse() {
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isShowCollegeStats() {
        return showCollegeStats;
    }

    public void setShowCollegeStats(boolean showCollegeStats) {
        this.showCollegeStats = showCollegeStats;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public List<CollegeStatRow> getColleges() {
        return colleges;
    }

    public void setColleges(List<CollegeStatRow> colleges) {
        this.colleges = colleges;
    }

    public List<ClassStatRow> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassStatRow> classes) {
        this.classes = classes;
    }

    public static class CollegeStatRow {
        private String schoolName;
        private String collegeName;
        /** users1 中该院系学生人数（role=STUDENT） */
        private long studentHeadcount;
        /** 应考勤人次：每场早操 × 该院应到学生 */
        private long totalSlots;
        private long presentSlots;
        private long absentSlots;
        /** 0～100，保留两位小数由序列化侧处理 */
        private double attendanceRatePercent;

        public CollegeStatRow() {
        }

        public String getSchoolName() {
            return schoolName;
        }

        public void setSchoolName(String schoolName) {
            this.schoolName = schoolName;
        }

        public String getCollegeName() {
            return collegeName;
        }

        public void setCollegeName(String collegeName) {
            this.collegeName = collegeName;
        }

        public long getStudentHeadcount() {
            return studentHeadcount;
        }

        public void setStudentHeadcount(long studentHeadcount) {
            this.studentHeadcount = studentHeadcount;
        }

        public long getTotalSlots() {
            return totalSlots;
        }

        public void setTotalSlots(long totalSlots) {
            this.totalSlots = totalSlots;
        }

        public long getPresentSlots() {
            return presentSlots;
        }

        public void setPresentSlots(long presentSlots) {
            this.presentSlots = presentSlots;
        }

        public long getAbsentSlots() {
            return absentSlots;
        }

        public void setAbsentSlots(long absentSlots) {
            this.absentSlots = absentSlots;
        }

        public double getAttendanceRatePercent() {
            return attendanceRatePercent;
        }

        public void setAttendanceRatePercent(double attendanceRatePercent) {
            this.attendanceRatePercent = attendanceRatePercent;
        }
    }

    public static class ClassStatRow {
        private String schoolName;
        private String collegeName;
        private String className;
        private long studentHeadcount;
        private long totalSlots;
        private long presentSlots;
        private long absentSlots;
        private double attendanceRatePercent;

        public ClassStatRow() {
        }

        public String getSchoolName() {
            return schoolName;
        }

        public void setSchoolName(String schoolName) {
            this.schoolName = schoolName;
        }

        public String getCollegeName() {
            return collegeName;
        }

        public void setCollegeName(String collegeName) {
            this.collegeName = collegeName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public long getStudentHeadcount() {
            return studentHeadcount;
        }

        public void setStudentHeadcount(long studentHeadcount) {
            this.studentHeadcount = studentHeadcount;
        }

        public long getTotalSlots() {
            return totalSlots;
        }

        public void setTotalSlots(long totalSlots) {
            this.totalSlots = totalSlots;
        }

        public long getPresentSlots() {
            return presentSlots;
        }

        public void setPresentSlots(long presentSlots) {
            this.presentSlots = presentSlots;
        }

        public long getAbsentSlots() {
            return absentSlots;
        }

        public void setAbsentSlots(long absentSlots) {
            this.absentSlots = absentSlots;
        }

        public double getAttendanceRatePercent() {
            return attendanceRatePercent;
        }

        public void setAttendanceRatePercent(double attendanceRatePercent) {
            this.attendanceRatePercent = attendanceRatePercent;
        }
    }
}
