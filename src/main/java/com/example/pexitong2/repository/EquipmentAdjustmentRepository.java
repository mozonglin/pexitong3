package com.example.pexitong2.repository;

import com.example.pexitong2.entity.EquipmentAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EquipmentAdjustmentRepository extends JpaRepository<EquipmentAdjustment, String> {
    
    /**
     * 根据器材ID获取调整记录（分页）
     */
    Page<EquipmentAdjustment> findByEquipmentIdOrderByCreatedAtDesc(String equipmentId, Pageable pageable);
    
    /**
     * 根据操作人获取调整记录（分页）
     */
    Page<EquipmentAdjustment> findByOperatorOrderByCreatedAtDesc(String operator, Pageable pageable);
    
    /**
     * 根据调整类型获取记录（分页）
     */
    Page<EquipmentAdjustment> findByTypeOrderByCreatedAtDesc(EquipmentAdjustment.AdjustmentType type, Pageable pageable);
    
    /**
     * 根据日期范围获取调整记录
     */
    @Query("SELECT a FROM EquipmentAdjustment a WHERE a.createdAt >= :dateFrom AND a.createdAt <= :dateTo " +
           "ORDER BY a.createdAt DESC")
    Page<EquipmentAdjustment> findByDateRange(@Param("dateFrom") LocalDateTime dateFrom, 
                                              @Param("dateTo") LocalDateTime dateTo, 
                                              Pageable pageable);
    
    /**
     * 获取器材的最近调整记录
     */
    List<EquipmentAdjustment> findTop10ByEquipmentIdOrderByCreatedAtDesc(String equipmentId);
    
    /**
     * 统计调整记录总数
     */
    long countByEquipmentId(String equipmentId);
}





