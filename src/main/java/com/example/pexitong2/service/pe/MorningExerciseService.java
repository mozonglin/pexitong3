package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.MorningExercise;
import com.example.pexitong2.entity.pe.MorningExerciseAttendance;
import com.example.pexitong2.entity.pe.PeUser;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.pe.MorningExerciseRepository;
import com.example.pexitong2.repository.pe.MorningExerciseAttendanceRepository;
import com.example.pexitong2.repository.pe.PeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 早操管理服务
 */
@Service
public class MorningExerciseService {
    
    @Autowired
    private MorningExerciseRepository morningExerciseRepository;
    
    @Autowired
    private MorningExerciseAttendanceRepository attendanceRepository;
    
    @Autowired
    private PePermissionService permissionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PeUserRepository peUserRepository;

    private static final String NOTICE_SCHOOL_ADMIN =
        "早操活动仅院级管理员可发布与维护；校级管理员可查看本校早操出勤统计。\n"
            + "说明：院系统计、班级统计均基于所选日期范围内已实际发布的早操场次；仅当有院系发布了场次时才会出现对应院系（及班级）行，"
            + "尚未发布早操的学院不会出现在表中，并非权限限制。";
    private static final String NOTICE_DEPT_ADMIN =
        "以下为本院班级出勤统计（仅统计所选日期范围内已发布的早操场次）；院系统计仅校级管理员可见。";
    
    /**
     * 获取早操活动列表
     */
    public PageResponse<MorningExerciseResponse> getMorningExercises(
            int page, int pageSize, String date, Boolean isActive, 
            String startDate, String endDate, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        LocalDate dateParam = date != null ? LocalDate.parse(date) : null;
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        
        Page<MorningExercise> exercisePage = morningExerciseRepository.findMorningExercisesWithFilters(
            dateParam, isActive, start, end, allowedSchool, allowedCollege, pageable);
        
        List<MorningExerciseResponse> responses = exercisePage.getContent().stream()
            .map(MorningExerciseResponse::new)
            .collect(Collectors.toList());
        
        return new PageResponse<>(responses, exercisePage.getTotalElements(), page, pageSize);
    }
    
    /**
     * 获取单个早操活动详情
     */
    public MorningExerciseResponse getMorningExerciseById(String id, String currentUserId) {
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        MorningExercise exercise = morningExerciseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("早操活动不存在"));
        
        // TODO: 验证是否在权限范围内（需要关联创建者的学校/学院信息）
        
        return new MorningExerciseResponse(exercise);
    }
    
    /**
     * 创建早操活动
     */
    @Transactional
    public MorningExerciseResponse createMorningExercise(
            MorningExerciseRequest request, String currentUserId) {
        
        // 验证早操发布权限（只有院级管理员可以发布）
        permissionService.validateMorningExercisePublishPermission(currentUserId);
        
        // 验证请求参数
        if (request.getDate() == null || request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("日期和时间参数不能为空");
        }
        
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        // 获取当前用户的学院信息
        String currentUserCollege = permissionService.getAllowedCollege(currentUserId);
        String currentUserSchool = permissionService.getAllowedSchool(currentUserId);
        
        // 检查同学院同一天是否已存在早操活动
        List<MorningExercise> existingExercises = morningExerciseRepository.findBySchoolAndCollege(
            currentUserSchool, currentUserCollege);
        
        boolean hasConflict = existingExercises.stream()
            .anyMatch(exercise -> exercise.getDate().equals(request.getDate()));
        
        if (hasConflict) {
            throw new RuntimeException("同学院在该日期已存在早操活动，不能重复发布");
        }
        
        // 创建早操活动
        MorningExercise exercise = new MorningExercise();
        exercise.setId(UUID.randomUUID().toString());
        exercise.setTitle(request.getTitle() != null ? request.getTitle() : "早操考勤");
        exercise.setDescription(request.getDescription());
        exercise.setLocation(request.getLocation() != null ? request.getLocation() : "操场");
        exercise.setDate(request.getDate());
        exercise.setStartTime(request.getStartTime());
        exercise.setEndTime(request.getEndTime());
        exercise.setCreatedBy(currentUserId);
        
        MorningExercise savedExercise = morningExerciseRepository.save(exercise);
        
        return new MorningExerciseResponse(savedExercise);
    }
    
    /**
     * 更新早操活动
     */
    @Transactional
    public MorningExerciseResponse updateMorningExercise(
            String id, MorningExerciseRequest request, String currentUserId) {
        
        MorningExercise exercise = morningExerciseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("早操活动不存在"));
        
        // 验证修改权限（只有创建者所在学院的院级管理员可以修改）
        permissionService.validateMorningExerciseModifyPermission(currentUserId, exercise.getCreatedBy());
        
        // 验证请求参数
        if (request.getStartTime() != null && request.getEndTime() != null && 
            request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        // 更新字段
        if (request.getTitle() != null) {
            exercise.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            exercise.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            exercise.setLocation(request.getLocation());
        }
        if (request.getStartTime() != null) {
            exercise.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            exercise.setEndTime(request.getEndTime());
        }
        
        MorningExercise savedExercise = morningExerciseRepository.save(exercise);
        
        return new MorningExerciseResponse(savedExercise);
    }
    
    /**
     * 删除早操活动
     */
    @Transactional
    public void deleteMorningExercise(String id, String currentUserId) {
        MorningExercise exercise = morningExerciseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("早操活动不存在"));
        
        // 验证删除权限（只有创建者所在学院的院级管理员可以删除）
        permissionService.validateMorningExerciseModifyPermission(currentUserId, exercise.getCreatedBy());
        
        // 检查是否有关联的考勤记录
        List<MorningExerciseAttendance> attendances = attendanceRepository.findByExerciseId(id);
        if (!attendances.isEmpty()) {
            throw new RuntimeException("该早操活动已有考勤记录，无法删除");
        }
        
        morningExerciseRepository.deleteById(id);
    }
    
    /**
     * 获取早操考勤记录
     */
    public PageResponse<MorningExerciseAttendanceResponse> getMorningExerciseAttendance(
            String exerciseId, int page, int pageSize, String studentId, 
            Boolean isCheckedOut, String checkedBy, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 验证早操活动存在并获取活动信息
        MorningExercise exercise = morningExerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new RuntimeException("早操活动不存在"));
        
        // 对于院级管理员，检查是否是同学院的早操活动
        if (permissionService.hasMorningExercisePublishPermission(currentUserId)) {
            permissionService.validateMorningExerciseModifyPermission(currentUserId, exercise.getCreatedBy());
            
            // 院级管理员可以查看自己学院早操活动的所有考勤记录，无需进一步过滤
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            Page<MorningExerciseAttendance> attendancePage = 
                attendanceRepository.findByExerciseIdWithFilters(
                    exerciseId, studentId, isCheckedOut, checkedBy, 
                    null, null, pageable);
            
            List<MorningExerciseAttendanceResponse> responses = attendancePage.getContent().stream()
                .map(MorningExerciseAttendanceResponse::new)
                .collect(Collectors.toList());
            
            return new PageResponse<>(responses, attendancePage.getTotalElements(), page, pageSize);
        } else {
            // 校级管理员使用权限过滤
            String allowedSchool = permissionService.getAllowedSchool(currentUserId);
            String allowedCollege = permissionService.getAllowedCollege(currentUserId);
            
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            
            Page<MorningExerciseAttendance> attendancePage = 
                attendanceRepository.findByExerciseIdWithFilters(
                    exerciseId, studentId, isCheckedOut, checkedBy, 
                    allowedSchool, allowedCollege, pageable);
            
            List<MorningExerciseAttendanceResponse> responses = attendancePage.getContent().stream()
                .map(MorningExerciseAttendanceResponse::new)
                .collect(Collectors.toList());
            
            return new PageResponse<>(responses, attendancePage.getTotalElements(), page, pageSize);
        }
    }
    
    /**
     * 获取早操统计数据
     */
    public StatisticsResponse.MorningExerciseStatistics getMorningExerciseStatistics(
            String startDate, String endDate, String currentUserId) {
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 获取权限范围
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        
        List<Object[]> statistics = morningExerciseRepository.getMorningExerciseStatistics(
            start, end, allowedSchool, allowedCollege);
        
        if (statistics.isEmpty()) {
            return new StatisticsResponse.MorningExerciseStatistics(0L, 0L, 0L);
        }
        
        Object[] row = statistics.get(0);
        long total = ((Number) row[0]).longValue();
        long active = ((Number) row[1]).longValue();
        long completed = total - active;
        
        return new StatisticsResponse.MorningExerciseStatistics(total, active, completed);
    }

    /**
     * 早操出勤率统计大屏：按创建者院系对应 users1 中学生 roster，累计「人次」；
     * 出勤 = 该场次存在任一条考勤记录且已签退（isCheckedOut 或 checkOutTime 非空）。
     */
    public MorningExerciseAttendanceDashboardResponse getAttendanceDashboard(
            String startDateStr, String endDateStr, String currentUserId) {

        permissionService.validatePeManagementPermission(currentUserId);

        User admin = permissionService.getUser(currentUserId);
        String allowedSchool = permissionService.getAllowedSchool(currentUserId);
        String allowedCollege = permissionService.getAllowedCollege(currentUserId);

        LocalDate end = endDateStr != null && !endDateStr.isBlank()
            ? LocalDate.parse(endDateStr)
            : LocalDate.now();
        LocalDate start = startDateStr != null && !startDateStr.isBlank()
            ? LocalDate.parse(startDateStr)
            : end.minusDays(29);
        if (start.isAfter(end)) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }

        boolean showCollegeStats = admin.getUserType() == User.UserType.school_admin
            || admin.getUserType() == User.UserType.super_admin;

        List<MorningExercise> exercises = morningExerciseRepository.findForAttendanceDashboard(
            start, end, allowedSchool, allowedCollege);

        Set<String> creatorIds = exercises.stream()
            .map(MorningExercise::getCreatedBy)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<String, User> creators = userRepository.findAllById(creatorIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        Set<String> schoolsInScope = new HashSet<>();
        for (MorningExercise me : exercises) {
            User c = creators.get(me.getCreatedBy());
            if (c != null && c.getSchool() != null && !c.getSchool().isBlank()) {
                schoolsInScope.add(c.getSchool().trim());
            }
        }
        Map<String, List<PeUser>> studentsBySchool = new HashMap<>();
        for (String sch : schoolsInScope) {
            studentsBySchool.put(sch, peUserRepository.findBySchoolAndRole(sch, PeUser.Role.STUDENT));
        }

        Map<String, CollegeAccumulator> collegeAcc = new HashMap<>();
        Map<String, ClassAccumulator> classAcc = new HashMap<>();

        for (MorningExercise me : exercises) {
            User creator = creators.get(me.getCreatedBy());
            if (creator == null || creator.getSchool() == null || creator.getSchool().isBlank()) {
                continue;
            }
            String school = creator.getSchool().trim();
            String deptRaw = creator.getDepartmentName();
            if (deptRaw == null || deptRaw.isBlank()) {
                continue;
            }
            String collegeDisplay = deptRaw.trim();

            List<PeUser> schoolStudents = studentsBySchool.getOrDefault(school, List.of());
            List<PeUser> roster = filterStudentsByCreatorDept(schoolStudents, collegeDisplay);
            if (roster.isEmpty()) {
                continue;
            }

            List<MorningExerciseAttendance> atts = attendanceRepository.findByExerciseId(me.getId());

            String collegeKey = school + "\0" + normalizeCollegeKey(collegeDisplay);
            CollegeAccumulator ca = collegeAcc.computeIfAbsent(collegeKey,
                k -> new CollegeAccumulator(school, collegeDisplay));

            for (PeUser stu : roster) {
                ca.totalSlots++;
                boolean present = isStudentPresentForExercise(stu.getStudentId(), atts);
                if (present) {
                    ca.presentSlots++;
                } else {
                    ca.absentSlots++;
                }

                String cls = (stu.getClassName() == null || stu.getClassName().isBlank())
                    ? "（未分班）"
                    : stu.getClassName().trim();
                String classKey = school + "\0" + normalizeCollegeKey(collegeDisplay) + "\0" + cls;
                ClassAccumulator cla = classAcc.computeIfAbsent(classKey,
                    k -> new ClassAccumulator(school, collegeDisplay, cls));
                cla.totalSlots++;
                if (present) {
                    cla.presentSlots++;
                } else {
                    cla.absentSlots++;
                }
            }
        }

        MorningExerciseAttendanceDashboardResponse response = new MorningExerciseAttendanceDashboardResponse();
        response.setStartDate(start.toString());
        response.setEndDate(end.toString());
        response.setExerciseCount(exercises.size());
        response.setShowCollegeStats(showCollegeStats);
        response.setNotice(showCollegeStats ? NOTICE_SCHOOL_ADMIN : NOTICE_DEPT_ADMIN);

        if (showCollegeStats) {
            List<MorningExerciseAttendanceDashboardResponse.CollegeStatRow> collegeRows = new ArrayList<>();
            for (CollegeAccumulator ca : collegeAcc.values()) {
                MorningExerciseAttendanceDashboardResponse.CollegeStatRow row =
                    new MorningExerciseAttendanceDashboardResponse.CollegeStatRow();
                row.setSchoolName(ca.schoolName);
                row.setCollegeName(ca.collegeName);
                row.setStudentHeadcount(countStudentsInCollege(
                    studentsBySchool.getOrDefault(ca.schoolName, List.of()), ca.collegeName));
                row.setTotalSlots(ca.totalSlots);
                row.setPresentSlots(ca.presentSlots);
                row.setAbsentSlots(ca.absentSlots);
                row.setAttendanceRatePercent(rate(ca.presentSlots, ca.totalSlots));
                collegeRows.add(row);
            }
            collegeRows.sort(Comparator
                .comparing(MorningExerciseAttendanceDashboardResponse.CollegeStatRow::getSchoolName,
                    Comparator.nullsLast(String::compareTo))
                .thenComparing(MorningExerciseAttendanceDashboardResponse.CollegeStatRow::getCollegeName,
                    Comparator.nullsLast(String::compareTo)));
            response.setColleges(collegeRows);
        } else {
            response.setColleges(List.of());
        }

        List<MorningExerciseAttendanceDashboardResponse.ClassStatRow> classRows = new ArrayList<>();
        for (ClassAccumulator cla : classAcc.values()) {
            MorningExerciseAttendanceDashboardResponse.ClassStatRow row =
                new MorningExerciseAttendanceDashboardResponse.ClassStatRow();
            row.setSchoolName(cla.schoolName);
            row.setCollegeName(cla.collegeName);
            row.setClassName(cla.className);
            row.setStudentHeadcount(countStudentsInClass(
                studentsBySchool.getOrDefault(cla.schoolName, List.of()),
                cla.collegeName,
                cla.className));
            row.setTotalSlots(cla.totalSlots);
            row.setPresentSlots(cla.presentSlots);
            row.setAbsentSlots(cla.absentSlots);
            row.setAttendanceRatePercent(rate(cla.presentSlots, cla.totalSlots));
            classRows.add(row);
        }
        classRows.sort(Comparator
            .comparing(MorningExerciseAttendanceDashboardResponse.ClassStatRow::getSchoolName,
                Comparator.nullsLast(String::compareTo))
            .thenComparing(MorningExerciseAttendanceDashboardResponse.ClassStatRow::getCollegeName,
                Comparator.nullsLast(String::compareTo))
            .thenComparing(MorningExerciseAttendanceDashboardResponse.ClassStatRow::getClassName,
                Comparator.nullsLast(String::compareTo)));
        response.setClasses(classRows);

        return response;
    }

    private static double rate(long present, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round(present * 10000.0 / total) / 100.0;
    }

    private static boolean isFullyPresent(MorningExerciseAttendance a) {
        if (a == null) {
            return false;
        }
        if (Boolean.TRUE.equals(a.getIsCheckedOut())) {
            return true;
        }
        return a.getCheckOutTime() != null;
    }

    private static boolean isStudentPresentForExercise(String studentId, List<MorningExerciseAttendance> atts) {
        if (studentId == null || atts == null) {
            return false;
        }
        return atts.stream()
            .filter(a -> studentId.equals(a.getStudentId()))
            .anyMatch(MorningExerciseService::isFullyPresent);
    }

    private static String normalizeCollegeKey(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    /**
     * 与 PeStatisticsService 一致：先 trim+忽略大小写，再比较去空白后小写。
     */
    private static List<PeUser> filterStudentsByCreatorDept(List<PeUser> schoolStudents, String deptRaw) {
        if (deptRaw == null || deptRaw.isBlank()) {
            return List.of();
        }
        final String dept = deptRaw.trim();
        List<PeUser> match = schoolStudents.stream()
            .filter(u -> u.getCollege() != null && !u.getCollege().isBlank())
            .filter(u -> dept.equalsIgnoreCase(u.getCollege().trim()))
            .collect(Collectors.toList());
        if (!match.isEmpty()) {
            return match;
        }
        final String deptKey = normalizeCollegeKey(dept);
        return schoolStudents.stream()
            .filter(u -> u.getCollege() != null && !u.getCollege().isBlank())
            .filter(u -> normalizeCollegeKey(u.getCollege()).equals(deptKey))
            .collect(Collectors.toList());
    }

    private static long countStudentsInCollege(List<PeUser> schoolStudents, String collegeDisplay) {
        return filterStudentsByCreatorDept(schoolStudents, collegeDisplay).size();
    }

    private static long countStudentsInClass(List<PeUser> schoolStudents, String collegeDisplay, String classLabel) {
        List<PeUser> inCollege = filterStudentsByCreatorDept(schoolStudents, collegeDisplay);
        if (classLabel == null) {
            return 0;
        }
        if ("（未分班）".equals(classLabel)) {
            return inCollege.stream()
                .filter(u -> u.getClassName() == null || u.getClassName().isBlank())
                .count();
        }
        final String cls = classLabel.trim();
        return inCollege.stream()
            .filter(u -> u.getClassName() != null && cls.equalsIgnoreCase(u.getClassName().trim()))
            .count();
    }

    private static final class CollegeAccumulator {
        final String schoolName;
        final String collegeName;
        long totalSlots;
        long presentSlots;
        long absentSlots;

        CollegeAccumulator(String schoolName, String collegeName) {
            this.schoolName = schoolName;
            this.collegeName = collegeName;
        }
    }

    private static final class ClassAccumulator {
        final String schoolName;
        final String collegeName;
        final String className;
        long totalSlots;
        long presentSlots;
        long absentSlots;

        ClassAccumulator(String schoolName, String collegeName, String className) {
            this.schoolName = schoolName;
            this.collegeName = collegeName;
            this.className = className;
        }
    }
}
