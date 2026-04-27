package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.pe.TempClass;
import com.example.pexitong2.repository.pe.TempClassRepository;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<TempClass>>> getTempClasses(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String teacherId) {
        try {
            List<TempClass> classes;
            if (teacherId != null && !teacherId.isEmpty()) {
                classes = tempClassRepository.findByTeacherId(teacherId);
            } else if (school != null && semester != null) {
                classes = tempClassRepository.findBySchoolAndSemester(school, semester);
            } else {
                classes = tempClassRepository.findAll();
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
                    "SELECT e.id AS enrollment_id, e.student_id, e.enrolled_at, " +
                    "u.real_name, u.student_id AS student_number, u.school, u.department_name " +
                    "FROM temp_class_enrollments e " +
                    "LEFT JOIN users1 u ON e.student_id = u.id " +
                    "WHERE e.class_id = ? " +
                    "ORDER BY e.enrolled_at DESC",
                    classId);
            return ResponseEntity.ok(ApiResponse.success("获取选课学生列表成功", enrollments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
