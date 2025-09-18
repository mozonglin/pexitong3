package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.PointsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PointsRecordRepository extends JpaRepository<PointsRecord, String> {
    
    @Query("SELECT pr FROM PointsRecord pr " +
           "LEFT JOIN PeUser u ON pr.userId = u.id " +
           "WHERE pr.userId = :userId AND " +
           "(:startDate IS NULL OR pr.earnedAt >= :startDate) AND " +
           "(:endDate IS NULL OR pr.earnedAt <= :endDate) AND " +
           "(:activityType IS NULL OR pr.activityType = :activityType) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college) " +
           "ORDER BY pr.earnedAt DESC")
    Page<PointsRecord> findUserPointsHistoryWithFilters(@Param("userId") String userId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        @Param("activityType") String activityType,
                                                        @Param("school") String school,
                                                        @Param("college") String college,
                                                        Pageable pageable);
    
    List<PointsRecord> findByUserId(String userId);
    
    List<PointsRecord> findByActivityId(String activityId);
    
    @Query("SELECT pr FROM PointsRecord pr " +
           "LEFT JOIN PeUser u ON pr.userId = u.id " +
           "WHERE (:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    List<PointsRecord> findBySchoolAndCollege(@Param("school") String school, @Param("college") String college);
}
