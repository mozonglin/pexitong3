package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.TempClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempClassRepository extends JpaRepository<TempClass, String> {

    List<TempClass> findBySchoolAndSemester(String school, String semester);

    List<TempClass> findByTeacherId(String teacherId);

    boolean existsBySchoolAndSemesterAndTeacherIdAndClassName(String school, String semester, String teacherId, String className);
}
