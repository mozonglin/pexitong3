package com.example.pexitong2.repository.sports;

import com.example.pexitong2.entity.sports.SportsMeeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SportsMeetingRepository extends JpaRepository<SportsMeeting, Long> {

    @Query("SELECT m FROM SportsMeeting m WHERE " +
            "(:school IS NULL OR m.school = :school) AND " +
            "(:status IS NULL OR m.status = :status) " +
            "ORDER BY m.createdAt DESC")
    Page<SportsMeeting> findWithFilters(@Param("school") String school,
                                        @Param("status") String status,
                                        Pageable pageable);

    long countByStatus(String status);

    long countBySchool(String school);
}
