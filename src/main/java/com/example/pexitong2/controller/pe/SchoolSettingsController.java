package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.pe.HomeworkExerciseStandard;
import com.example.pexitong2.entity.pe.SchoolSettings;
import com.example.pexitong2.repository.pe.HomeworkExerciseStandardRepository;
import com.example.pexitong2.service.pe.SchoolSettingsService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pe/school-settings")
@CrossOrigin(origins = "*")
public class SchoolSettingsController {

    @Autowired
    private SchoolSettingsService schoolSettingsService;

    @Autowired
    private HomeworkExerciseStandardRepository homeworkExerciseStandardRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<SchoolSettings> getSchoolSettings(@RequestParam String school) {
        try {
            SchoolSettings settings = schoolSettingsService.getOrCreateSettings(school);
            return ApiResponse.success("获取成功", settings);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PutMapping("/sunshine-run-distance")
    public ApiResponse<SchoolSettings> updateSunshineRunDistance(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userType = jwtUtil.extractUserType(token);

            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ApiResponse.error(403, "权限不足：仅校级管理员或超级管理员可操作");
            }

            String school = (String) body.get("school");
            int distance = ((Number) body.get("distance")).intValue();

            SchoolSettings settings = schoolSettingsService.updateSunshineRunDistance(school, distance);
            return ApiResponse.success("更新成功", settings);
        } catch (ClassCastException | NullPointerException e) {
            return ApiResponse.error(400, "请求参数错误：需要 school(String) 和 distance(int)");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PutMapping("/sunshine-run-settings")
    public ApiResponse<SchoolSettings> updateSunshineRunSettings(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userType = jwtUtil.extractUserType(token);

            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ApiResponse.error(403, "权限不足：仅校级管理员或超级管理员可操作");
            }

            String school = (String) body.get("school");
            if (school == null || school.isBlank()) {
                return ApiResponse.error(400, "school 不能为空");
            }

            Integer distanceMale = body.get("distanceMale") != null ? ((Number) body.get("distanceMale")).intValue() : null;
            Integer distanceFemale = body.get("distanceFemale") != null ? ((Number) body.get("distanceFemale")).intValue() : null;
            Double paceMinMale = body.get("paceMinMale") != null ? ((Number) body.get("paceMinMale")).doubleValue() : null;
            Double paceMaxMale = body.get("paceMaxMale") != null ? ((Number) body.get("paceMaxMale")).doubleValue() : null;
            Double paceMinFemale = body.get("paceMinFemale") != null ? ((Number) body.get("paceMinFemale")).doubleValue() : null;
            Double paceMaxFemale = body.get("paceMaxFemale") != null ? ((Number) body.get("paceMaxFemale")).doubleValue() : null;
            Integer runsPerWeek = body.get("runsPerWeek") != null ? ((Number) body.get("runsPerWeek")).intValue() : null;
            Integer totalWeeks = body.get("totalWeeks") != null ? ((Number) body.get("totalWeeks")).intValue() : null;

            SchoolSettings settings = schoolSettingsService.updateSunshineRunSettings(
                    school, distanceMale, distanceFemale, paceMinMale, paceMaxMale, paceMinFemale, paceMaxFemale,
                    runsPerWeek, totalWeeks);
            return ApiResponse.success("更新成功", settings);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PutMapping("/homework-submission-settings")
    public ResponseEntity<ApiResponse<SchoolSettings>> updateHomeworkSubmissionSettings(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未提供有效的授权令牌"));
            }
            String token = authHeader.substring(7);
            String userType = jwtUtil.extractUserType(token);

            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(403, "权限不足：仅校级管理员或超级管理员可操作"));
            }

            Object schoolObj = body.get("school");
            String school = schoolObj instanceof String ? (String) schoolObj : schoolObj != null ? String.valueOf(schoolObj).trim() : null;
            if (school == null || school.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "school 不能为空"));
            }

            Integer weeklyRequired = bodyInt(body, "weeklySubmissionsRequired");
            Integer semesterWeeks = bodyInt(body, "submissionSemesterWeeks");

            SchoolSettings settings = schoolSettingsService.updateHomeworkSubmissionSettings(
                    school, weeklyRequired, semesterWeeks);
            return ResponseEntity.ok(ApiResponse.success("更新成功", settings));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, msg));
        }
    }

    /** 兼容 JSON 中数字或字符串形式的整型字段。 */
    private static Integer bodyInt(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(t);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @GetMapping("/homework-standards")
    public ApiResponse<List<HomeworkExerciseStandard>> getHomeworkStandards(@RequestParam String school) {
        try {
            List<HomeworkExerciseStandard> standards = homeworkExerciseStandardRepository.findBySchool(school);
            return ApiResponse.success("获取成功", standards);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/homework-standards")
    public ApiResponse<List<HomeworkExerciseStandard>> updateHomeworkStandards(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userType = jwtUtil.extractUserType(token);

            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ApiResponse.error(403, "权限不足：仅校级管理员或超级管理员可操作");
            }

            String school = (String) body.get("school");
            if (school == null || school.isBlank()) {
                return ApiResponse.error(400, "school 不能为空");
            }

            List<Map<String, Object>> standardsList = (List<Map<String, Object>>) body.get("standards");
            if (standardsList == null || standardsList.isEmpty()) {
                return ApiResponse.error(400, "standards 不能为空");
            }

            for (Map<String, Object> item : standardsList) {
                String exerciseType = (String) item.get("exerciseType");
                int maleStandard = ((Number) item.get("maleStandard")).intValue();
                int femaleStandard = ((Number) item.get("femaleStandard")).intValue();

                Optional<HomeworkExerciseStandard> existing =
                        homeworkExerciseStandardRepository.findBySchoolAndExerciseType(school, exerciseType);

                HomeworkExerciseStandard standard;
                if (existing.isPresent()) {
                    standard = existing.get();
                } else {
                    standard = new HomeworkExerciseStandard();
                    standard.setSchool(school);
                    standard.setExerciseType(exerciseType);
                }
                standard.setMaleStandard(maleStandard);
                standard.setFemaleStandard(femaleStandard);
                homeworkExerciseStandardRepository.save(standard);
            }

            List<HomeworkExerciseStandard> result = homeworkExerciseStandardRepository.findBySchool(school);
            return ApiResponse.success("更新成功", result);
        } catch (ClassCastException | NullPointerException e) {
            return ApiResponse.error(400, "请求参数错误");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}
