package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.HomeworkExerciseStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeworkExerciseStandardRepository extends JpaRepository<HomeworkExerciseStandard, String> {

    List<HomeworkExerciseStandard> findBySchool(String school);

    Optional<HomeworkExerciseStandard> findBySchoolAndExerciseType(String school, String exerciseType);
}
