package com.example.pexitong2.service.teaching;

import com.example.pexitong2.dto.teaching.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.teaching.TeacherAttendanceRecord;
import com.example.pexitong2.entity.teaching.TeacherAttendancePhoto;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.teaching.TeacherAttendanceRecordRepository;
import com.example.pexitong2.repository.teaching.TeacherAttendancePhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeachingAttendanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(TeachingAttendanceService.class);
    
    @Autowired
    private TeacherAttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private TeacherAttendancePhotoRepository attendancePhotoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取课程列表（支持分页、筛选和搜索）
     */
    public TeachingPageResponse<CourseAttendanceResponse> getCourses(
            Integer page, Integer pageSize, String date, String status, String search, User currentUser) {
        
        // 参数验证和默认值设置
        page = page != null && page > 0 ? page : 1;
        pageSize = pageSize != null && pageSize > 0 ? pageSize : 12;
        
        // 权限检查
        String userSchool = getUserSchoolScope(currentUser);
        
        // 日期解析
        LocalDate queryDate = null;
        if (date != null && !date.trim().isEmpty()) {
            try {
                queryDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date format: {}", date);
                throw new IllegalArgumentException("日期格式错误，请使用YYYY-MM-DD格式");
            }
        }
        
        // 状态解析
        TeacherAttendanceRecord.AttendanceStatus attendanceStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                attendanceStatus = TeacherAttendanceRecord.AttendanceStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status: {}", status);
                throw new IllegalArgumentException("签到状态参数错误");
            }
        }
        
        // 搜索关键词处理
        String searchKeyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        // 分页查询
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<TeacherAttendanceRecord> recordPage = attendanceRecordRepository.findWithFilters(
                queryDate, attendanceStatus, searchKeyword, userSchool, pageable);
        
        // 转换为响应DTO
        List<CourseAttendanceResponse> responseList = recordPage.getContent()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new TeachingPageResponse<>(
                responseList, 
                recordPage.getTotalElements(), 
                page, 
                pageSize
        );
    }
    
    /**
     * 获取课程详情
     */
    public CourseAttendanceResponse getCourseDetail(String courseId, User currentUser) {
        // 解析课程ID
        Long id = parseCourseId(courseId);
        
        // 查找签到记录
        Optional<TeacherAttendanceRecord> recordOpt = attendanceRecordRepository.findByCourseId(id);
        if (!recordOpt.isPresent()) {
            throw new IllegalArgumentException("课程签到记录不存在");
        }
        
        TeacherAttendanceRecord record = recordOpt.get();
        
        // 权限检查
        checkAccessPermission(record, currentUser);
        
        // 加载照片信息
        List<TeacherAttendancePhoto> photos = attendancePhotoRepository.findByCourseIdOrderByUploadTimeAsc(id);
        record.setPhotos(photos);
        
        return convertToResponse(record);
    }
    
    /**
     * 获取统计数据
     */
    public TeachingStatisticsResponse getStatistics(String date, String period, User currentUser) {
        // 日期解析，默认今天
        LocalDate queryDate = LocalDate.now();
        if (date != null && !date.trim().isEmpty()) {
            try {
                queryDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date format: {}, using today", date);
            }
        }
        
        // 权限检查
        String userSchool = getUserSchoolScope(currentUser);
        
        // 查询统计数据
        List<Object[]> statusCounts = attendanceRecordRepository.countByStatusAndFilters(
                queryDate, userSchool);
        
        // 统计教师数量
        Long totalTeachers;
        if (userSchool != null) {
            totalTeachers = userRepository.countByUserTypeAndSchool(User.UserType.teacher, userSchool);
        } else {
            totalTeachers = userRepository.countByUserType(User.UserType.teacher);
        }
        
        // 处理统计结果
        Map<String, Integer> statusMap = new HashMap<>();
        statusCounts.forEach(result -> {
            String statusName = ((TeacherAttendanceRecord.AttendanceStatus) result[0]).name();
            Integer count = ((Number) result[1]).intValue();
            statusMap.put(statusName, count);
        });
        
        int totalCourses = statusMap.values().stream().mapToInt(Integer::intValue).sum();
        int completedCount = statusMap.getOrDefault("completed", 0);
        int lateCount = statusMap.getOrDefault("late", 0);
        int pendingCount = statusMap.getOrDefault("pending", 0);
        int missedCount = statusMap.getOrDefault("missed", 0);
        
        return new TeachingStatisticsResponse(
                totalCourses, completedCount, lateCount, pendingCount, missedCount, 
                totalTeachers.intValue()
        );
    }
    
    /**
     * 获取签到照片
     */
    public AttendancePhotoResponse getAttendancePhoto(String courseId, User currentUser) {
        Long id = parseCourseId(courseId);
        
        // 权限检查
        TeacherAttendanceRecord record = attendanceRecordRepository.findByCourseId(id)
                .orElseThrow(() -> new IllegalArgumentException("课程签到记录不存在"));
        
        checkAccessPermission(record, currentUser);
        
        // 查找第一张照片
        TeacherAttendancePhoto photo = attendancePhotoRepository.findFirstPhotoByCourseId(id);
        if (photo == null) {
            throw new IllegalArgumentException("该课程没有签到照片");
        }
        
        return new AttendancePhotoResponse(photo);
    }
    
    /**
     * 获取签到照片列表
     */
    public List<AttendancePhotoResponse> getAttendancePhotos(String courseId, User currentUser) {
        Long id = parseCourseId(courseId);
        
        // 权限检查
        TeacherAttendanceRecord record = attendanceRecordRepository.findByCourseId(id)
                .orElseThrow(() -> new IllegalArgumentException("课程签到记录不存在"));
        
        checkAccessPermission(record, currentUser);
        
        // 查找所有照片
        List<TeacherAttendancePhoto> photos = attendancePhotoRepository.findByCourseIdOrderByUploadTimeAsc(id);
        
        return photos.stream()
                .map(AttendancePhotoResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取签到历史记录
     */
    public TeachingPageResponse<AttendanceHistoryResponse> getAttendanceHistory(
            String courseId, String teacherId, String period, String startDate, String endDate,
            Integer page, Integer pageSize, User currentUser) {
        
        // 参数验证和默认值设置
        page = page != null && page > 0 ? page : 1;
        pageSize = pageSize != null && pageSize > 0 ? pageSize : 20;
        
        // 权限检查
        String userSchool = getUserSchoolScope(currentUser);
        
        // 日期范围解析
        LocalDate start = null;
        LocalDate end = null;
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                start = LocalDate.parse(startDate);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("开始日期格式错误");
            }
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                end = LocalDate.parse(endDate);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("结束日期格式错误");
            }
        }
        
        // 如果没有指定日期范围，根据period设置默认范围
        if (start == null || end == null) {
            LocalDate today = LocalDate.now();
            if ("week".equals(period)) {
                start = today.minusDays(today.getDayOfWeek().getValue() - 1); // 本周一
                end = start.plusDays(6); // 本周日
            } else if ("month".equals(period)) {
                start = today.withDayOfMonth(1); // 本月第一天
                end = today.withDayOfMonth(today.lengthOfMonth()); // 本月最后一天
            } else {
                // 默认查询最近一个月
                start = today.minusMonths(1);
                end = today;
            }
        }
        
        // 课程ID解析
        Long courseIdLong = null;
        if (courseId != null && !courseId.trim().isEmpty()) {
            courseIdLong = parseCourseId(courseId);
        }
        
        // 分页查询
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<TeacherAttendanceRecord> recordPage = attendanceRecordRepository.findByDateRange(
                start, end, courseIdLong, teacherId, userSchool, pageable);
        
        // 转换为响应DTO
        List<AttendanceHistoryResponse> responseList = recordPage.getContent()
                .stream()
                .map(AttendanceHistoryResponse::new)
                .collect(Collectors.toList());
        
        return new TeachingPageResponse<>(
                responseList,
                recordPage.getTotalElements(),
                page,
                pageSize
        );
    }
    
    // ============== 私有辅助方法 ==============
    
    /**
     * 转换为响应DTO
     */
    private CourseAttendanceResponse convertToResponse(TeacherAttendanceRecord record) {
        CourseAttendanceResponse response = new CourseAttendanceResponse(record);
        
        // 如果需要照片URL，手动查询第一张照片
        if (record.getPhotos() == null || record.getPhotos().isEmpty()) {
            TeacherAttendancePhoto firstPhoto = attendancePhotoRepository.findFirstPhotoByCourseId(record.getCourseId());
            if (firstPhoto != null) {
                response.setAttendancePhotoUrl("/api/teaching/files/" + firstPhoto.getFileName());
            }
        }
        
        return response;
    }
    
    /**
     * 解析课程ID
     */
    private Long parseCourseId(String courseId) {
        try {
            if (courseId.startsWith("course_")) {
                return Long.parseLong(courseId.substring(7));
            } else {
                return Long.parseLong(courseId);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("课程ID格式错误");
        }
    }
    
    /**
     * 获取用户学校权限范围
     */
    private String getUserSchoolScope(User currentUser) {
        if (currentUser.getUserType() == User.UserType.super_admin) {
            return null; // 超级管理员可以查看所有学校
        } else if (currentUser.getUserType() == User.UserType.school_admin) {
            return currentUser.getSchool(); // 校级管理员只能查看本校
        } else {
            throw new SecurityException("权限不足，只有管理员才能查看教师签到信息");
        }
    }
    
    /**
     * 检查访问权限
     */
    private void checkAccessPermission(TeacherAttendanceRecord record, User currentUser) {
        String userSchool = getUserSchoolScope(currentUser);
        
        // 如果是校级管理员，需要检查学校范围
        if (userSchool != null) {
            // 查询教师的学校信息
            Optional<User> teacherOpt = userRepository.findById(record.getTeacherId());
            if (teacherOpt.isPresent() && !userSchool.equals(teacherOpt.get().getSchool())) {
                throw new SecurityException("权限不足，无法查看其他学校的数据");
            }
        }
    }
}
