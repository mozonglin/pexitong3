package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.PeUser;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.pe.PeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PE统计管理服务
 */
@Service
public class PeStatisticsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PeUserRepository peUserRepository;
    
    @Autowired
    private PePermissionService pePermissionService;
    
    /**
     * 校级管理员设置PE积分指标
     */
    public void setSchoolPeTargets(String adminUserId, PeTargetRequest request) {
        // 验证权限
        User admin = pePermissionService.getUser(adminUserId);
        if (admin.getUserType() != User.UserType.school_admin && 
            admin.getUserType() != User.UserType.super_admin) {
            throw new RuntimeException("权限不足，只有校级管理员可以设置PE积分指标");
        }
        
        // 更新指标
        admin.setWeeklyTarget(request.getWeeklyTarget());
        admin.setMonthlyTarget(request.getMonthlyTarget());
        admin.setTotalTarget(request.getTotalTarget());
        
        userRepository.save(admin);
    }
    
    /**
     * 获取校级管理员统计数据
     */
    public SchoolStatisticsResponse getSchoolStatistics(String adminUserId) {
        // 验证权限
        User admin = pePermissionService.getUser(adminUserId);
        if (admin.getUserType() != User.UserType.school_admin && 
            admin.getUserType() != User.UserType.super_admin) {
            throw new RuntimeException("权限不足，只有校级管理员可以查看统计数据");
        }
        
        String school = admin.getSchool();
        
        SchoolStatisticsResponse response = new SchoolStatisticsResponse();
        response.setSchool(school);
        
        // 设置PE积分指标
        SchoolStatisticsResponse.PeTargets targets = new SchoolStatisticsResponse.PeTargets(
            admin.getWeeklyTarget(), admin.getMonthlyTarget(), admin.getTotalTarget()
        );
        response.setTargets(targets);
        
        // 获取本校所有学生
        List<PeUser> schoolStudents = peUserRepository.findBySchool(school);
        response.setTotalStudents(schoolStudents.size());
        
        // 计算整体达标率
        SchoolStatisticsResponse.ComplianceRates overallCompliance = calculateComplianceRates(schoolStudents, targets);
        response.setOverallCompliance(overallCompliance);
        
        // 按院系分组统计
        Map<String, List<PeUser>> collegeGroups = schoolStudents.stream()
            .filter(student -> student.getCollege() != null && !student.getCollege().isEmpty())
            .collect(Collectors.groupingBy(PeUser::getCollege));
        
        List<SchoolStatisticsResponse.CollegeStatistics> collegeStats = new ArrayList<>();
        List<SchoolStatisticsResponse.CollegeRanking> collegeRankings = new ArrayList<>();
        
        for (Map.Entry<String, List<PeUser>> entry : collegeGroups.entrySet()) {
            String collegeName = entry.getKey();
            List<PeUser> collegeStudents = entry.getValue();
            
            SchoolStatisticsResponse.ComplianceRates collegeCompliance = calculateComplianceRates(collegeStudents, targets);
            
            // 院系统计
            SchoolStatisticsResponse.CollegeStatistics collegeStat = new SchoolStatisticsResponse.CollegeStatistics(
                collegeName, collegeStudents.size(), collegeCompliance
            );
            collegeStats.add(collegeStat);
            
            // 院系排名（计算综合达标率）
            double overallRate = calculateOverallComplianceRate(collegeCompliance);
            SchoolStatisticsResponse.CollegeRanking ranking = new SchoolStatisticsResponse.CollegeRanking(
                0, collegeName, overallRate, collegeCompliance
            );
            collegeRankings.add(ranking);
        }
        
        // 按综合达标率排序
        collegeRankings.sort((a, b) -> Double.compare(b.getOverallComplianceRate(), a.getOverallComplianceRate()));
        for (int i = 0; i < collegeRankings.size(); i++) {
            collegeRankings.get(i).setRank(i + 1);
        }
        
        response.setCollegeStats(collegeStats);
        response.setCollegeRankings(collegeRankings);
        
        return response;
    }
    
    /**
     * 获取院级管理员统计数据
     * 校级管理员访问时返回全校所有院系和班级的数据
     * 院级管理员访问时只返回本院的数据
     */
    public CollegeStatisticsResponse getCollegeStatistics(String adminUserId) {
        // 验证权限
        User admin = pePermissionService.getUser(adminUserId);
        if (admin.getUserType() != User.UserType.department_admin && 
            admin.getUserType() != User.UserType.school_admin && 
            admin.getUserType() != User.UserType.super_admin) {
            throw new RuntimeException("权限不足，只有院级管理员及以上可以查看院系统计数据");
        }
        
        String school = admin.getSchool();
        
        // 获取校级管理员设置的指标
        User schoolAdmin = userRepository.findBySchoolAndUserType(school, User.UserType.school_admin)
            .orElseThrow(() -> new RuntimeException("未找到该校的校级管理员或未设置PE积分指标"));
        
        SchoolStatisticsResponse.PeTargets targets = new SchoolStatisticsResponse.PeTargets(
            schoolAdmin.getWeeklyTarget(), schoolAdmin.getMonthlyTarget(), schoolAdmin.getTotalTarget()
        );
        
        if (targets.getWeeklyTarget() == null || targets.getMonthlyTarget() == null || targets.getTotalTarget() == null) {
            throw new RuntimeException("校级管理员尚未设置PE积分指标");
        }
        
        CollegeStatisticsResponse response = new CollegeStatisticsResponse();
        response.setSchool(school);
        response.setTargets(targets);
        
        List<PeUser> targetStudents;
        
        // 根据管理员类型确定数据范围
        if (admin.getUserType() == User.UserType.school_admin || admin.getUserType() == User.UserType.super_admin) {
            // 校级管理员：获取全校学生数据
            targetStudents = peUserRepository.findBySchool(school);
            response.setCollege("全校所有院系");
        } else {
            // 院级管理员：获取本院学生数据
            String college = admin.getDepartmentName(); // 院级管理员的department_name对应院系
            targetStudents = peUserRepository.findBySchoolAndCollege(school, college);
            response.setCollege(college);
        }
        
        response.setTotalStudents(targetStudents.size());
        
        // 计算整体达标率
        SchoolStatisticsResponse.ComplianceRates overallCompliance = calculateComplianceRates(targetStudents, targets);
        response.setOverallCompliance(overallCompliance);
        
        // 按班级分组统计（如果是校级管理员，会包含所有院系的所有班级）
        Map<String, List<PeUser>> classGroups = targetStudents.stream()
            .filter(student -> student.getClassName() != null && !student.getClassName().isEmpty())
            .collect(Collectors.groupingBy(PeUser::getClassName));
        
        List<CollegeStatisticsResponse.ClassStatistics> classStats = new ArrayList<>();
        List<CollegeStatisticsResponse.ClassRanking> classRankings = new ArrayList<>();
        
        for (Map.Entry<String, List<PeUser>> entry : classGroups.entrySet()) {
            String className = entry.getKey();
            List<PeUser> classStudents = entry.getValue();
            
            SchoolStatisticsResponse.ComplianceRates classCompliance = calculateComplianceRates(classStudents, targets);
            
            // 班级统计
            CollegeStatisticsResponse.ClassStatistics classStat = new CollegeStatisticsResponse.ClassStatistics(
                className, classStudents.size(), classCompliance
            );
            classStats.add(classStat);
            
            // 班级排名（计算综合达标率）
            double overallRate = calculateOverallComplianceRate(classCompliance);
            CollegeStatisticsResponse.ClassRanking ranking = new CollegeStatisticsResponse.ClassRanking(
                0, className, overallRate, classCompliance
            );
            classRankings.add(ranking);
        }
        
        // 按综合达标率排序
        classRankings.sort((a, b) -> Double.compare(b.getOverallComplianceRate(), a.getOverallComplianceRate()));
        for (int i = 0; i < classRankings.size(); i++) {
            classRankings.get(i).setRank(i + 1);
        }
        
        response.setClassStats(classStats);
        response.setClassRankings(classRankings);
        
        return response;
    }
    
    /**
     * 计算达标率
     */
    private SchoolStatisticsResponse.ComplianceRates calculateComplianceRates(
        List<PeUser> students, SchoolStatisticsResponse.PeTargets targets) {
        
        if (students.isEmpty()) {
            return new SchoolStatisticsResponse.ComplianceRates(0.0, 0.0, 0.0);
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 计算周达标率（简化：以总积分除以周数估算）
        long weeklyCompliantCount = 0;
        long monthlyCompliantCount = 0;
        long totalCompliantCount = 0;
        
        for (PeUser student : students) {
            Integer totalPoints = student.getPoints();
            if (totalPoints == null) totalPoints = 0;
            
            // 简化计算：假设从创建时间到现在的总积分分布
            // 实际应用中应该根据具体的积分获取时间进行更精确的计算
            
            // 计算从账户创建到现在的周数和月数
            LocalDateTime createdAt = student.getCreatedAt();
            if (createdAt == null) createdAt = now.minusMonths(1); // 默认一个月前创建
            long dayssincecreated = ChronoUnit.DAYS.between(createdAt, now);
            double weeksSinceCreated = Math.max(1.0,dayssincecreated/7.0);
            double monthsSinceCreated = Math.max(1.0,dayssincecreated/30.0);
            
            if (weeksSinceCreated == 0) weeksSinceCreated = 1;
            if (monthsSinceCreated == 0) monthsSinceCreated = 1;
            
            // 估算周积分和月积分
            double estimatedWeeklyPoints = (double) totalPoints / weeksSinceCreated;
            double estimatedMonthlyPoints = (double) totalPoints / monthsSinceCreated;
            
            // 判断是否达标
            if (targets.getWeeklyTarget() != null && estimatedWeeklyPoints >= targets.getWeeklyTarget()) {
                weeklyCompliantCount++;
            }
            if (targets.getMonthlyTarget() != null && estimatedMonthlyPoints >= targets.getMonthlyTarget()) {
                monthlyCompliantCount++;
            }
            if (targets.getTotalTarget() != null && totalPoints >= targets.getTotalTarget()) {
                totalCompliantCount++;
            }
        }
        
        double weeklyRate = (double) weeklyCompliantCount / students.size() * 100;
        double monthlyRate = (double) monthlyCompliantCount / students.size() * 100;
        double totalRate = (double) totalCompliantCount / students.size() * 100;
        
        return new SchoolStatisticsResponse.ComplianceRates(
            Math.round(weeklyRate * 100.0) / 100.0,
            Math.round(monthlyRate * 100.0) / 100.0,
            Math.round(totalRate * 100.0) / 100.0
        );
    }
    
    /**
     * 计算综合达标率（取三项平均）
     */
    private double calculateOverallComplianceRate(SchoolStatisticsResponse.ComplianceRates rates) {
        double sum = 0;
        int count = 0;
        
        if (rates.getWeeklyComplianceRate() != null) {
            sum += rates.getWeeklyComplianceRate();
            count++;
        }
        if (rates.getMonthlyComplianceRate() != null) {
            sum += rates.getMonthlyComplianceRate();
            count++;
        }
        if (rates.getTotalComplianceRate() != null) {
            sum += rates.getTotalComplianceRate();
            count++;
        }
        
        return count > 0 ? Math.round(sum / count * 100.0) / 100.0 : 0.0;
    }
}
