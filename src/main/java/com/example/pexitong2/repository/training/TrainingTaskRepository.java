package com.example.pexitong2.repository.training;

import com.example.pexitong2.entity.training.TrainingTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingTaskRepository extends JpaRepository<TrainingTask, Long> {

    List<TrainingTask> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    Page<TrainingTask> findByTeacherId(String teacherId, Pageable pageable);

    Page<TrainingTask> findByTeacherIdAndStatus(String teacherId, TrainingTask.TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM TrainingTask t WHERE t.status = 'active' AND t.targetClasses LIKE %:className%")
    List<TrainingTask> findActiveTasksByClassName(@Param("className") String className);

    @Query("SELECT t FROM TrainingTask t WHERE t.status = 'active' AND t.endDate < :today")
    List<TrainingTask> findExpiredActiveTasks(@Param("today") LocalDate today);

    @Query("SELECT t FROM TrainingTask t WHERE t.teacherId = :teacherId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "ORDER BY t.createdAt DESC")
    Page<TrainingTask> findByTeacherIdAndOptionalStatus(
            @Param("teacherId") String teacherId,
            @Param("status") TrainingTask.TaskStatus status,
            Pageable pageable);

    long countByTeacherIdAndStatus(String teacherId, TrainingTask.TaskStatus status);
}
