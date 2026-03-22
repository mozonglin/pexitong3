package com.example.pexitong2.repository;

import com.example.pexitong2.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, String> {
    
    /**
     * 获取未删除的器材（分页）
     */
    Page<Equipment> findByIsDeletedFalse(Pageable pageable);
    
    /**
     * 根据分类ID获取器材（分页）
     */
    Page<Equipment> findByCategoryIdAndIsDeletedFalse(String categoryId, Pageable pageable);
    
    /**
     * 根据关键词搜索器材名称或型号（分页）
     */
    @Query("SELECT e FROM Equipment e WHERE e.isDeleted = false AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.model) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Equipment> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据分类和关键词搜索器材（分页）
     */
    @Query("SELECT e FROM Equipment e WHERE e.isDeleted = false AND e.categoryId = :categoryId AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.model) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Equipment> findByCategoryIdAndKeyword(@Param("categoryId") String categoryId, 
                                               @Param("keyword") String keyword, 
                                               Pageable pageable);
    
    /**
     * 根据库存状态筛选
     */
    @Query("SELECT e FROM Equipment e WHERE e.isDeleted = false AND " +
           "CASE WHEN :status = 'available' THEN e.availableQuantity > 0 " +
           "WHEN :status = 'shortage' THEN e.availableQuantity <= 5 " +
           "ELSE true END")
    Page<Equipment> findByStatus(@Param("status") String status, Pageable pageable);
    
    /**
     * 综合查询：分类+关键词+状态
     */
    @Query("SELECT e FROM Equipment e WHERE e.isDeleted = false " +
           "AND (:categoryId IS NULL OR e.categoryId = :categoryId) " +
           "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(e.model) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (CASE WHEN :status = 'available' THEN e.availableQuantity > 0 " +
           "     WHEN :status = 'shortage' THEN e.availableQuantity <= 5 " +
           "     ELSE true END)")
    Page<Equipment> findWithFilters(@Param("categoryId") String categoryId,
                                   @Param("keyword") String keyword,
                                   @Param("status") String status,
                                   Pageable pageable);
    
    /**
     * 获取未删除的器材详情
     */
    Optional<Equipment> findByIdAndIsDeletedFalse(String id);
    
    /**
     * 根据分类ID获取器材数量
     */
    long countByCategoryIdAndIsDeletedFalse(String categoryId);
    
    /**
     * 统计器材总数（未删除）
     */
    long countByIsDeletedFalse();
    
    /**
     * 统计可用器材数量
     */
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.isDeleted = false AND e.availableQuantity > 0")
    long countAvailableEquipment();
    
    /**
     * 统计借出器材数量
     */
    @Query("SELECT SUM(e.borrowedQuantity) FROM Equipment e WHERE e.isDeleted = false")
    Long countBorrowedEquipment();
    
    /**
     * 统计损坏器材数量
     */
    @Query("SELECT SUM(e.damagedQuantity) FROM Equipment e WHERE e.isDeleted = false")
    Long countDamagedEquipment();
    
    /**
     * 获取热门器材（根据借用数量排序）
     */
    @Query("SELECT e FROM Equipment e WHERE e.isDeleted = false ORDER BY e.borrowedQuantity DESC")
    List<Equipment> findPopularEquipment(Pageable pageable);

    // ===== 按学校过滤的查询 =====

    Page<Equipment> findBySchoolAndIsDeletedFalse(String school, Pageable pageable);

    @Query("SELECT e FROM Equipment e WHERE e.isDeleted = false AND e.school = :school " +
           "AND (:categoryId IS NULL OR e.categoryId = :categoryId) " +
           "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(e.model) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (CASE WHEN :status = 'available' THEN e.availableQuantity > 0 " +
           "     WHEN :status = 'shortage' THEN e.availableQuantity <= 5 " +
           "     ELSE true END)")
    Page<Equipment> findBySchoolWithFilters(@Param("school") String school,
                                           @Param("categoryId") String categoryId,
                                           @Param("keyword") String keyword,
                                           @Param("status") String status,
                                           Pageable pageable);
}





