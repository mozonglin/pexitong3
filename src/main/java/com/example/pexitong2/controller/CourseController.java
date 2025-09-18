package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.dto.CourseSearchResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.service.CourseService;
import com.example.pexitong2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/listening/courses")
@CrossOrigin(origins = "*")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 搜索课程
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseSearchResponse>>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Authentication authentication) {
        
        try {
            // 获取当前用户信息
            String currentUserId = (String) authentication.getDetails();
            User currentUser = userService.getUserById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            
            // 只搜索本校老师的课程
            List<CourseSearchResponse> courses = courseService.searchCourses(keyword, date, currentUser.getSchool());
            return ResponseEntity.ok(ApiResponse.success("搜索成功", courses));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 根据教师姓名和日期搜索课程
     */
    @GetMapping("/search/teacher")
    public ResponseEntity<ApiResponse<List<CourseSearchResponse>>> searchCoursesByTeacher(
            @RequestParam String teacherName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Authentication authentication) {
        
        try {
            // 获取当前用户信息
            String currentUserId = (String) authentication.getDetails();
            User currentUser = userService.getUserById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            
            // 只搜索本校老师的课程
            List<CourseSearchResponse> courses = courseService.searchCoursesByTeacherAndDate(teacherName, date, currentUser.getSchool());
            return ResponseEntity.ok(ApiResponse.success("搜索成功", courses));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 根据日期获取课程列表
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<CourseSearchResponse>>> getCoursesByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Authentication authentication) {
        
        try {
            // 获取当前用户信息
            String currentUserId = (String) authentication.getDetails();
            User currentUser = userService.getUserById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            
            // 只获取本校老师的课程
            List<CourseSearchResponse> courses = courseService.getCoursesByDate(date, currentUser.getSchool());
            return ResponseEntity.ok(ApiResponse.success("获取成功", courses));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    // 删除：根据院系获取课程列表（不再需要department_id）
} 