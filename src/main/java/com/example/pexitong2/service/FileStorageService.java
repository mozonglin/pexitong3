package com.example.pexitong2.service;

import com.example.pexitong2.entity.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${listening.file.base-path}")
    private String basePath;
    
    // 支持的文件类型
    private static final List<String> EVALUATION_FILE_TYPES = Arrays.asList(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.ms-excel", // .xls
        "application/pdf", // .pdf
        "application/msword", // .doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    );
    
    private static final List<String> VIDEO_FILE_TYPES = Arrays.asList(
        "video/mp4", // .mp4
        "video/avi", // .avi
        "video/quicktime" // .mov
    );
    
    private static final List<String> TEMPLATE_FILE_TYPES = Arrays.asList(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.ms-excel", // .xls
        "application/pdf", // .pdf
        "application/msword", // .doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    );
    
    // 文件大小限制（字节）
    private static final long MAX_EVALUATION_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_VIDEO_FILE_SIZE = 500 * 1024 * 1024; // 500MB
    private static final long MAX_TEMPLATE_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    /**
     * 存储评价文件
     */
    public FileInfo storeEvaluationFile(MultipartFile file) throws IOException {
        validateEvaluationFile(file);
        return storeFile(file, "evaluations");
    }
    
    /**
     * 存储视频文件
     */
    public FileInfo storeVideoFile(MultipartFile file) throws IOException {
        validateVideoFile(file);
        return storeFile(file, "videos");
    }
    
    /**
     * 存储模板文件
     */
    public FileInfo storeTemplateFile(MultipartFile file) throws IOException {
        validateTemplateFile(file);
        return storeFile(file, "templates");
    }
    
    /**
     * 通用文件存储方法
     */
    private FileInfo storeFile(MultipartFile file, String subDirectory) throws IOException {
        // 创建目录
        Path uploadPath = Paths.get(basePath, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = String.format("%s_%s_%s%s", 
            subDirectory.substring(0, subDirectory.length() - 1), // 去掉s
            timestamp, 
            uuid, 
            extension
        );
        
        // 存储文件
        Path targetPath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 构建相对路径
        String relativePath = subDirectory + "/" + filename;
        
        // 返回文件信息
        return new FileInfo(
            originalFilename,
            relativePath,
            file.getContentType(),
            file.getSize(),
            LocalDateTime.now()
        );
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(basePath, relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取文件完整路径
     */
    public Path getFilePath(String relativePath) {
        return Paths.get(basePath, relativePath);
    }
    
    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String relativePath) {
        return Files.exists(getFilePath(relativePath));
    }
    
    /**
     * 验证评价文件
     */
    private void validateEvaluationFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("评价文件不能为空");
        }
        
        if (file.getSize() > MAX_EVALUATION_FILE_SIZE) {
            throw new IllegalArgumentException("评价文件大小不能超过50MB");
        }
        
        String contentType = file.getContentType();
        if (!EVALUATION_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的评价文件类型，仅支持Excel、PDF、Word文件");
        }
    }
    
    /**
     * 验证视频文件
     */
    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("视频文件不能为空");
        }
        
        if (file.getSize() > MAX_VIDEO_FILE_SIZE) {
            throw new IllegalArgumentException("视频文件大小不能超过500MB");
        }
        
        String contentType = file.getContentType();
        if (!VIDEO_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的视频文件类型，仅支持MP4、AVI、MOV文件");
        }
    }
    
    /**
     * 验证模板文件
     */
    private void validateTemplateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("模板文件不能为空");
        }
        
        if (file.getSize() > MAX_TEMPLATE_FILE_SIZE) {
            throw new IllegalArgumentException("模板文件大小不能超过10MB");
        }
        
        String contentType = file.getContentType();
        if (!TEMPLATE_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的模板文件类型，仅支持Excel文件");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
} 