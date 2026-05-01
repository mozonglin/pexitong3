package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.HomeworkAssignment;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.pe.HomeworkAssignmentService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pe/homework-assignments")
@CrossOrigin(origins = "*")
public class HomeworkAssignmentController {

    @Autowired
    private HomeworkAssignmentService homeworkAssignmentService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("")
    public ResponseEntity<ApiResponse<HomeworkAssignment>> createAssignment(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            String userType = jwtUtil.extractUserType(token);
            String userId = jwtUtil.extractUserId(token);

            if (!"teacher".equals(userType) && !"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "权限不足：仅教师或管理员可创建作业"));
            }

            String teacherId = (String) body.getOrDefault("teacherId", userId);
            String tempClassId = (String) body.get("tempClassId");
            String school = (String) body.get("school");
            if (school == null || school.isBlank()) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    school = user.getSchool();
                }
            }
            String title = (String) body.get("title");
            String exerciseType = (String) body.get("exerciseType");
            Integer requiredCount = body.get("requiredCount") != null
                    ? ((Number) body.get("requiredCount")).intValue() : null;
            LocalDateTime startTime = body.get("startTime") != null
                    ? LocalDateTime.parse((String) body.get("startTime")) : null;
            LocalDateTime deadline = body.get("deadline") != null
                    ? LocalDateTime.parse((String) body.get("deadline")) : null;

            HomeworkAssignment assignment = homeworkAssignmentService.createAssignment(
                    teacherId, tempClassId, school, title, exerciseType, requiredCount, startTime, deadline);
            return ResponseEntity.ok(ApiResponse.success("作业创建成功", assignment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAssignments(
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String school,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            String userType = jwtUtil.extractUserType(token);
            String userId = jwtUtil.extractUserId(token);

            List<Map<String, Object>> assignments;
            if ("teacher".equals(userType)) {
                assignments = homeworkAssignmentService.getAssignmentRowsByTeacher(
                        teacherId != null ? teacherId : userId);
            } else if (school != null) {
                assignments = homeworkAssignmentService.getAssignmentRowsBySchool(school);
            } else {
                assignments = homeworkAssignmentService.getAssignmentRowsByTeacher(userId);
            }
            return ResponseEntity.ok(ApiResponse.success("获取作业列表成功", assignments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/{id}/completions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompletions(
            @PathVariable String id) {
        try {
            List<Map<String, Object>> completions = homeworkAssignmentService.getAssignmentCompletions(id);
            return ResponseEntity.ok(ApiResponse.success("获取完成情况成功", completions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HomeworkAssignment>> updateAssignment(
            @PathVariable String id,
            @RequestBody HomeworkAssignment updates,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            String userType = jwtUtil.extractUserType(token);

            if (!"teacher".equals(userType) && !"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "权限不足"));
            }

            HomeworkAssignment updated = homeworkAssignmentService.updateAssignment(id, updates);
            return ResponseEntity.ok(ApiResponse.success("作业更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            String userType = jwtUtil.extractUserType(token);

            if (!"teacher".equals(userType) && !"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "权限不足"));
            }

            homeworkAssignmentService.deleteAssignment(id);
            return ResponseEntity.ok(ApiResponse.success("作业删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/completion-dashboard")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompletionDashboard(
            @RequestParam String school,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            List<String> counselorClasses = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String userType = jwtUtil.extractUserType(token);
                if ("counselor".equals(userType)) {
                    String userId = jwtUtil.extractUserId(token);
                    counselorClasses = homeworkAssignmentService.getCounselorClassNames(userId);
                }
            }
            List<Map<String, Object>> dashboard = homeworkAssignmentService.getCompletionDashboard(school);
            if (counselorClasses != null && !counselorClasses.isEmpty()) {
                final List<String> allowed = counselorClasses;
                dashboard = dashboard.stream()
                    .filter(row -> {
                        Object cn = row.get("class_name");
                        return cn != null && allowed.contains(cn.toString());
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            return ResponseEntity.ok(ApiResponse.success("获取完成情况统计成功", dashboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
