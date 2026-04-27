package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.pe.SchoolSettings;
import com.example.pexitong2.service.pe.SchoolSettingsService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pe/school-settings")
@CrossOrigin(origins = "*")
public class SchoolSettingsController {

    @Autowired
    private SchoolSettingsService schoolSettingsService;

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
}
