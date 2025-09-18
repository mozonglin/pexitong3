package com.example.pexitong2.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CheckUserService {
    
    private static final String CHECK_DB_URL = "jdbc:mysql://38.207.179.218:3306/checkuser?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
    private static final String DB_USER = "pexitong3";
    private static final String DB_PASSWORD = "790128mmmm";
    
    /*
     * 验证学生信息
     */
    public Optional<CheckStudentInfo> validateStudent(String studentId, String school, String name) {
        String sql = "SELECT school, college, studentid, name FROM checkstudent WHERE studentid = ? AND school = ? AND name = ?";

        try (Connection conn = DriverManager.getConnection(CHECK_DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, school);
            stmt.setString(3, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CheckStudentInfo info = new CheckStudentInfo();
                    info.setSchool(rs.getString("school"));
                    info.setCollege(rs.getString("college"));
                    info.setStudentid(rs.getString("studentid"));
                    info.setName(rs.getString("name"));
                    return Optional.of(info);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * 验证教师信息
     */
    public Optional<CheckTeacherInfo> validateTeacher(String teacherId, String school, String name) {
        String sql = "SELECT school, teacherid, college, name FROM checkteacher WHERE teacherid = ? AND school = ? AND name = ?";
        
        try (Connection conn = DriverManager.getConnection(CHECK_DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, teacherId);
            stmt.setString(2, school);
            stmt.setString(3, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CheckTeacherInfo info = new CheckTeacherInfo();
                    info.setSchool(rs.getString("school"));
                    info.setTeacherid(rs.getString("teacherid"));
                    info.setCollege(rs.getString("college"));
                    info.setName(rs.getString("name"));
                    return Optional.of(info);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * 标记学生为已使用（可选功能，根据实际需求决定是否需要）
     * 注：由于预导入表结构固定，暂时不实现标记功能
     */
    public void markStudentAsUsed(String studentId) {
        // 预导入表结构固定，不包含is_used字段
        // 如需要，可在主数据库中记录已使用的学工号
        System.out.println("学生 " + studentId + " 已完成验证");
    }
    
    /**
     * 标记教师为已使用（可选功能，根据实际需求决定是否需要）
     * 注：由于预导入表结构固定，暂时不实现标记功能
     */
    public void markTeacherAsUsed(String teacherId) {
        // 预导入表结构固定，不包含is_used字段
        // 如需要，可在主数据库中记录已使用的学工号
        System.out.println("教师 " + teacherId + " 已完成验证");
    }
    
    // 内部类定义
    public static class CheckStudentInfo {
        private String school;
        private String college;
        private String studentid;
        private String name;
        
        // Getters and Setters
        public String getSchool() { return school; }
        public void setSchool(String school) { this.school = school; }
        
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        
        public String getStudentid() { return studentid; }
        public void setStudentid(String studentid) { this.studentid = studentid; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class CheckTeacherInfo {
        private String school;
        private String teacherid;
        private String college;
        private String name;
        
        // Getters and Setters
        public String getSchool() { return school; }
        public void setSchool(String school) { this.school = school; }
        
        public String getTeacherid() { return teacherid; }
        public void setTeacherid(String teacherid) { this.teacherid = teacherid; }
        
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
} 