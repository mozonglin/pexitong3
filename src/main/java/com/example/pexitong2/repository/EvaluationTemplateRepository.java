package com.example.pexitong2.repository;

import com.example.pexitong2.entity.EvaluationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationTemplateRepository extends JpaRepository<EvaluationTemplate, Long> {
    
    /**
     * 查找默认模板
     */
    Optional<EvaluationTemplate> findByIsDefaultTrue();
    
    /**
     * 根据上传者查找模板
     */
    List<EvaluationTemplate> findByUploadedBy(String uploadedBy);
    
    /**
     * 根据名称查找模板
     */
    Optional<EvaluationTemplate> findByName(String name);
    
    /**
     * 根据名称模糊查询模板
     */
    List<EvaluationTemplate> findByNameContaining(String name);
    
    /**
     * 获取所有模板，按上传时间倒序
     */
    @Query("SELECT t FROM EvaluationTemplate t ORDER BY t.uploadedAt DESC")
    List<EvaluationTemplate> findAllOrderByUploadedAtDesc();
    
    /**
     * 获取所有模板，默认模板排在前面
     */
    @Query("SELECT t FROM EvaluationTemplate t ORDER BY t.isDefault DESC, t.uploadedAt DESC")
    List<EvaluationTemplate> findAllOrderByDefaultAndUploadedAt();
    
    /**
     * 清除所有默认标记
     */
    @Modifying
    @Query("UPDATE EvaluationTemplate t SET t.isDefault = false WHERE t.isDefault = true")
    void clearAllDefaultFlags();
    
    /**
     * 设置指定模板为默认模板
     */
    @Modifying
    @Query("UPDATE EvaluationTemplate t SET t.isDefault = :isDefault WHERE t.id = :id")
    void updateDefaultFlag(@Param("id") Long id, @Param("isDefault") Boolean isDefault);
    
    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 统计模板总数
     */
    long count();
} 