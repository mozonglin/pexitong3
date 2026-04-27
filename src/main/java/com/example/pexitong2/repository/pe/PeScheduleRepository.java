package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.PeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeScheduleRepository extends JpaRepository<PeSchedule, String> {

    List<PeSchedule> findBySchoolAndSemester(String school, String semester);

    List<PeSchedule> findBySchool(String school);

    void deleteBySchoolAndSemester(String school, String semester);
}
