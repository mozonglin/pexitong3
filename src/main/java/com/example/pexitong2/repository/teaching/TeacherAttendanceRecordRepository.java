package com.example.pexitong2.repository.teaching;

import com.example.pexitong2.entity.teaching.TeacherAttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherAttendanceRecordRepository extends JpaRepository<TeacherAttendanceRecord, Long> {
    
    /**
     * 根据课程ID查找签到记录
     */
    Optional<TeacherAttendanceRecord> findByCourseId(Long courseId);
    
    /**
     * 根据教师ID查找签到记录
     */
    List<TeacherAttendanceRecord> findByTeacherIdOrderByClassDateDesc(String teacherId);
    
    /**
     * 分页查询签到记录，支持多条件筛选
     */
    @Query("SELECT tar FROM TeacherAttendanceRecord tar " +
           "LEFT JOIN FETCH tar.photos " +
           "JOIN User u ON tar.teacherId = u.id " +
           "WHERE (:date IS NULL OR tar.classDate = :date) " +
           "AND (:status IS NULL OR tar.attendanceStatus = :status) " +
           "AND (:search IS NULL OR tar.courseName LIKE %:search% OR tar.teacherName LIKE %:search%) " +
           "AND (:school IS NULL OR u.school = :school) " +
           "ORDER BY tar.classDate DESC, tar.startTime DESC")
    Page<TeacherAttendanceRecord> findWithFilters(
            @Param("date") LocalDate date,
            @Param("status") TeacherAttendanceRecord.AttendanceStatus status,
            @Param("search") String search,
            @Param("school") String school,
            Pageable pageable);
    
    /**
     * 统计查询 - 按状态分组统计
     */
    @Query("SELECT tar.attendanceStatus, COUNT(tar) FROM TeacherAttendanceRecord tar " +
           "JOIN User u ON tar.teacherId = u.id " +
           "WHERE (:date IS NULL OR tar.classDate = :date) " +
           "AND (:school IS NULL OR u.school = :school) " +
           "GROUP BY tar.attendanceStatus")
    List<Object[]> countByStatusAndFilters(
            @Param("date") LocalDate date,
            @Param("school") String school);
    
    /**
     * 统计教师数量
     */
    @Query("SELECT COUNT(DISTINCT tar.teacherId) FROM TeacherAttendanceRecord tar " +
           "JOIN User u ON tar.teacherId = u.id " +
           "WHERE (:date IS NULL OR tar.classDate = :date) " +
           "AND (:school IS NULL OR u.school = :school)")
    Long countDistinctTeachers(
            @Param("date") LocalDate date,
            @Param("school") String school);
    
    /**
     * 查询指定日期范围内的签到记录
     */
    @Query("SELECT tar FROM TeacherAttendanceRecord tar " +
           "JOIN User u ON tar.teacherId = u.id " +
           "WHERE tar.classDate BETWEEN :startDate AND :endDate " +
           "AND (:courseId IS NULL OR tar.courseId = :courseId) " +
           "AND (:teacherId IS NULL OR tar.teacherId = :teacherId) " +
           "AND (:school IS NULL OR u.school = :school) " +
           "ORDER BY tar.classDate DESC, tar.startTime DESC")
    Page<TeacherAttendanceRecord> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("courseId") Long courseId,
            @Param("teacherId") String teacherId,
            @Param("school") String school,
            Pageable pageable);
    
    /**
     * 查询本周签到记录
     */
    @Query("SELECT tar FROM TeacherAttendanceRecord tar " +
           "JOIN User u ON tar.teacherId = u.id " +
           "WHERE tar.classDate BETWEEN :startOfWeek AND :endOfWeek " +
           "AND (:school IS NULL OR u.school = :school) " +
           "ORDER BY tar.classDate DESC")
    List<TeacherAttendanceRecord> findWeeklyRecords(
            @Param("startOfWeek") LocalDate startOfWeek,
            @Param("endOfWeek") LocalDate endOfWeek,
            @Param("school") String school);
    
    /**
     * 查询本月签到记录
     */
    @Query("SELECT tar FROM TeacherAttendanceRecord tar " +
           "JOIN User u ON tar.teacherId = u.id " +
           "WHERE tar.classDate BETWEEN :startOfMonth AND :endOfMonth " +
           "AND (:school IS NULL OR u.school = :school) " +
           "ORDER BY tar.classDate DESC")
    List<TeacherAttendanceRecord> findMonthlyRecords(
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth,
            @Param("school") String school);
    
    /**
     * 检查是否存在指定课程的签到记录
     */
    boolean existsByCourseIdAndTeacherIdAndClassDate(Long courseId, String teacherId, LocalDate classDate);
}
