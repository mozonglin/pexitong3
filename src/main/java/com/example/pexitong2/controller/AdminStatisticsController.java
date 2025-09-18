package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.dto.ObservationResponse;
import com.example.pexitong2.dto.StatisticsResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.ListeningObservationService;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/listening/admin")
@CrossOrigin(origins = "*")
public class AdminStatisticsController {
    
    @Autowired
    private ListeningObservationService observationService;
    
    @Autowired
    private UserRepository userRepository;
    
    // @Autowired
    // private DepartmentRepository departmentRepository; // 已删除
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取管理员听课记录（与普通听课记录接口类似，但权限更广泛）
     */
    @GetMapping("/observations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminObservations(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "department_name", required = false) String departmentName, // 修改为department_name
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        
        try {
            // 从JWT Token获取用户信息
            String token = extractToken(request);
            String username = jwtUtil.extractUsername(token);
            String userType = jwtUtil.extractUserType(token);
            
            System.out.println("管理员查询听课记录 - 用户名: " + username + ", 用户类型: " + userType + 
                             ", 院系: " + departmentName + ", 关键词: " + keyword);
            
            // 检查管理员权限
            if (!hasAdminPermission(userType)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "权限不足"));
            }
            
            // 获取用户ID（管理员查看需要正确的用户ID进行权限验证）
            String userId = getUserIdFromToken(token);
            
            // 构建分页参数
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 调用听课记录服务获取数据
            Page<ObservationResponse> observations = observationService.getObservations(
                userId, userType, keyword, departmentName, startDate, endDate, pageable);
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("list", observations.getContent());
            data.put("total", observations.getTotalElements());
            data.put("page", page);
            data.put("limit", limit);
            
            System.out.println("管理员查询结果 - 总记录数: " + observations.getTotalElements() + 
                             ", 当前页记录数: " + observations.getContent().size());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", data));
            
        } catch (Exception e) {
            System.out.println("管理员查询听课记录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics(
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(name = "department_id", required = false) Long departmentId,
            HttpServletRequest request) {
        
        try {
            // 从JWT Token获取用户信息
            String token = extractToken(request);
            String userType = jwtUtil.extractUserType(token);
            
            // 检查管理员权限
            if (!hasAdminPermission(userType)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "权限不足"));
            }
            
            // 构建统计数据
            StatisticsResponse stats = new StatisticsResponse();
            
            // 总听课记录数
            stats.setTotalObservations(observationService.getTotalObservationCount());
            
            // 本月听课记录数
            YearMonth currentMonth = YearMonth.now();
            LocalDate monthStart = currentMonth.atDay(1);
            LocalDate monthEnd = currentMonth.atEndOfMonth();
            stats.setThisMonthCount(observationService.getObservationCountByDateRange(monthStart, monthEnd));
            
            // 教师数量
            long teacherCount = userRepository.countByUserType(User.UserType.teacher);
            stats.setTeacherCount(teacherCount);
            
            // 评价文件数量
            stats.setEvaluationFiles(observationService.getEvaluationFileCount());
            
            // 视频文件数量
            stats.setVideoFiles(observationService.getVideoFileCount());
            
            // 院系统计数据（基于department_name）
            List<StatisticsResponse.DepartmentStatistics> departmentStats = new ArrayList<>();
            // 简化版本：可以通过查询users表的department_name来获取院系统计
            // 这里先返回空列表，可以后续扩展
            
            // TODO: 实现基于department_name的统计查询
            // 例如：SELECT department_name, COUNT(*) FROM users WHERE user_type='teacher' GROUP BY department_name
            
            stats.setDepartmentStats(departmentStats);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", stats));
            
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
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("未找到有效的授权令牌");
    }

    /**
     * 从JWT Token中获取用户ID
     */
    private String getUserIdFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在：" + username));
        return user.getId();
    }
    
    /**
     * 检查是否有管理员权限
     */
    private boolean hasAdminPermission(String userType) {
        return userType.equals("department_admin") || 
               userType.equals("school_admin") || 
               userType.equals("super_admin");
    }
} 