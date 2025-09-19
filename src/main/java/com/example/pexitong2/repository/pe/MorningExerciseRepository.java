package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.MorningExercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface MorningExerciseRepository extends JpaRepository<MorningExercise, String> {
    
    @Query("SELECT me FROM MorningExercise me " +
           "LEFT JOIN User u ON me.createdBy = u.id " +
           "WHERE (:date IS NULL OR me.date = :date) AND " +
           "(:isActive IS NULL OR me.isActive = :isActive) AND " +
           "(:startDate IS NULL OR me.date >= :startDate) AND " +
           "(:endDate IS NULL OR me.date <= :endDate) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.departmentName = :college) " +
           "ORDER BY me.date DESC")
    Page<MorningExercise> findMorningExercisesWithFilters(@Param("date") LocalDate date,
                                                         @Param("isActive") Boolean isActive,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         @Param("school") String school,
                                                         @Param("college") String college,
                                                         Pageable pageable);
    
    @Query("SELECT COUNT(me), COUNT(CASE WHEN me.isActive = true THEN 1 END) FROM MorningExercise me " +
           "LEFT JOIN User u ON me.createdBy = u.id " +
           "WHERE (:startDate IS NULL OR me.date >= :startDate) AND " +
           "(:endDate IS NULL OR me.date <= :endDate) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.departmentName = :college)")
    List<Object[]> getMorningExerciseStatistics(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("school") String school,
                                               @Param("college") String college);
    
    @Query("SELECT YEAR(me.date), MONTH(me.date), " +
           "COUNT(me), SUM(me.checkedInCount), SUM(me.checkedOutCount), " +
           "AVG(CASE WHEN me.checkedInCount > 0 THEN me.checkedOutCount * 1.0 / me.checkedInCount ELSE 0 END) " +
           "FROM MorningExercise me " +
           "LEFT JOIN User u ON me.createdBy = u.id " +
           "WHERE (:startDate IS NULL OR me.date >= :startDate) AND " +
           "(:endDate IS NULL OR me.date <= :endDate) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.departmentName = :college) " +
           "GROUP BY YEAR(me.date), MONTH(me.date) " +
           "ORDER BY YEAR(me.date), MONTH(me.date)")
    List<Object[]> getMorningExerciseMonthlyStatistics(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate,
                                                       @Param("school") String school,
                                                       @Param("college") String college);
    
    List<MorningExercise> findByDate(LocalDate date);
    
    List<MorningExercise> findByCreatedBy(String createdBy);
    
    @Query("SELECT me FROM MorningExercise me " +
           "LEFT JOIN User u ON me.createdBy = u.id " +
           "WHERE (:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.departmentName = :college)")
    List<MorningExercise> findBySchoolAndCollege(@Param("school") String school, @Param("college") String college);
}
