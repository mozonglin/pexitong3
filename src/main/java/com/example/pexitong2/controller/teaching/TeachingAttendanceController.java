package com.example.pexitong2.controller.teaching;

import com.example.pexitong2.dto.teaching.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.service.teaching.TeachingAttendanceService;
import com.example.pexitong2.service.UserService;
import com.example.pexitong2.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/teaching")
@CrossOrigin(origins = "*")
public class TeachingAttendanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(TeachingAttendanceController.class);
    
    @Autowired
    private TeachingAttendanceService teachingAttendanceService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 1.1 获取课程列表
     * GET /teaching/courses
     */
    @GetMapping("/courses")
    public ResponseEntity<TeachingApiResponse<List<CourseAttendanceResponse>>> getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(request);
            
            // 调用服务
            TeachingPageResponse<CourseAttendanceResponse> result = 
                teachingAttendanceService.getCourses(page, pageSize, date, status, search, currentUser);
            
            // 创建符合API文档的响应格式
            return ResponseEntity.ok(TeachingApiResponse.success(
                result.getData(), 
                result.getTotal(), 
                result.getPage(), 
                result.getPageSize()
            ));
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(TeachingApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TeachingApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting courses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TeachingApiResponse.error(500, "获取课程列表失败"));
        }
    }
    
    /**
     * 1.2 获取课程详情
     * GET /teaching/courses/{courseId}
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<TeachingApiResponse<CourseAttendanceResponse>> getCourseDetail(
            @PathVariable String courseId,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(request);
            
            // 调用服务
            CourseAttendanceResponse result = 
                teachingAttendanceService.getCourseDetail(courseId, currentUser);
            
            return ResponseEntity.ok(TeachingApiResponse.success(result));
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(TeachingApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid course ID: {}", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(TeachingApiResponse.error(404, "课程不存在"));
        } catch (Exception e) {
            logger.error("Error getting course detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TeachingApiResponse.error(500, "获取课程详情失败"));
        }
    }
    
    /**
     * 1.3 获取统计数据
     * GET /teaching/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<TeachingApiResponse<TeachingStatisticsResponse>> getStatistics(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "day") String period,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(request);
            
            // 调用服务
            TeachingStatisticsResponse result = 
                teachingAttendanceService.getStatistics(date, period, currentUser);
            
            return ResponseEntity.ok(TeachingApiResponse.success(result));
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(TeachingApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TeachingApiResponse.error(500, "获取统计数据失败"));
        }
    }
    
    /**
     * 2.1 获取单个课程签到照片
     * GET /teaching/courses/{courseId}/attendance/photo
     */
    @GetMapping("/courses/{courseId}/attendance/photo")
    public ResponseEntity<TeachingApiResponse<AttendancePhotoResponse>> getAttendancePhoto(
            @PathVariable String courseId,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(request);
            
            // 调用服务
            AttendancePhotoResponse result = 
                teachingAttendanceService.getAttendancePhoto(courseId, currentUser);
            
            return ResponseEntity.ok(TeachingApiResponse.success(result));
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(TeachingApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Photo not found for course: {}", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(TeachingApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting attendance photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TeachingApiResponse.error(500, "获取签到照片失败"));
        }
    }
    
    /**
     * 2.2 获取课程签到照片列表
     * GET /teaching/courses/{courseId}/attendance/photos
     */
    @GetMapping("/courses/{courseId}/attendance/photos")
    public ResponseEntity<TeachingApiResponse<List<AttendancePhotoResponse>>> getAttendancePhotos(
            @PathVariable String courseId,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(request);
            
            // 调用服务
            List<AttendancePhotoResponse> result = 
                teachingAttendanceService.getAttendancePhotos(courseId, currentUser);
            
            return ResponseEntity.ok(TeachingApiResponse.success(result));
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(TeachingApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Photos not found for course: {}", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(TeachingApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting attendance photos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TeachingApiResponse.error(500, "获取签到照片列表失败"));
        }
    }
    
    /**
     * 2.3 下载签到照片压缩包
     * GET /teaching/courses/{courseId}/attendance/photos/download
     */
    @GetMapping("/courses/{courseId}/attendance/photos/download")
    public ResponseEntity<Object> downloadAttendancePhotos(
            @PathVariable String courseId,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户并进行权限检查
            getCurrentUser(request);
            
            // TODO: 实现照片打包下载功能
            // 这里应该调用文件服务生成ZIP文件并返回
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(null);
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            logger.error("Error downloading attendance photos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 2.4 下载签到报告
     * GET /teaching/courses/{courseId}/attendance/report
     */
    @GetMapping("/courses/{courseId}/attendance/report")
    public ResponseEntity<Object> downloadAttendanceReport(
            @PathVariable String courseId,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户并进行权限检查
            getCurrentUser(request);
            
            // TODO: 实现报告生成和下载功能
            // 这里应该调用报告服务生成PDF文件并返回
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(null);
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            logger.error("Error downloading attendance report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 3.1 获取签到历史记录
     * GET /teaching/attendance/history
     */
    @GetMapping("/attendance/history")
    public ResponseEntity<TeachingApiResponse<List<AttendanceHistoryResponse>>> getAttendanceHistory(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String teacherId,
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(request);
            
            // 调用服务
            TeachingPageResponse<AttendanceHistoryResponse> result = 
                teachingAttendanceService.getAttendanceHistory(
                    courseId, teacherId, period, startDate, endDate, page, pageSize, currentUser);
            
            return ResponseEntity.ok(TeachingApiResponse.success(
                result.getData(), 
                result.getTotal(), 
                result.getPage(), 
                result.getPageSize()
            ));
            
        } catch (SecurityException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(TeachingApiResponse.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TeachingApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting attendance history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TeachingApiResponse.error(500, "获取签到历史失败"));
        }
    }
    

    
    // ============== 私有辅助方法 ==============
    
    /**
     * 获取当前用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new SecurityException("未提供认证令牌");
        }
        
        try {
            String userId = jwtUtil.extractUserId(token);
            logger.debug("Extracted user ID from token: {}", userId);
            
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new SecurityException("用户不存在"));
            
            logger.debug("Found user: {} with type: {} and school: {}", 
                    user.getRealName(), user.getUserType(), user.getSchool());
            
            // 权限检查：只有校级管理员和超级管理员可以访问教学签到功能
            if (user.getUserType() != User.UserType.school_admin && 
                user.getUserType() != User.UserType.super_admin) {
                logger.warn("User {} with type {} tried to access teaching system", 
                        user.getRealName(), user.getUserType());
                throw new SecurityException("权限不足，只有校级管理员和超级管理员才能查看教师签到信息");
            }
            
            return user;
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            throw new SecurityException("认证令牌无效或权限不足");
        }
    }
    
    /**
     * 从请求中提取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
