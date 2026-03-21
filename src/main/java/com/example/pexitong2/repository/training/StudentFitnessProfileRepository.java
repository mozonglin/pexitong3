package com.example.pexitong2.repository.training;

import com.example.pexitong2.entity.training.StudentFitnessProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentFitnessProfileRepository extends JpaRepository<StudentFitnessProfile, Long> {

    Optional<StudentFitnessProfile> findByUserId(String userId);

    Optional<StudentFitnessProfile> findByStudentId(String studentId);

    List<StudentFitnessProfile> findByClassName(String className);

    List<StudentFitnessProfile> findByDepartmentName(String departmentName);

    Page<StudentFitnessProfile> findByClassName(String className, Pageable pageable);

    @Query("SELECT p FROM StudentFitnessProfile p WHERE p.className IN :classNames")
    List<StudentFitnessProfile> findByClassNameIn(@Param("classNames") List<String> classNames);

    @Query("SELECT p FROM StudentFitnessProfile p WHERE p.className IN :classNames")
    Page<StudentFitnessProfile> findByClassNameIn(@Param("classNames") List<String> classNames, Pageable pageable);

    @Query("SELECT p FROM StudentFitnessProfile p WHERE " +
           "(p.realName LIKE %:keyword% OR p.studentId LIKE %:keyword%) " +
           "AND (:className IS NULL OR p.className = :className)")
    Page<StudentFitnessProfile> searchProfiles(
            @Param("keyword") String keyword,
            @Param("className") String className,
            Pageable pageable);

    @Query("SELECT DISTINCT p.className FROM StudentFitnessProfile p WHERE p.className IS NOT NULL ORDER BY p.className")
    List<String> findDistinctClassNames();

    @Query("SELECT COUNT(p) FROM StudentFitnessProfile p WHERE p.aiFitnessLevel = :level")
    long countByFitnessLevel(@Param("level") String level);
}
