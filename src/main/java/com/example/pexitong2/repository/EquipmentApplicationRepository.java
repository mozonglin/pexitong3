package com.example.pexitong2.repository;

import com.example.pexitong2.entity.EquipmentApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EquipmentApplicationRepository extends JpaRepository<EquipmentApplication, String> {
    
    /**
     * 根据状态获取申请（分页）
     */
    Page<EquipmentApplication> findByStatus(EquipmentApplication.ApplicationStatus status, Pageable pageable);
    
    /**
     * 根据器材ID获取申请（分页）
     */
    Page<EquipmentApplication> findByEquipmentId(String equipmentId, Pageable pageable);
    
    /**
     * 根据借用人ID获取申请（分页）
     */
    Page<EquipmentApplication> findByBorrowerId(String borrowerId, Pageable pageable);
    
    /**
     * 根据日期范围获取申请
     */
    @Query("SELECT a FROM EquipmentApplication a WHERE a.createdAt >= :dateFrom AND a.createdAt <= :dateTo")
    Page<EquipmentApplication> findByDateRange(@Param("dateFrom") LocalDateTime dateFrom, 
                                               @Param("dateTo") LocalDateTime dateTo, 
                                               Pageable pageable);
    
    /**
     * 综合查询：状态+器材+借用人+日期范围
     */
    @Query("SELECT a FROM EquipmentApplication a WHERE " +
           "(:status IS NULL OR a.status = :status) " +
           "AND (:equipmentId IS NULL OR a.equipmentId = :equipmentId) " +
           "AND (:borrowerId IS NULL OR a.borrowerId = :borrowerId) " +
           "AND (:dateFrom IS NULL OR a.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR a.createdAt <= :dateTo)")
    Page<EquipmentApplication> findWithFilters(@Param("status") EquipmentApplication.ApplicationStatus status,
                                               @Param("equipmentId") String equipmentId,
                                               @Param("borrowerId") String borrowerId,
                                               @Param("dateFrom") LocalDateTime dateFrom,
                                               @Param("dateTo") LocalDateTime dateTo,
                                               Pageable pageable);
    
    /**
     * 统计待审批申请数量
     */
    long countByStatus(EquipmentApplication.ApplicationStatus status);
    
    /**
     * 统计今日申请数量
     */
    @Query("SELECT COUNT(a) FROM EquipmentApplication a WHERE DATE(a.createdAt) = CURRENT_DATE")
    long countTodayApplications();
    
    /**
     * 统计本周申请数量
     */
    @Query("SELECT COUNT(a) FROM EquipmentApplication a WHERE a.createdAt >= :weekStart")
    long countWeekApplications(@Param("weekStart") LocalDateTime weekStart);
    
    /**
     * 统计本月申请数量
     */
    @Query("SELECT COUNT(a) FROM EquipmentApplication a WHERE a.createdAt >= :monthStart")
    long countMonthApplications(@Param("monthStart") LocalDateTime monthStart);
    
    /**
     * 获取指定器材的已批准但未归还的申请
     */
    @Query("SELECT a FROM EquipmentApplication a WHERE a.equipmentId = :equipmentId " +
           "AND a.status = 'approved' AND a.actualReturnDate IS NULL")
    List<EquipmentApplication> findApprovedNotReturnedByEquipmentId(@Param("equipmentId") String equipmentId);
    
    /**
     * 获取超期未归还的申请
     */
    @Query("SELECT a FROM EquipmentApplication a WHERE a.status = 'approved' " +
           "AND a.actualReturnDate IS NULL AND a.expectedReturnDate < :currentTime")
    List<EquipmentApplication> findOverdueApplications(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 统计器材借用次数（用于热门器材统计）
     */
    @Query("SELECT a.equipmentId, COUNT(a) FROM EquipmentApplication a " +
           "WHERE a.status = 'approved' GROUP BY a.equipmentId ORDER BY COUNT(a) DESC")
    List<Object[]> findEquipmentBorrowCounts(Pageable pageable);
}





