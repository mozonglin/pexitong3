package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.MorningExerciseAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MorningExerciseAttendanceRepository extends JpaRepository<MorningExerciseAttendance, String> {
    
    @Query("SELECT mea FROM MorningExerciseAttendance mea " +
           "LEFT JOIN PeUser u ON mea.studentId = u.studentId " +
           "WHERE mea.exerciseId = :exerciseId AND " +
           "(:studentId IS NULL OR mea.studentId = :studentId) AND " +
           "(:isCheckedOut IS NULL OR mea.isCheckedOut = :isCheckedOut) AND " +
           "(:checkedBy IS NULL OR mea.checkedBy = :checkedBy) AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college) " +
           "ORDER BY mea.checkInTime DESC")
    Page<MorningExerciseAttendance> findByExerciseIdWithFilters(@Param("exerciseId") String exerciseId,
                                                               @Param("studentId") String studentId,
                                                               @Param("isCheckedOut") Boolean isCheckedOut,
                                                               @Param("checkedBy") String checkedBy,
                                                               @Param("school") String school,
                                                               @Param("college") String college,
                                                               Pageable pageable);
    
    List<MorningExerciseAttendance> findByExerciseId(String exerciseId);
    
    List<MorningExerciseAttendance> findByStudentId(String studentId);
    
    @Query("SELECT mea FROM MorningExerciseAttendance mea " +
           "LEFT JOIN PeUser u ON mea.studentId = u.studentId " +
           "WHERE (:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    List<MorningExerciseAttendance> findBySchoolAndCollege(@Param("school") String school, @Param("college") String college);
    
    boolean existsByExerciseIdAndStudentId(String exerciseId, String studentId);
}
