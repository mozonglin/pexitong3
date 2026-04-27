package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.HomeworkAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkAssignmentRepository extends JpaRepository<HomeworkAssignment, String> {

    List<HomeworkAssignment> findByTeacherId(String teacherId);

    List<HomeworkAssignment> findByTempClassId(String tempClassId);

    List<HomeworkAssignment> findBySchoolAndStatus(String school, HomeworkAssignment.AssignmentStatus status);

    List<HomeworkAssignment> findBySchool(String school);
}
