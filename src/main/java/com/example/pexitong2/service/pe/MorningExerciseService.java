package com.example.pexitong2.service.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.entity.pe.MorningExercise;
import com.example.pexitong2.entity.pe.MorningExerciseAttendance;
import com.example.pexitong2.repository.pe.MorningExerciseRepository;
import com.example.pexitong2.repository.pe.MorningExerciseAttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        // 验证请求参数
        if (request.getDate() == null || request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("日期和时间参数不能为空");
        }
        
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        // 检查同一天是否已存在早操活动
        List<MorningExercise> existingExercises = morningExerciseRepository.findByDate(request.getDate());
        if (!existingExercises.isEmpty()) {
            throw new RuntimeException("该日期已存在早操活动");
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
        
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        MorningExercise exercise = morningExerciseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("早操活动不存在"));
        
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
        // 验证权限
        permissionService.validatePeManagementPermission(currentUserId);
        
        morningExerciseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("早操活动不存在"));
        
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
        
        // 验证早操活动存在
        morningExerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new RuntimeException("早操活动不存在"));
        
        // 获取权限范围
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
}
