package com.example.pexitong2.repository.race;

import com.example.pexitong2.entity.race.RaceResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RaceResultRepository extends JpaRepository<RaceResult, Long> {

    /**
     * 多条件分页查询（管理端）
     */
    @Query("SELECT r FROM RaceResult r WHERE " +
           "(:school IS NULL OR r.school = :school) AND " +
           "(:gender IS NULL OR r.gender = :gender) AND " +
           "(:teacherName IS NULL OR r.teacherName LIKE %:teacherName%) AND " +
           "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.studentNumber LIKE %:keyword%) AND " +
           "(:startDate IS NULL OR r.uploadedAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.uploadedAt <= :endDate)")
    Page<RaceResult> findWithFilters(
            @Param("school")      String school,
            @Param("gender")      String gender,
            @Param("teacherName") String teacherName,
            @Param("keyword")     String keyword,
            @Param("startDate")   LocalDateTime startDate,
            @Param("endDate")     LocalDateTime endDate,
            Pageable pageable);

    /**
     * 统计指定学校的总人数
     */
    long countBySchool(String school);

    /**
     * 统计指定学校完赛（finalTimeMs 不为空）的人数
     */
    long countBySchoolAndFinalTimeMsIsNotNull(String school);

    /**
     * 统计指定学校未完赛（finalTimeMs 为空）的人数
     */
    long countBySchoolAndFinalTimeMsIsNull(String school);

    /**
     * 统计全部记录数
     */
    @Query("SELECT COUNT(r) FROM RaceResult r")
    long countAll();

    /**
     * 统计完赛人数（全局）
     */
    long countByFinalTimeMsIsNotNull();

    /**
     * 统计未完赛人数（全局）
     */
    long countByFinalTimeMsIsNull();
}
