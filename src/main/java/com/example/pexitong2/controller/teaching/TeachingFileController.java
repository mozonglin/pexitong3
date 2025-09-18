package com.example.pexitong2.controller.teaching;

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

/**
 * 教师签到系统文件控制器
 * 处理教师签到照片等文件的访问
 */
@RestController
@RequestMapping("/api/teaching")
@CrossOrigin(origins = "*")
public class TeachingFileController {
    
    @Value("${teaching.file.base-path}")
    private String teachingBasePath;
    
    /**
     * 下载教师签到照片
     * GET /api/teaching/files/**
     */
    @GetMapping("/files/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        
        try {
            // 获取文件路径（去掉/api/teaching/files/前缀）
            String requestURL = request.getRequestURL().toString();
            String relativePath = requestURL.substring(requestURL.indexOf("/files/") + "/files/".length());
            
            // 构建完整文件路径
            Path filePath = Paths.get(teachingBasePath, relativePath);
            File file = filePath.toFile();
            
            System.out.println("尝试下载教师签到文件: " + filePath.toString());
            System.out.println("文件是否存在: " + file.exists());
            
            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                System.out.println("文件不存在或不是文件: " + filePath.toString());
                return ResponseEntity.notFound().build();
            }
            
            // 检查文件是否在允许的目录范围内（安全检查）
            String canonicalBasePath = Paths.get(teachingBasePath).toFile().getCanonicalPath();
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
            
            // 对于图片文件，使用inline显示方式
            String disposition = isImageFile(filename) ? "inline" : "attachment";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + encodedFilename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
            
        } catch (Exception e) {
            System.out.println("教师签到文件下载失败: " + e.getMessage());
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
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip":
                return "application/zip";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return "jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension) || 
               "gif".equals(extension) || "bmp".equals(extension) || "webp".equals(extension);
    }
}


