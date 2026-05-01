package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pe/class-selection")
public class ClassSelectionController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/window")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createWindow(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            String userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String id = UUID.randomUUID().toString();
            String school = request.get("school") != null ? String.valueOf(request.get("school")) : null;
            if (school == null || school.isBlank()) {
                school = user.getSchool();
            }
            String semester = (String) request.get("semester");
            String openTime = request.get("openTime") != null ? String.valueOf(request.get("openTime")) : (String) request.get("startTime");
            String closeTime = request.get("closeTime") != null ? String.valueOf(request.get("closeTime")) : (String) request.get("endTime");
            Boolean isActive = request.get("isActive") != null ? (Boolean) request.get("isActive") : true;

            jdbcTemplate.update(
                    "INSERT INTO class_selection_windows (id, school, semester, open_time, close_time, is_active, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    id, school, semester, openTime, closeTime, isActive, userId);

            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT id, school, semester, open_time AS startTime, close_time AS endTime, is_active AS isActive, created_by AS createdBy, created_at AS createdAt FROM class_selection_windows WHERE id = ?",
                    id);

            return ResponseEntity.ok(ApiResponse.success("创建选课窗口成功", result.isEmpty() ? null : result.get(0)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/window")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWindow(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String semester) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String resolvedSchool = (school != null && !school.isBlank()) ? school : user.getSchool();

            List<Map<String, Object>> results;
            if (semester != null && !semester.isBlank()) {
                results = jdbcTemplate.queryForList(
                        "SELECT id, school, semester, open_time AS startTime, close_time AS endTime, is_active AS isActive, created_by AS createdBy, created_at AS createdAt " +
                                "FROM class_selection_windows WHERE school = ? AND semester = ? ORDER BY created_at DESC LIMIT 1",
                        resolvedSchool, semester);
            } else {
                results = jdbcTemplate.queryForList(
                        "SELECT id, school, semester, open_time AS startTime, close_time AS endTime, is_active AS isActive, created_by AS createdBy, created_at AS createdAt " +
                                "FROM class_selection_windows WHERE school = ? ORDER BY created_at DESC LIMIT 1",
                        resolvedSchool);
            }

            if (results.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("暂无选课窗口", null));
            }
            return ResponseEntity.ok(ApiResponse.success("获取选课窗口成功", results.get(0)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/window/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateWindow(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            StringBuilder sql = new StringBuilder("UPDATE class_selection_windows SET ");
            List<Object> params = new java.util.ArrayList<>();

            if (request.containsKey("openTime")) {
                sql.append("open_time = ?, ");
                params.add(request.get("openTime"));
            } else if (request.containsKey("startTime")) {
                sql.append("open_time = ?, ");
                params.add(request.get("startTime"));
            }
            if (request.containsKey("closeTime")) {
                sql.append("close_time = ?, ");
                params.add(request.get("closeTime"));
            } else if (request.containsKey("endTime")) {
                sql.append("close_time = ?, ");
                params.add(request.get("endTime"));
            }
            if (request.containsKey("isActive")) {
                sql.append("is_active = ?, ");
                params.add(request.get("isActive"));
            }

            if (params.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "没有需要更新的字段"));
            }

            sql.setLength(sql.length() - 2);
            sql.append(" WHERE id = ?");
            params.add(id);

            jdbcTemplate.update(sql.toString(), params.toArray());

            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT id, school, semester, open_time AS startTime, close_time AS endTime, is_active AS isActive, created_by AS createdBy, created_at AS createdAt FROM class_selection_windows WHERE id = ?",
                    id);

            return ResponseEntity.ok(ApiResponse.success("更新选课窗口成功", result.isEmpty() ? null : result.get(0)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
