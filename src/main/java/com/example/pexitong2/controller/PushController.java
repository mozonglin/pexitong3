package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.service.JPushService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/push")
@CrossOrigin(origins = "*")
public class PushController {

    @Autowired
    private JPushService jpushService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<String>> testPush(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userType = jwtUtil.extractUserType(token);

            if (!"school_admin".equals(userType) && !"super_admin".equals(userType)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "权限不足：仅管理员可发送测试推送"));
            }

            String title = (String) body.getOrDefault("title", "测试推送");
            String content = (String) body.getOrDefault("content", "这是一条测试推送消息");

            @SuppressWarnings("unchecked")
            List<String> targetIds = (List<String>) body.get("targetIds");
            String school = (String) body.get("school");

            if (targetIds != null && !targetIds.isEmpty()) {
                jpushService.pushToStudents(targetIds, title, content, Map.of("type", "test"));
                return ResponseEntity.ok(ApiResponse.success("测试推送已发送至指定用户", null));
            } else if (school != null && !school.isEmpty()) {
                jpushService.pushToAll(school, title, content);
                return ResponseEntity.ok(ApiResponse.success("测试推送已发送至全校: " + school, null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "请提供 targetIds 或 school 参数"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "推送失败: " + e.getMessage()));
        }
    }
}
