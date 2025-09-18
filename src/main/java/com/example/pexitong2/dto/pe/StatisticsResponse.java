package com.example.pexitong2.dto.pe;

import java.util.List;

public class StatisticsResponse {
    
    private ActivityStatistics activities;
    private UserStatistics users;
    private MorningExerciseStatistics morningExercises;
    private AttendanceStatistics attendance;
    
    public StatisticsResponse() {}
    
    public StatisticsResponse(ActivityStatistics activities, UserStatistics users, 
                            MorningExerciseStatistics morningExercises, AttendanceStatistics attendance) {
        this.activities = activities;
        this.users = users;
        this.morningExercises = morningExercises;
        this.attendance = attendance;
    }
    
    public static class ActivityStatistics {
        private long total;
        private long pending;
        private long approved;
        private long rejected;
        
        public ActivityStatistics() {}
        
        public ActivityStatistics(long total, long pending, long approved, long rejected) {
            this.total = total;
            this.pending = pending;
            this.approved = approved;
            this.rejected = rejected;
        }
        
        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        
        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }
        
        public long getApproved() { return approved; }
        public void setApproved(long approved) { this.approved = approved; }
        
        public long getRejected() { return rejected; }
        public void setRejected(long rejected) { this.rejected = rejected; }
    }
    
    public static class UserStatistics {
        private long total;
        private long students;
        private long checkers;
        private long subCheckers;
        private long admins;
        
        public UserStatistics() {}
        
        public UserStatistics(long total, long students, long checkers, long subCheckers, long admins) {
            this.total = total;
            this.students = students;
            this.checkers = checkers;
            this.subCheckers = subCheckers;
            this.admins = admins;
        }
        
        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        
        public long getStudents() { return students; }
        public void setStudents(long students) { this.students = students; }
        
        public long getCheckers() { return checkers; }
        public void setCheckers(long checkers) { this.checkers = checkers; }
        
        public long getSubCheckers() { return subCheckers; }
        public void setSubCheckers(long subCheckers) { this.subCheckers = subCheckers; }
        
        public long getAdmins() { return admins; }
        public void setAdmins(long admins) { this.admins = admins; }
    }
    
    public static class MorningExerciseStatistics {
        private long total;
        private long active;
        private long completed;
        
        public MorningExerciseStatistics() {}
        
        public MorningExerciseStatistics(long total, long active, long completed) {
            this.total = total;
            this.active = active;
            this.completed = completed;
        }
        
        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        
        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }
        
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
    }
    
    public static class AttendanceStatistics {
        private long totalCheckins;
        private long totalCheckouts;
        private double averageDuration;
        private long totalPoints;
        
        public AttendanceStatistics() {}
        
        public AttendanceStatistics(long totalCheckins, long totalCheckouts, double averageDuration, long totalPoints) {
            this.totalCheckins = totalCheckins;
            this.totalCheckouts = totalCheckouts;
            this.averageDuration = averageDuration;
            this.totalPoints = totalPoints;
        }
        
        // Getters and Setters
        public long getTotalCheckins() { return totalCheckins; }
        public void setTotalCheckins(long totalCheckins) { this.totalCheckins = totalCheckins; }
        
        public long getTotalCheckouts() { return totalCheckouts; }
        public void setTotalCheckouts(long totalCheckouts) { this.totalCheckouts = totalCheckouts; }
        
        public double getAverageDuration() { return averageDuration; }
        public void setAverageDuration(double averageDuration) { this.averageDuration = averageDuration; }
        
        public long getTotalPoints() { return totalPoints; }
        public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }
    }
    
    // Chart data classes
    public static class ActivityChartData {
        private List<ChartItem> chartData;
        private ChartSummary summary;
        
        public ActivityChartData() {}
        
        public ActivityChartData(List<ChartItem> chartData, ChartSummary summary) {
            this.chartData = chartData;
            this.summary = summary;
        }
        
        // Getters and Setters
        public List<ChartItem> getChartData() { return chartData; }
        public void setChartData(List<ChartItem> chartData) { this.chartData = chartData; }
        
        public ChartSummary getSummary() { return summary; }
        public void setSummary(ChartSummary summary) { this.summary = summary; }
    }
    
    public static class ChartItem {
        private String name;
        private long count;
        private long participants;
        
        public ChartItem() {}
        
        public ChartItem(String name, long count, long participants) {
            this.name = name;
            this.count = count;
            this.participants = participants;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        
        public long getParticipants() { return participants; }
        public void setParticipants(long participants) { this.participants = participants; }
    }
    
    public static class ChartSummary {
        private long totalActivities;
        private long totalParticipants;
        private double averageParticipants;
        
        public ChartSummary() {}
        
        public ChartSummary(long totalActivities, long totalParticipants, double averageParticipants) {
            this.totalActivities = totalActivities;
            this.totalParticipants = totalParticipants;
            this.averageParticipants = averageParticipants;
        }
        
        // Getters and Setters
        public long getTotalActivities() { return totalActivities; }
        public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }
        
        public long getTotalParticipants() { return totalParticipants; }
        public void setTotalParticipants(long totalParticipants) { this.totalParticipants = totalParticipants; }
        
        public double getAverageParticipants() { return averageParticipants; }
        public void setAverageParticipants(double averageParticipants) { this.averageParticipants = averageParticipants; }
    }
    
    // Getters and Setters
    public ActivityStatistics getActivities() { return activities; }
    public void setActivities(ActivityStatistics activities) { this.activities = activities; }
    
    public UserStatistics getUsers() { return users; }
    public void setUsers(UserStatistics users) { this.users = users; }
    
    public MorningExerciseStatistics getMorningExercises() { return morningExercises; }
    public void setMorningExercises(MorningExerciseStatistics morningExercises) { this.morningExercises = morningExercises; }
    
    public AttendanceStatistics getAttendance() { return attendance; }
    public void setAttendance(AttendanceStatistics attendance) { this.attendance = attendance; }
}




