package com.example.pexitong2.repository.teaching;

import com.example.pexitong2.entity.teaching.TeacherAttendanceStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherAttendanceStatisticsRepository extends JpaRepository<TeacherAttendanceStatistics, Long> {
    
    /**
     * 查找指定日期、类型、学校和院系的统计记录
     */
    Optional<TeacherAttendanceStatistics> findByStatDateAndStatTypeAndSchool(
            LocalDate statDate, 
            TeacherAttendanceStatistics.StatType statType, 
            String school);
    
    /**
     * 查找指定日期、类型和学校的统计记录
     */
    @Query("SELECT tas FROM TeacherAttendanceStatistics tas " +
           "WHERE tas.statDate = :statDate " +
           "AND tas.statType = :statType " +
           "AND tas.school = :school")
    Optional<TeacherAttendanceStatistics> findSchoolStatistics(
            @Param("statDate") LocalDate statDate,
            @Param("statType") TeacherAttendanceStatistics.StatType statType,
            @Param("school") String school);
    
    /**
     * 查找指定学校的最新统计记录
     */
    @Query("SELECT tas FROM TeacherAttendanceStatistics tas " +
           "WHERE tas.school = :school " +
           "AND tas.statType = :statType " +
           "ORDER BY tas.statDate DESC")
    List<TeacherAttendanceStatistics> findLatestBySchoolAndType(
            @Param("school") String school,
            @Param("statType") TeacherAttendanceStatistics.StatType statType);
    
    /**
     * 查找指定时间范围的统计记录
     */
    @Query("SELECT tas FROM TeacherAttendanceStatistics tas " +
           "WHERE tas.statDate BETWEEN :startDate AND :endDate " +
           "AND tas.school = :school " +
           "AND tas.statType = :statType " +
           "ORDER BY tas.statDate DESC")
    List<TeacherAttendanceStatistics> findByDateRangeAndSchoolAndType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("school") String school,
            @Param("statType") TeacherAttendanceStatistics.StatType statType);
    
    /**
     * 删除指定日期、类型和学校的统计记录（用于重新计算）
     */
    void deleteByStatDateAndStatTypeAndSchool(
            LocalDate statDate, 
            TeacherAttendanceStatistics.StatType statType, 
            String school);
}
