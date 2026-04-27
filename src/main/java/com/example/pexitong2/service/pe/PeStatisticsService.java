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
        
        // 获取本校所有学生（PE 库 users1 中 role=STUDENT，与院系统计口径一致）
        List<PeUser> schoolStudents = peUserRepository.findBySchoolAndRole(school, PeUser.Role.STUDENT);
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
        
        // 根据管理员类型确定数据范围（仅统计 PE 学生；院系与 users.department_name 对齐并兼容空格/大小写）
        if (admin.getUserType() == User.UserType.school_admin || admin.getUserType() == User.UserType.super_admin) {
            targetStudents = peUserRepository.findBySchoolAndRole(school, PeUser.Role.STUDENT);
            response.setCollege("全校所有院系");
        } else {
            targetStudents = resolveCollegeStudentsForAdmin(admin, school);
            String collegeLabel = admin.getDepartmentName() != null ? admin.getDepartmentName().trim() : "";
            response.setCollege(collegeLabel.isEmpty() ? "未配置院系" : collegeLabel);
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
    
    /**
     * 获取校级管理员阳光跑统计数据（按院系统计）
     */
    public SunshineRunStatisticsResponse getSchoolSunshineRunStatistics(String adminUserId) {
        // 验证权限
        User admin = pePermissionService.getUser(adminUserId);
        if (admin.getUserType() != User.UserType.school_admin && 
            admin.getUserType() != User.UserType.super_admin) {
            throw new RuntimeException("权限不足，只有校级管理员可以查看统计数据");
        }
        
        String school = admin.getSchool();
        
        SunshineRunStatisticsResponse response = new SunshineRunStatisticsResponse();
        response.setSchool(school);
        response.setScope("全校");
        
        // 获取本校所有学生
        List<PeUser> schoolStudents = peUserRepository.findBySchoolAndRole(school, PeUser.Role.STUDENT);
        response.setTotalStudents(schoolStudents.size());
        
        // 计算整体统计
        SunshineRunStatisticsResponse.SunshineRunAggregate overallAggregate = calculateSunshineRunAggregate(schoolStudents);
        response.setOverall(overallAggregate);
        
        // 按院系分组统计
        Map<String, List<PeUser>> collegeGroups = schoolStudents.stream()
            .filter(student -> student.getCollege() != null && !student.getCollege().isEmpty())
            .collect(Collectors.groupingBy(PeUser::getCollege));
        
        List<SunshineRunStatisticsResponse.GroupStatistics> groupStats = new ArrayList<>();
        List<SunshineRunStatisticsResponse.GroupRanking> groupRankings = new ArrayList<>();
        
        for (Map.Entry<String, List<PeUser>> entry : collegeGroups.entrySet()) {
            String collegeName = entry.getKey();
            List<PeUser> collegeStudents = entry.getValue();
            
            SunshineRunStatisticsResponse.SunshineRunAggregate collegeAggregate = calculateSunshineRunAggregate(collegeStudents);
            
            // 院系统计
            SunshineRunStatisticsResponse.GroupStatistics groupStat = new SunshineRunStatisticsResponse.GroupStatistics(
                collegeName, collegeStudents.size(), collegeAggregate
            );
            groupStats.add(groupStat);
            
            // 院系排名（按人均跑步距离排名）
            SunshineRunStatisticsResponse.GroupRanking ranking = new SunshineRunStatisticsResponse.GroupRanking(
                0, collegeName, collegeAggregate.getAvgDistancePerStudent(), collegeAggregate
            );
            groupRankings.add(ranking);
        }
        
        // 按人均跑步距离排序
        groupRankings.sort((a, b) -> Double.compare(b.getAvgDistancePerStudent(), a.getAvgDistancePerStudent()));
        for (int i = 0; i < groupRankings.size(); i++) {
            groupRankings.get(i).setRank(i + 1);
        }
        
        response.setGroupStats(groupStats);
        response.setGroupRankings(groupRankings);
        
        return response;
    }
    
    /**
     * 获取院级管理员阳光跑统计数据（按班级统计）
     * 校级管理员访问时返回全校所有班级的数据
     * 院级管理员访问时只返回本院的数据
     */
    public SunshineRunStatisticsResponse getCollegeSunshineRunStatistics(String adminUserId) {
        User admin = pePermissionService.getUser(adminUserId);
        if (admin.getUserType() != User.UserType.department_admin && 
            admin.getUserType() != User.UserType.school_admin && 
            admin.getUserType() != User.UserType.super_admin &&
            admin.getUserType() != User.UserType.counselor) {
            throw new RuntimeException("权限不足，只有院级管理员及以上可以查看院系统计数据");
        }
        
        String school = admin.getSchool();
        
        SunshineRunStatisticsResponse response = new SunshineRunStatisticsResponse();
        response.setSchool(school);
        
        List<PeUser> targetStudents;
        
        if (admin.getUserType() == User.UserType.school_admin || admin.getUserType() == User.UserType.super_admin) {
            targetStudents = peUserRepository.findBySchoolAndRole(school, PeUser.Role.STUDENT);
            response.setScope("全校所有院系");
        } else if (admin.getUserType() == User.UserType.counselor) {
            List<String> allowedClasses = pePermissionService.getAllowedClassNames(adminUserId);
            List<PeUser> collegeStudents = resolveCollegeStudentsForCounselor(admin, school);
            if (allowedClasses != null && !allowedClasses.isEmpty()) {
                targetStudents = collegeStudents.stream()
                    .filter(s -> s.getClassName() != null && allowedClasses.contains(s.getClassName().trim()))
                    .collect(java.util.stream.Collectors.toList());
                response.setScope("管辖班级");
            } else {
                targetStudents = collegeStudents;
                response.setScope("未分配班级");
            }
        } else {
            targetStudents = resolveCollegeStudentsForAdmin(admin, school);
            String collegeLabel = admin.getDepartmentName() != null ? admin.getDepartmentName().trim() : "";
            response.setScope(collegeLabel.isEmpty() ? "未配置院系" : collegeLabel);
        }
        
        response.setTotalStudents(targetStudents.size());
        
        // 计算整体统计
        SunshineRunStatisticsResponse.SunshineRunAggregate overallAggregate = calculateSunshineRunAggregate(targetStudents);
        response.setOverall(overallAggregate);
        
        // 按班级分组统计（如果是校级管理员，会包含所有院系的所有班级）
        Map<String, List<PeUser>> classGroups = targetStudents.stream()
            .filter(student -> student.getClassName() != null && !student.getClassName().isEmpty())
            .collect(Collectors.groupingBy(PeUser::getClassName));
        
        List<SunshineRunStatisticsResponse.GroupStatistics> groupStats = new ArrayList<>();
        List<SunshineRunStatisticsResponse.GroupRanking> groupRankings = new ArrayList<>();
        
        for (Map.Entry<String, List<PeUser>> entry : classGroups.entrySet()) {
            String className = entry.getKey();
            List<PeUser> classStudents = entry.getValue();
            
            SunshineRunStatisticsResponse.SunshineRunAggregate classAggregate = calculateSunshineRunAggregate(classStudents);
            
            // 班级统计
            SunshineRunStatisticsResponse.GroupStatistics groupStat = new SunshineRunStatisticsResponse.GroupStatistics(
                className, classStudents.size(), classAggregate
            );
            groupStats.add(groupStat);
            
            // 班级排名（按人均跑步距离排名）
            SunshineRunStatisticsResponse.GroupRanking ranking = new SunshineRunStatisticsResponse.GroupRanking(
                0, className, classAggregate.getAvgDistancePerStudent(), classAggregate
            );
            groupRankings.add(ranking);
        }
        
        // 按人均跑步距离排序
        groupRankings.sort((a, b) -> Double.compare(b.getAvgDistancePerStudent(), a.getAvgDistancePerStudent()));
        for (int i = 0; i < groupRankings.size(); i++) {
            groupRankings.get(i).setRank(i + 1);
        }
        
        response.setGroupStats(groupStats);
        response.setGroupRankings(groupRankings);
        
        return response;
    }

    /**
     * 院级管理员：users.department_name 与 PE 库 users1.college 对齐；仅统计 role=STUDENT。
     * 依次尝试：忽略大小写+trim、去全部空白后比较小写。
     */
    private List<PeUser> resolveCollegeStudentsForAdmin(User admin, String school) {
        if (admin.getUserType() != User.UserType.department_admin) {
            throw new IllegalStateException("resolveCollegeStudentsForAdmin 仅适用于院级管理员");
        }
        String deptRaw = admin.getDepartmentName();
        if (deptRaw == null || deptRaw.isBlank()) {
            throw new RuntimeException(
                "院级管理员未配置所属院系（users.department_name）。请在校级管理员「管理员管理」中补全院系名称，并与 PE 学生档案(users1.college)保持一致。");
        }
        final String dept = deptRaw.trim();
        List<PeUser> schoolStudents = peUserRepository.findBySchoolAndRole(school, PeUser.Role.STUDENT);

        List<PeUser> match = schoolStudents.stream()
            .filter(u -> u.getCollege() != null && !u.getCollege().isBlank())
            .filter(u -> dept.equalsIgnoreCase(u.getCollege().trim()))
            .collect(Collectors.toList());
        if (!match.isEmpty()) {
            return match;
        }

        final String deptKey = normalizeCollegeKey(dept);
        match = schoolStudents.stream()
            .filter(u -> u.getCollege() != null && !u.getCollege().isBlank())
            .filter(u -> normalizeCollegeKey(u.getCollege()).equals(deptKey))
            .collect(Collectors.toList());
        if (!match.isEmpty()) {
            return match;
        }

        boolean hasOtherColleges = schoolStudents.stream()
            .anyMatch(u -> u.getCollege() != null && !u.getCollege().isBlank());
        if (hasOtherColleges) {
            Set<String> samples = schoolStudents.stream()
                .map(PeUser::getCollege)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
            List<String> sampleList = samples.stream().limit(8).collect(Collectors.toList());
            throw new RuntimeException(String.format(
                "本院统计未匹配到学生：管理员院系为「%s」，与 PE 库中学生 college 字段不一致。本校已有 college 示例：%s。请在后台统一院系名称。",
                dept, sampleList.isEmpty() ? "（无）" : String.join("、", sampleList)));
        }

        return Collections.emptyList();
    }

    private List<PeUser> resolveCollegeStudentsForCounselor(User admin, String school) {
        String dept = admin.getDepartmentName();
        if (dept == null || dept.isBlank()) {
            dept = pePermissionService.getCounselorDepartment(admin.getId());
        }
        if (dept == null || dept.isBlank()) {
            return Collections.emptyList();
        }
        final String deptTrimmed = dept.trim();
        List<PeUser> schoolStudents = peUserRepository.findBySchoolAndRole(school, PeUser.Role.STUDENT);
        List<PeUser> match = schoolStudents.stream()
            .filter(u -> u.getCollege() != null && !u.getCollege().isBlank())
            .filter(u -> deptTrimmed.equalsIgnoreCase(u.getCollege().trim()))
            .collect(Collectors.toList());
        if (!match.isEmpty()) return match;
        final String deptKey = normalizeCollegeKey(deptTrimmed);
        return schoolStudents.stream()
            .filter(u -> u.getCollege() != null && !u.getCollege().isBlank())
            .filter(u -> normalizeCollegeKey(u.getCollege()).equals(deptKey))
            .collect(Collectors.toList());
    }

    private static String normalizeCollegeKey(String name) {
        if (name == null) return "";
        return name.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
    
    /**
     * 计算阳光跑汇总数据
     */
    private SunshineRunStatisticsResponse.SunshineRunAggregate calculateSunshineRunAggregate(List<PeUser> students) {
        if (students.isEmpty()) {
            return new SunshineRunStatisticsResponse.SunshineRunAggregate(0L, 0L, 0L, 0.0, 0.0, 0.0);
        }
        
        long totalRuns = 0;
        long totalDistance = 0;  // 米
        long totalDuration = 0;  // 秒
        
        for (PeUser student : students) {
            Integer runs = student.getSunshineTotalRuns();
            Double distance = student.getSunshineTotalDistance();
            Long duration = student.getSunshineTotalDuration();
            
            if (runs != null) {
                totalRuns += runs;
            }
            
            // 距离：从小数转换为整数米（82.77310080397267 -> 82）
            if (distance != null) {
                totalDistance += distance.longValue();
            }
            
            // 时长：从毫秒转换为秒（15657 -> 15）
            if (duration != null) {
                totalDuration += duration / 1000;
            }
        }
        
        int studentCount = students.size();
        double avgRuns = Math.round((double) totalRuns / studentCount * 100.0) / 100.0;
        double avgDistance = Math.round((double) totalDistance / studentCount * 100.0) / 100.0;
        double avgDuration = Math.round((double) totalDuration / studentCount * 100.0) / 100.0;
        
        return new SunshineRunStatisticsResponse.SunshineRunAggregate(
            totalRuns, totalDistance, totalDuration,
            avgRuns, avgDistance, avgDuration
        );
    }
}
