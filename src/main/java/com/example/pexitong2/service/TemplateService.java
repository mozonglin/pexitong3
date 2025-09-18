package com.example.pexitong2.service;

import com.example.pexitong2.entity.EvaluationTemplate;
import com.example.pexitong2.entity.FileInfo;
import com.example.pexitong2.repository.EvaluationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {
    
    @Autowired
    private EvaluationTemplateRepository templateRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * 获取所有模板
     */
    public List<EvaluationTemplate> getAllTemplates() {
        return templateRepository.findAllOrderByDefaultAndUploadedAt();
    }
    
    /**
     * 根据ID获取模板
     */
    public Optional<EvaluationTemplate> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }
    
    /**
     * 获取默认模板
     */
    public Optional<EvaluationTemplate> getDefaultTemplate() {
        return templateRepository.findByIsDefaultTrue();
    }
    
    /**
     * 根据名称查找模板
     */
    public Optional<EvaluationTemplate> getTemplateByName(String name) {
        return templateRepository.findByName(name);
    }
    
    /**
     * 根据上传者查找模板
     */
    public List<EvaluationTemplate> getTemplatesByUploader(String uploaderId) {
        return templateRepository.findByUploadedBy(uploaderId);
    }
    
    /**
     * 上传模板
     */
    @Transactional
    public EvaluationTemplate uploadTemplate(MultipartFile file, String name, String uploaderId) throws IOException {
        // 检查模板名称是否已存在
        if (templateRepository.existsByName(name)) {
            throw new IllegalArgumentException("模板名称已存在：" + name);
        }
        
        // 存储文件
        FileInfo fileInfo = fileStorageService.storeTemplateFile(file);
        
        // 创建模板记录
        EvaluationTemplate template = new EvaluationTemplate(
            name,
            fileInfo.getPath(),
            fileInfo.getType(),
            fileInfo.getSize(),
            uploaderId
        );
        
        return templateRepository.save(template);
    }
    
    /**
     * 设置默认模板
     */
    @Transactional
    public void setDefaultTemplate(Long templateId) {
        // 检查模板是否存在
        if (!templateRepository.existsById(templateId)) {
            throw new IllegalArgumentException("模板不存在：" + templateId);
        }
        
        // 清除所有默认标记
        templateRepository.clearAllDefaultFlags();
        
        // 设置新的默认模板
        templateRepository.updateDefaultFlag(templateId, true);
    }
    
    /**
     * 删除模板
     */
    @Transactional
    public void deleteTemplate(Long id) {
        EvaluationTemplate template = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("模板不存在：" + id));
        
        // 如果是默认模板，不允许删除
        if (template.getIsDefault()) {
            throw new IllegalArgumentException("不能删除默认模板");
        }
        
        // 删除文件
        fileStorageService.deleteFile(template.getFilePath());
        
        // 删除数据库记录
        templateRepository.deleteById(id);
    }
    
    /**
     * 获取模板文件路径
     */
    public Path getTemplateFilePath(Long id) {
        EvaluationTemplate template = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("模板不存在：" + id));
        
        return fileStorageService.getFilePath(template.getFilePath());
    }
    
    /**
     * 获取默认模板文件路径
     */
    public Path getDefaultTemplateFilePath() {
        EvaluationTemplate template = templateRepository.findByIsDefaultTrue()
            .orElseThrow(() -> new IllegalArgumentException("未设置默认模板"));
        
        return fileStorageService.getFilePath(template.getFilePath());
    }
    
    /**
     * 检查模板文件是否存在
     */
    public boolean templateFileExists(Long id) {
        Optional<EvaluationTemplate> templateOpt = templateRepository.findById(id);
        if (templateOpt.isEmpty()) {
            return false;
        }
        
        return fileStorageService.fileExists(templateOpt.get().getFilePath());
    }
    
    /**
     * 统计模板数量
     */
    public long getTemplateCount() {
        return templateRepository.count();
    }
    
    /**
     * 根据名称模糊查询模板
     */
    public List<EvaluationTemplate> searchTemplatesByName(String name) {
        return templateRepository.findByNameContaining(name);
    }
} 