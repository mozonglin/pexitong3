package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    
    @Query("SELECT a FROM Activity a " +
           "LEFT JOIN PeUser u ON a.organizerId = u.id " +
           "WHERE (:approvalStatus IS NULL OR a.approvalStatus = :approvalStatus) AND " +
           "(:category IS NULL OR a.category = :category) AND " +
           "(:organizerId IS NULL OR a.organizerId = :organizerId) AND " +
           "(:startDate IS NULL OR a.activityStartTime >= :startDate) AND " +
           "(:endDate IS NULL OR a.activityEndTime <= :endDate) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college) " +
           "ORDER BY a.createdAt DESC")
    Page<Activity> findActivitiesWithFilters(@Param("approvalStatus") Activity.ApprovalStatus approvalStatus,
                                            @Param("category") String category,
                                            @Param("organizerId") String organizerId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("school") String school,
                                            @Param("college") String college,
                                            Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Activity a " +
           "LEFT JOIN PeUser u ON a.organizerId = u.id " +
           "WHERE a.approvalStatus = :status AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    long countByApprovalStatusAndSchoolAndCollege(@Param("status") Activity.ApprovalStatus status,
                                                 @Param("school") String school,
                                                 @Param("college") String college);
    
    @Query("SELECT a.category, COUNT(a), SUM(a.currentParticipants) FROM Activity a " +
           "LEFT JOIN PeUser u ON a.organizerId = u.id " +
           "WHERE a.approvalStatus = 'APPROVED' AND " +
           "(:startDate IS NULL OR a.activityStartTime >= :startDate) AND " +
           "(:endDate IS NULL OR a.activityEndTime <= :endDate) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college) " +
           "GROUP BY a.category")
    List<Object[]> getActivityStatisticsByCategory(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  @Param("school") String school,
                                                  @Param("college") String college);
    
    List<Activity> findByOrganizerId(String organizerId);
    
    @Query("SELECT a FROM Activity a " +
           "LEFT JOIN PeUser u ON a.organizerId = u.id " +
           "WHERE (:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    List<Activity> findBySchoolAndCollege(@Param("school") String school, @Param("college") String college);
}
