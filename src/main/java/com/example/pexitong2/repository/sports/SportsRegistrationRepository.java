package com.example.pexitong2.repository.sports;

import com.example.pexitong2.entity.sports.SportsRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportsRegistrationRepository extends JpaRepository<SportsRegistration, Long> {

    List<SportsRegistration> findByMeetingIdAndStatus(Long meetingId, String status);

    List<SportsRegistration> findByEventIdAndStatus(Long eventId, String status);

    List<SportsRegistration> findByMeetingId(Long meetingId);

    long countByMeetingId(Long meetingId);

    long countByEventId(Long eventId);

    @Query("SELECT DISTINCT r.department FROM SportsRegistration r WHERE r.meetingId = :meetingId AND r.status = 'approved'")
    List<String> findDistinctDepartmentsByMeetingId(@Param("meetingId") Long meetingId);

    @Query("SELECT COUNT(DISTINCT r.studentNumber) FROM SportsRegistration r WHERE r.meetingId = :meetingId AND r.status = 'approved'")
    long countDistinctAthletesByMeetingId(@Param("meetingId") Long meetingId);

    @Query("SELECT COUNT(r) FROM SportsRegistration r WHERE r.meetingId = :meetingId AND r.studentNumber = :studentNumber AND r.status = 'approved'")
    long countByMeetingIdAndStudentNumber(@Param("meetingId") Long meetingId, @Param("studentNumber") String studentNumber);

    @Query("SELECT COUNT(r) FROM SportsRegistration r WHERE r.eventId = :eventId AND r.department = :department AND r.status = 'approved'")
    long countByEventIdAndDepartment(@Param("eventId") Long eventId, @Param("department") String department);

    void deleteByMeetingId(Long meetingId);

    void deleteByEventId(Long eventId);
}
