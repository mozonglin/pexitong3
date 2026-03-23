package com.example.pexitong2.repository.race;

import com.example.pexitong2.entity.race.RaceExcelFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceExcelFileRepository extends JpaRepository<RaceExcelFile, Long> {

    @Query("SELECT f FROM RaceExcelFile f WHERE " +
           "(:school IS NULL OR f.school = :school) AND " +
           "(:teacherName IS NULL OR f.teacherName LIKE %:teacherName%)")
    Page<RaceExcelFile> findWithFilters(
            @Param("school")      String school,
            @Param("teacherName") String teacherName,
            Pageable pageable);
}
