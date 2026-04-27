package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, String> {

    List<HomeworkSubmission> findByAssignmentId(String assignmentId);

    List<HomeworkSubmission> findByStudentId(String studentId);

    Optional<HomeworkSubmission> findByAssignmentIdAndStudentId(String assignmentId, String studentId);
}
