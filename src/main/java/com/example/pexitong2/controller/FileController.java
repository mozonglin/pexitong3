package com.example.pexitong2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class FileController {
    
    @Value("${listening.file.base-path}")
    private String basePath;
    
    /**
     * 下载文件（评价文件或视频文件）
     */
    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        
        try {
            // 获取文件路径（去掉/api/files/download/前缀）
            String requestURL = request.getRequestURL().toString();
            String relativePath = requestURL.substring(requestURL.indexOf("/download/") + "/download/".length());
            
            // 构建完整文件路径
            Path filePath = Paths.get(basePath, relativePath);
            File file = filePath.toFile();
            
            System.out.println("尝试下载文件: " + filePath.toString());
            System.out.println("文件是否存在: " + file.exists());
            
            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                System.out.println("文件不存在或不是文件: " + filePath.toString());
                return ResponseEntity.notFound().build();
            }
            
            // 检查文件是否在允许的目录范围内（安全检查）
            String canonicalBasePath = Paths.get(basePath).toFile().getCanonicalPath();
            String canonicalFilePath = file.getCanonicalPath();
            if (!canonicalFilePath.startsWith(canonicalBasePath)) {
                System.out.println("文件路径不在允许范围内: " + canonicalFilePath);
                return ResponseEntity.badRequest().build();
            }
            
            // 创建文件资源
            Resource resource = new FileSystemResource(file);
            
            // 获取文件名并编码
            String filename = file.getName();
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            
            // 根据文件扩展名确定Content-Type
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
            
        } catch (Exception e) {
            System.out.println("文件下载失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pdf":
                return "application/pdf";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
} 