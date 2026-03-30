package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 人脸库导入代理接口（仅 super_admin 可用）
 *
 * POST /face/import   → 转发到 38.207.179.218:5000/face/import
 * GET  /face          → 转发到 38.207.179.218:5000/face?sid=xxx（人脸图片获取）
 *
 * 请求参数：
 *   file      - zip 压缩包（multipart/form-data）
 *   overwrite - true/false，默认 true
 */
@RestController
@RequestMapping("/face")
@CrossOrigin(origins = "*")
public class FaceImportController {

    private static final String FACE_SERVICE_BASE = "http://38.207.179.218:5000";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // ── 权限校验 ──────────────────────────────────────────────────────────────

    private void requireSuperAdmin(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("未提供认证 Token");
        }
        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new SecurityException("用户不存在"));
        if (user.getUserType() != User.UserType.super_admin) {
            throw new SecurityException("权限不足，仅超级管理员可操作");
        }
    }

    // ── 批量导入人脸 ──────────────────────────────────────────────────────────

    /**
     * 批量导入人脸库
     * 将 zip 包转发到人脸服务 POST /face/import
     *
     * @param file      zip 压缩包，文件名（不含后缀）为学号
     * @param overwrite 是否覆盖已有图片，默认 true
     */
    @PostMapping("/import")
    public ResponseEntity<Map> importFaces(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "true") String overwrite,
            HttpServletRequest request) {
        try {
            requireSuperAdmin(request);

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("code", 400, "message", "文件不能为空"));
            }
            if (!file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("code", 400, "message", "只支持上传 .zip 压缩包"));
            }

            RestTemplate restTemplate = new RestTemplate();

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("overwrite", overwrite);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                FACE_SERVICE_BASE + "/face/import",
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of("code", 403, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("code", 500, "message", "转发请求失败：" + e.getMessage()));
        }
    }
}
