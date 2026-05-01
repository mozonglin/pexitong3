package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.TempClass;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.pe.TempClassRepository;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pe/temp-classes")
public class TempClassController {

    @Autowired
    private TempClassRepository tempClassRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTempClasses(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String teacherId) {
        try {
            if (school == null || school.isEmpty()) {
                String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
                String userId = jwtUtil.extractUserId(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                school = user.getSchool();
            }
            String baseSql =
                    "SELECT tc.id, tc.schedule_id AS scheduleId, tc.teacher_id AS teacherId, " +
                    "tc.class_name AS className, tc.school, tc.semester, " +
                    "tc.capacity, tc.current_count AS currentCount, " +
                    "tc.day_of_week AS dayOfWeek, tc.start_time AS startTime, tc.end_time AS endTime, " +
                    "tc.location, " +
                    "COALESCE(u.real_name, s.teacher_name) AS teacherName, " +
                    "CASE WHEN tc.start_time IS NOT NULL AND tc.end_time IS NOT NULL THEN CONCAT(tc.start_time, '-', tc.end_time) ELSE '' END AS time, " +
                    "(SELECT COUNT(*) FROM temp_class_enrollments tce WHERE tce.temp_class_id = tc.id) AS enrolled " +
                    "FROM temp_classes tc " +
                    "LEFT JOIN users u ON tc.teacher_id = u.id " +
                    "LEFT JOIN pe_schedules s ON tc.schedule_id = s.id " +
                    "WHERE 1=1 ";
            List<Map<String, Object>> classes;
            if (teacherId != null && !teacherId.isEmpty()) {
                classes = jdbcTemplate.queryForList(baseSql + "AND tc.teacher_id = ? ORDER BY tc.created_at DESC", teacherId);
            } else if (school != null && !school.isEmpty() && semester != null && !semester.isEmpty()) {
                classes = jdbcTemplate.queryForList(baseSql + "AND tc.school = ? AND tc.semester = ? ORDER BY tc.created_at DESC", school, semester);
            } else if (school != null && !school.isEmpty()) {
                classes = jdbcTemplate.queryForList(baseSql + "AND tc.school = ? ORDER BY tc.created_at DESC", school);
            } else {
                classes = jdbcTemplate.queryForList(baseSql + "ORDER BY tc.created_at DESC");
            }
            for (Map<String, Object> row : classes) {
                String normalizedStart = normalizeTimeString(row.get("startTime") != null ? String.valueOf(row.get("startTime")) : null);
                String normalizedEnd = normalizeTimeString(row.get("endTime") != null ? String.valueOf(row.get("endTime")) : null);
                row.put("startTime", normalizedStart);
                row.put("endTime", normalizedEnd);
                if (normalizedStart != null && !normalizedStart.isBlank() && normalizedEnd != null && !normalizedEnd.isBlank()) {
                    row.put("time", normalizedStart + "-" + normalizedEnd);
                } else if (normalizedStart != null && !normalizedStart.isBlank()) {
                    row.put("time", normalizedStart);
                } else {
                    row.put("time", "");
                }
            }
            return ResponseEntity.ok(ApiResponse.success("获取临时班级列表成功", classes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/{classId}/enrollments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEnrollments(
            @PathVariable String classId) {
        try {
            List<Map<String, Object>> enrollments = jdbcTemplate.queryForList(
                    "SELECT e.id AS enrollmentId, e.enrolled_at AS enrolledAt, " +
                    "u.name AS studentName, COALESCE(u.student_id, e.student_id) AS studentId, u.school, u.college " +
                    "FROM temp_class_enrollments e " +
                    "LEFT JOIN users1 u ON e.student_id = u.student_id " +
                    "WHERE e.temp_class_id = ? " +
                    "ORDER BY e.enrolled_at DESC",
                    classId);
            return ResponseEntity.ok(ApiResponse.success("获取选课学生列表成功", enrollments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    private String normalizeTimeString(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        try {
            double excelValue = Double.parseDouble(raw.trim());
            double fraction = excelValue - Math.floor(excelValue);
            int totalSeconds = (int) Math.round(fraction * 24 * 60 * 60);
            if (totalSeconds >= 24 * 60 * 60) {
                totalSeconds %= (24 * 60 * 60);
            }
            return LocalTime.ofSecondOfDay(totalSeconds).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (NumberFormatException ignored) {
            return raw;
        }
    }
}
