package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.PeSchedule;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.pe.PeScheduleService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/pe/schedules")
public class PeScheduleController {

    @Autowired
    private PeScheduleService peScheduleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Integer>> importSchedules(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam String semester) {
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
            String school = user.getSchool();

            int count = peScheduleService.importFromExcel(file.getInputStream(), school, semester);
            return ResponseEntity.ok(ApiResponse.success("导入成功，共导入" + count + "条记录", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<PeSchedule>>> getSchedules(
            @RequestParam String school,
            @RequestParam String semester) {
        try {
            List<PeSchedule> schedules = peScheduleService.getSchedules(school, semester);
            return ResponseEntity.ok(ApiResponse.success("获取课表成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userType = jwtUtil.extractUserType(token);
            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "无权限执行此操作"));
            }

            peScheduleService.deleteSchedule(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
