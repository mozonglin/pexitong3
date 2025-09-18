package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.dto.CreateObservationRequest;
import com.example.pexitong2.dto.FileInfoDTO;
import com.example.pexitong2.dto.ObservationResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.ListeningObservationService;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/listening/observations")
@CrossOrigin(origins = "*")
public class ListeningObservationController {
    
    @Autowired
    private ListeningObservationService observationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取听课记录列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getObservations(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "department_name", required = false) String departmentName,
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        
        try {
            // 从JWT Token获取用户信息
            String token = extractToken(request);
            String userId = getUserIdFromToken(token);
            String userType = jwtUtil.extractUserType(token);
            
            // 分页参数
            Pageable pageable = PageRequest.of(page - 1, limit);
            
            // 获取听课记录
            Page<ObservationResponse> observations = observationService.getObservations(
                userId, userType, keyword, departmentName, startDate, endDate, pageable);
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("items", observations.getContent());
            data.put("total", observations.getTotalElements());
            data.put("page", page);
            data.put("totalPages", observations.getTotalPages());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 创建听课记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createObservation(
            @Valid @RequestBody CreateObservationRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            System.out.println("收到创建听课记录请求: " + request.getClassDate() + ", courseId: " + request.getCourseId());
            
            // 从JWT Token获取用户信息
            String token = extractToken(httpRequest);
            System.out.println("提取到token: " + (token != null ? "有效" : "无效"));
            
            String username = jwtUtil.extractUsername(token);
            String userId = getUserIdFromToken(token);
            String userType = jwtUtil.extractUserType(token);
            System.out.println("用户名: " + username + ", 用户ID: " + userId + ", 用户类型: " + userType);
            
            // 检查权限
            if (!hasCreatePermission(userType)) {
                System.out.println("权限检查失败，用户类型: " + userType);
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "权限不足"));
            }
            
            // 创建听课记录 - 使用用户ID
            ObservationResponse observation = observationService.createObservation(userId, request);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", observation.getId());
            data.put("created_at", observation.getCreatedAt());
            
            return ResponseEntity.ok(ApiResponse.success("创建成功", data));
            
        } catch (IllegalArgumentException e) {
            System.out.println("参数错误: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "参数错误: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("创建听课记录时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取听课记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ObservationResponse>> getObservationById(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        try {
            // 从JWT Token获取用户信息
            String token = extractToken(request);
            String userId = getUserIdFromToken(token);
            String userType = jwtUtil.extractUserType(token);
            
            // 获取听课记录详情
            Optional<ObservationResponse> observationOpt = observationService.getObservationById(id, userId, userType);
            
            if (observationOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, "听课记录不存在"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", observationOpt.get()));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 删除听课记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteObservation(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        try {
            // 从JWT Token获取用户信息
            String token = extractToken(request);
            String userType = jwtUtil.extractUserType(token);
            
            // 删除听课记录
            observationService.deleteObservation(id, userType);
            
            return ResponseEntity.ok(ApiResponse.<Void>success("删除成功", null));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 上传评价文件
     */
    @PostMapping("/{id}/evaluation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadEvaluationFile(
            @PathVariable Long id,
            @RequestParam("evaluation_file") MultipartFile file,
            HttpServletRequest request) {
        
        try {
            // 从JWT Token获取用户信息
            String token = extractToken(request);
            String userId = getUserIdFromToken(token);
            String userType = jwtUtil.extractUserType(token);
            
            // 上传评价文件
            ObservationResponse observation = observationService.uploadEvaluationFile(id, file, userId, userType);
            
            Map<String, Object> data = new HashMap<>();
            data.put("file", observation.getEvaluationFile());
            
            return ResponseEntity.ok(ApiResponse.success("上传成功", data));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 上传听课视频
     */
    @PostMapping("/{id}/video")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadVideoFile(
            @PathVariable Long id,
            @RequestParam("video_file") MultipartFile file,
            HttpServletRequest request) {
        
        try {
            // 从JWT Token获取用户信息
            String token = extractToken(request);
            String userId = getUserIdFromToken(token);
            String userType = jwtUtil.extractUserType(token);
            
            // 上传视频文件
            ObservationResponse observation = observationService.uploadVideoFile(id, file, userId, userType);
            
            Map<String, Object> data = new HashMap<>();
            data.put("file", observation.getVideoFile());
            
            return ResponseEntity.ok(ApiResponse.success("上传成功", data));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 从请求中提取JWT Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + bearerToken);
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            System.out.println("提取的Token长度: " + token.length());
            return token;
        }
        throw new IllegalArgumentException("未找到有效的授权令牌或格式不正确");
    }
    
    /**
     * 根据JWT token获取用户ID
     */
    private String getUserIdFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在：" + username));
        return user.getId();
    }
    
    /**
     * 检查是否有创建权限
     */
    private boolean hasCreatePermission(String userType) {
        return "teacher".equals(userType) || 
               "department_admin".equals(userType) || 
               "school_admin".equals(userType) || 
               "super_admin".equals(userType);
    }
} 