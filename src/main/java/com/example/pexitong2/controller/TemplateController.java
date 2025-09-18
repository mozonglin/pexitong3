package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.dto.TemplateResponse;
import com.example.pexitong2.entity.EvaluationTemplate;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.TemplateService;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/listening/templates")
@CrossOrigin(origins = "*")
public class TemplateController {
    
    @Autowired
    private TemplateService templateService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取模板列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplates(
            HttpServletRequest request) {
        
        try {
            // 检查权限
            String token = extractToken(request);
            String userType = jwtUtil.extractUserType(token);
            
            if (!hasTemplateManagePermission(userType)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "权限不足"));
            }
            
            List<EvaluationTemplate> templates = templateService.getAllTemplates();
            List<TemplateResponse> templateResponses = templates.stream()
                .map(TemplateResponse::fromEntity)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", templateResponses));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 上传模板
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> uploadTemplate(
            @RequestParam("template_file") MultipartFile file,
            @RequestParam("name") String name,
            HttpServletRequest request) {
        
        try {
            // 检查权限
            String token = extractToken(request);
            String username = jwtUtil.extractUsername(token);
            String userId = getUserIdFromToken(token);
            String userType = jwtUtil.extractUserType(token);
            
            System.out.println("模板上传请求 - 用户名: " + username + ", 用户ID: " + userId + ", 用户类型: " + userType);
            
            if (!hasTemplateManagePermission(userType)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "权限不足"));
            }
            
            // 上传模板 - 使用正确的用户ID
            EvaluationTemplate template = templateService.uploadTemplate(file, name, userId);
            
            // 使用DTO避免序列化问题
            TemplateResponse templateResponse = TemplateResponse.fromEntity(template);
            
            return ResponseEntity.ok(ApiResponse.success("上传成功", templateResponse));
            
        } catch (Exception e) {
            System.out.println("模板上传失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 下载模板
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> downloadTemplate(@PathVariable Long id) {
        
        try {
            Optional<EvaluationTemplate> templateOpt = templateService.getTemplateById(id);
            
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            EvaluationTemplate template = templateOpt.get();
            Path filePath = templateService.getTemplateFilePath(id);
            
            if (!templateService.templateFileExists(id)) {
                return ResponseEntity.notFound().build();
            }
            
            FileSystemResource resource = new FileSystemResource(filePath);
            String encodedFilename = URLEncoder.encode(template.getName(), StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 下载默认模板
     */
    @GetMapping("/default/download")
    public ResponseEntity<FileSystemResource> downloadDefaultTemplate() {
        
        try {
            Optional<EvaluationTemplate> templateOpt = templateService.getDefaultTemplate();
            
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            EvaluationTemplate template = templateOpt.get();
            Path filePath = templateService.getDefaultTemplateFilePath();
            
            FileSystemResource resource = new FileSystemResource(filePath);
            String encodedFilename = URLEncoder.encode(template.getName(), StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 设置默认模板
     */
    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultTemplate(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        try {
            // 检查权限
            String token = extractToken(request);
            String userType = jwtUtil.extractUserType(token);
            
            if (!hasTemplateManagePermission(userType)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "权限不足"));
            }
            
            templateService.setDefaultTemplate(id);
            return ResponseEntity.ok(ApiResponse.<Void>success("设置成功", null));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        try {
            // 检查权限
            String token = extractToken(request);
            String userType = jwtUtil.extractUserType(token);
            
            if (!hasTemplateManagePermission(userType)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "权限不足"));
            }
            
            templateService.deleteTemplate(id);
            return ResponseEntity.ok(ApiResponse.<Void>success("删除成功", null));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<Void>error(400, e.getMessage()));
        }
    }
    
    /**
     * 从请求中提取JWT Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("未找到有效的授权令牌");
    }
    
    /**
     * 根据JWT token获取用户ID
     */
    private String getUserIdFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在：" + username));
        return user.getId();
    }
    
    /**
     * 检查是否有模板管理权限
     */
    private boolean hasTemplateManagePermission(String userType) {
        return userType.equals("school_admin") || userType.equals("super_admin");
    }
} 