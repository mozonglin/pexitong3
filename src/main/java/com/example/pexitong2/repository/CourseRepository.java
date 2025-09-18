package com.example.pexitong2.repository;

import com.example.pexitong2.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * 根据教师ID查找课程
     */
    List<Course> findByTeacherId(String teacherId);
    
    /**
     * 根据上课日期查找课程
     */
    List<Course> findByClassDate(LocalDate classDate);
    
    /**
     * 根据上课日期范围查找课程
     */
    List<Course> findByClassDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据关键词搜索课程（课程名称、教师姓名），限制在指定学校内
     */
    @Query("SELECT c FROM Course c JOIN c.teacher t WHERE " +
           "t.school = :school " +
           "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacherName LIKE %:keyword%) " +
           "AND (:classDate IS NULL OR c.classDate = :classDate)")
    List<Course> searchCourses(@Param("keyword") String keyword, 
                              @Param("classDate") LocalDate classDate,
                              @Param("school") String school);
    
    /**
     * 分页查询课程，限制在指定学校内
     */
    @Query("SELECT c FROM Course c JOIN c.teacher t WHERE " +
           "t.school = :school " +
           "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacherName LIKE %:keyword%) " +
           "AND (:startDate IS NULL OR c.classDate >= :startDate) " +
           "AND (:endDate IS NULL OR c.classDate <= :endDate)")
    Page<Course> searchCoursesWithPage(@Param("keyword") String keyword,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("school") String school,
                                      Pageable pageable);
    
    /**
     * 根据教师姓名和日期查找课程，限制在指定学校内
     */
    @Query("SELECT c FROM Course c JOIN c.teacher t WHERE " +
           "t.school = :school " +
           "AND c.teacherName LIKE %:teacherName% " +
           "AND c.classDate = :classDate")
    List<Course> findByTeacherNameContainingAndClassDate(@Param("teacherName") String teacherName, 
                                                         @Param("classDate") LocalDate classDate,
                                                         @Param("school") String school);
    
    /**
     * 根据上课日期查找课程，限制在指定学校内
     */
    @Query("SELECT c FROM Course c JOIN c.teacher t WHERE " +
           "t.school = :school " +
           "AND c.classDate = :classDate")
    List<Course> findByClassDateAndSchool(@Param("classDate") LocalDate classDate,
                                         @Param("school") String school);
    
    /**
     * 统计某教师的课程数量
     */
    long countByTeacherId(String teacherId);
} 