package com.example.pexitong2.repository;

import com.example.pexitong2.entity.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, String> {
    
    /**
     * 根据排序字段获取所有分类（升序）
     */
    List<EquipmentCategory> findAllByOrderBySortAscCreatedAtAsc();
    
    /**
     * 根据名称查找分类
     */
    EquipmentCategory findByName(String name);
    
    /**
     * 检查分类名称是否已存在（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM EquipmentCategory c WHERE c.name = ?1 AND c.id != ?2")
    boolean existsByNameAndIdNot(String name, String id);
    
    /**
     * 检查分类名称是否已存在
     */
    boolean existsByName(String name);
}





