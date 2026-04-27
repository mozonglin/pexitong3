package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/counselor")
@CrossOrigin(origins = "*")
public class CounselorController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/assign-classes")
    public ApiResponse<?> assignClasses(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userId = jwtUtil.extractUserId(token);
            String userType = jwtUtil.extractUserType(token);

            if (!"department_admin".equals(userType) && !"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ApiResponse.error(403, "权限不足：只有院级及以上管理员可以分配班级");
            }

            String counselorId = (String) body.get("counselorId");
            @SuppressWarnings("unchecked")
            List<String> classNames = (List<String>) body.get("classNames");
            String school = (String) body.get("school");
            String departmentName = (String) body.get("departmentName");

            if (counselorId == null || classNames == null || classNames.isEmpty()) {
                return ApiResponse.error("counselorId 和 classNames 不能为空");
            }

            for (String className : classNames) {
                String id = UUID.randomUUID().toString();
                jdbcTemplate.update(
                    "INSERT INTO counselor_class_assignments (id, counselor_id, class_name, school, department_name, assigned_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    id, counselorId, className, school, departmentName, userId, LocalDateTime.now()
                );
            }

            return ApiResponse.success("班级分配成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/my-classes")
    public ApiResponse<?> getMyClasses(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userId = jwtUtil.extractUserId(token);

            List<Map<String, Object>> classes = jdbcTemplate.queryForList(
                "SELECT * FROM counselor_class_assignments WHERE counselor_id = ?",
                userId
            );

            return ApiResponse.success("获取成功", classes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/classes")
    public ApiResponse<?> getCounselorClasses(@PathVariable String id) {
        try {
            List<Map<String, Object>> classes = jdbcTemplate.queryForList(
                "SELECT * FROM counselor_class_assignments WHERE counselor_id = ?",
                id
            );

            return ApiResponse.success("获取成功", classes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{counselorId}/classes")
    public ApiResponse<?> removeAllClasses(
            @PathVariable String counselorId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userType = jwtUtil.extractUserType(token);
            if (!"department_admin".equals(userType) && !"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ApiResponse.error(403, "权限不足");
            }
            jdbcTemplate.update("DELETE FROM counselor_class_assignments WHERE counselor_id = ?", counselorId);
            return ApiResponse.success("已清除全部班级分配", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<?> listCounselors(@RequestParam String school) {
        try {
            List<Map<String, Object>> counselors = jdbcTemplate.queryForList(
                "SELECT * FROM users WHERE user_type = 'counselor' AND school = ?",
                school
            );

            return ApiResponse.success("获取成功", counselors);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
