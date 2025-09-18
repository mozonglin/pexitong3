package com.example.pexitong2.repository;

import com.example.pexitong2.entity.ListeningObservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ListeningObservationRepository extends JpaRepository<ListeningObservation, Long> {
    
    /**
     * 根据听课者ID查找听课记录
     */
    List<ListeningObservation> findByObserverId(String observerId);
    
    /**
     * 根据课程ID查找听课记录
     */
    List<ListeningObservation> findByCourseId(Long courseId);
    
    /**
     * 根据听课日期查找听课记录
     */
    List<ListeningObservation> findByClassDate(LocalDate classDate);
    
    /**
     * 根据听课日期范围查找听课记录
     */
    List<ListeningObservation> findByClassDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 分页查询听课记录（教师权限：只能看到自己作为听课者的记录）
     */
    @Query("SELECT lo FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN lo.observer o " +
           "WHERE lo.observerId = :observerId " +
           "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacherName LIKE %:keyword% OR o.realName LIKE %:keyword%) " +
           "AND (:startDate IS NULL OR lo.classDate >= :startDate) " +
           "AND (:endDate IS NULL OR lo.classDate <= :endDate) " +
           "ORDER BY lo.createdAt DESC")
    Page<ListeningObservation> findObservationsByObserver(@Param("observerId") String observerId,
                                                          @Param("keyword") String keyword,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate,
                                                          Pageable pageable);
    
    /**
     * 分页查询听课记录（管理员权限：可以查看权限范围内的所有记录），限制在指定学校内
     */
    @Query("SELECT lo FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN lo.observer o " +
           "JOIN c.teacher t " +
           "WHERE t.school = :school " +
           "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacherName LIKE %:keyword% OR o.realName LIKE %:keyword%) " +
           "AND (:startDate IS NULL OR lo.classDate >= :startDate) " +
           "AND (:endDate IS NULL OR lo.classDate <= :endDate) " +
           "ORDER BY lo.createdAt DESC")
    Page<ListeningObservation> findAllObservationsBySchool(@Param("keyword") String keyword,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate,
                                                           @Param("school") String school,
                                                           Pageable pageable);
    
    /**
     * 分页查询某院系的听课记录（根据教师所属院系过滤），限制在指定学校内
     */
    @Query("SELECT lo FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN lo.observer o " +
           "JOIN c.teacher t " +
           "WHERE t.school = :school " +
           "AND t.departmentName = :departmentName " +
           "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacherName LIKE %:keyword% OR o.realName LIKE %:keyword%) " +
           "AND (:startDate IS NULL OR lo.classDate >= :startDate) " +
           "AND (:endDate IS NULL OR lo.classDate <= :endDate) " +
           "ORDER BY lo.createdAt DESC")
    Page<ListeningObservation> findObservationsByDepartmentNameAndSchool(@Param("departmentName") String departmentName,
                                                                         @Param("keyword") String keyword,
                                                                         @Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate,
                                                                         @Param("school") String school,
                                                                         Pageable pageable);
    
    /**
     * 保留原有的不带学校过滤的方法（用于超级管理员）
     */
    @Query("SELECT lo FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN lo.observer o " +
           "WHERE (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacherName LIKE %:keyword% OR o.realName LIKE %:keyword%) " +
           "AND (:startDate IS NULL OR lo.classDate >= :startDate) " +
           "AND (:endDate IS NULL OR lo.classDate <= :endDate) " +
           "ORDER BY lo.createdAt DESC")
    Page<ListeningObservation> findAllObservations(@Param("keyword") String keyword,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   Pageable pageable);
    
    /**
     * 保留原有的院系查询方法（用于超级管理员）
     */
    @Query("SELECT lo FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN lo.observer o " +
           "JOIN c.teacher t " +
           "WHERE t.departmentName = :departmentName " +
           "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacherName LIKE %:keyword% OR o.realName LIKE %:keyword%) " +
           "AND (:startDate IS NULL OR lo.classDate >= :startDate) " +
           "AND (:endDate IS NULL OR lo.classDate <= :endDate) " +
           "ORDER BY lo.createdAt DESC")
    Page<ListeningObservation> findObservationsByDepartmentName(@Param("departmentName") String departmentName,
                                                                @Param("keyword") String keyword,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate,
                                                                Pageable pageable);
    
    /**
     * 统计总听课记录数
     */
    long count();
    
    /**
     * 统计某听课者的听课记录数
     */
    long countByObserverId(String observerId);
    
    /**
     * 统计日期范围内的听课记录数
     */
    long countByClassDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 统计有评价文件的记录数
     */
    @Query("SELECT COUNT(lo) FROM ListeningObservation lo WHERE lo.evaluationFileName IS NOT NULL")
    long countWithEvaluationFile();
    
    /**
     * 统计有视频文件的记录数
     */
    @Query("SELECT COUNT(lo) FROM ListeningObservation lo WHERE lo.videoFileName IS NOT NULL")
    long countWithVideoFile();
    
    /**
     * 按院系统计听课记录数（基于teacher的department_name）
     */
    @Query("SELECT t.departmentName, COUNT(lo) FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN c.teacher t " +
           "WHERE t.departmentName IS NOT NULL " +
           "GROUP BY t.departmentName")
    List<Object[]> countObservationsByDepartmentName();
    
    /**
     * 按院系统计评价文件数（基于teacher的department_name）
     */
    @Query("SELECT t.departmentName, COUNT(lo) FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN c.teacher t " +
           "WHERE lo.evaluationFileName IS NOT NULL " +
           "AND t.departmentName IS NOT NULL " +
           "GROUP BY t.departmentName")
    List<Object[]> countEvaluationFilesByDepartmentName();
    
    /**
     * 按院系统计视频文件数（基于teacher的department_name）
     */
    @Query("SELECT t.departmentName, COUNT(lo) FROM ListeningObservation lo " +
           "JOIN lo.course c " +
           "JOIN c.teacher t " +
           "WHERE lo.videoFileName IS NOT NULL " +
           "AND t.departmentName IS NOT NULL " +
           "GROUP BY t.departmentName")
    List<Object[]> countVideoFilesByDepartmentName();
} 