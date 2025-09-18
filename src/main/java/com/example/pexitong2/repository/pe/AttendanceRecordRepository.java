package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, String> {
    
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "LEFT JOIN PeUser u ON ar.userId = u.id " +
           "WHERE (:activityId IS NULL OR ar.activityId = :activityId) AND " +
           "(:studentId IS NULL OR ar.studentId = :studentId) AND " +
           "(:studentName IS NULL OR ar.userName LIKE %:studentName%) AND " +
           "(:startDate IS NULL OR ar.checkInTime >= :startDate) AND " +
           "(:endDate IS NULL OR ar.checkInTime <= :endDate) AND " +
           "(:isCheckedOut IS NULL OR ar.isCheckedOut = :isCheckedOut) AND " +
           "(:minDuration IS NULL OR ar.duration >= :minDuration) AND " +
           "(:maxDuration IS NULL OR ar.duration <= :maxDuration) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college) " +
           "ORDER BY ar.checkInTime DESC")
    Page<AttendanceRecord> findAttendanceRecordsWithFilters(@Param("activityId") String activityId,
                                                           @Param("studentId") String studentId,
                                                           @Param("studentName") String studentName,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate,
                                                           @Param("isCheckedOut") Boolean isCheckedOut,
                                                           @Param("minDuration") Integer minDuration,
                                                           @Param("maxDuration") Integer maxDuration,
                                                           @Param("school") String school,
                                                           @Param("college") String college,
                                                           Pageable pageable);
    
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "LEFT JOIN PeUser u ON ar.userId = u.id " +
           "WHERE ar.activityId = :activityId AND " +
           "(:studentId IS NULL OR ar.studentId = :studentId) AND " +
           "(:isCheckedOut IS NULL OR ar.isCheckedOut = :isCheckedOut) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    Page<AttendanceRecord> findByActivityIdWithFilters(@Param("activityId") String activityId,
                                                      @Param("studentId") String studentId,
                                                      @Param("isCheckedOut") Boolean isCheckedOut,
                                                      @Param("school") String school,
                                                      @Param("college") String college,
                                                      Pageable pageable);
    
    @Query("SELECT COUNT(ar), COUNT(CASE WHEN ar.isCheckedOut = true THEN 1 END), " +
           "AVG(ar.duration), SUM(ar.pointsEarned) FROM AttendanceRecord ar " +
           "LEFT JOIN PeUser u ON ar.userId = u.id " +
           "WHERE (:startDate IS NULL OR ar.checkInTime >= :startDate) AND " +
           "(:endDate IS NULL OR ar.checkInTime <= :endDate) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    List<Object[]> getAttendanceStatistics(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("school") String school,
                                          @Param("college") String college);
    
    List<AttendanceRecord> findByActivityId(String activityId);
    
    List<AttendanceRecord> findByUserId(String userId);
}
