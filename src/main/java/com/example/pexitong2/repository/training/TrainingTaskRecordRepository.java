package com.example.pexitong2.repository.training;

import com.example.pexitong2.entity.training.TrainingTaskRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingTaskRecordRepository extends JpaRepository<TrainingTaskRecord, Long> {

    List<TrainingTaskRecord> findByTaskId(Long taskId);

    Page<TrainingTaskRecord> findByTaskId(Long taskId, Pageable pageable);

    List<TrainingTaskRecord> findByUserId(String userId);

    Optional<TrainingTaskRecord> findByTaskIdAndUserId(Long taskId, String userId);

    @Query("SELECT r FROM TrainingTaskRecord r WHERE r.taskId = :taskId AND r.status = :status")
    List<TrainingTaskRecord> findByTaskIdAndStatus(
            @Param("taskId") Long taskId,
            @Param("status") TrainingTaskRecord.RecordStatus status);

    long countByTaskIdAndStatus(Long taskId, TrainingTaskRecord.RecordStatus status);

    @Query("SELECT r FROM TrainingTaskRecord r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    Page<TrainingTaskRecord> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT r FROM TrainingTaskRecord r JOIN r.task t WHERE r.userId = :userId AND t.status = 'active' ORDER BY t.endDate ASC")
    List<TrainingTaskRecord> findActiveRecordsByUserId(@Param("userId") String userId);
}
