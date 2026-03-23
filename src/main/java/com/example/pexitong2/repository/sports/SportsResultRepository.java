package com.example.pexitong2.repository.sports;

import com.example.pexitong2.entity.sports.SportsResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportsResultRepository extends JpaRepository<SportsResult, Long> {

    List<SportsResult> findByEventIdOrderByRankingAsc(Long eventId);

    List<SportsResult> findByHeatIdOrderByRankingAsc(Long heatId);

    Optional<SportsResult> findByEventIdAndRegistrationId(Long eventId, Long registrationId);

    long countByEventId(Long eventId);

    @Query("SELECT SUM(r.score) FROM SportsResult r " +
            "JOIN SportsRegistration reg ON r.registrationId = reg.id " +
            "WHERE reg.meetingId = :meetingId AND reg.department = :department")
    Integer sumScoreByMeetingAndDepartment(@Param("meetingId") Long meetingId,
                                           @Param("department") String department);

    @Query("SELECT r FROM SportsResult r " +
            "JOIN SportsRegistration reg ON r.registrationId = reg.id " +
            "WHERE reg.meetingId = :meetingId")
    List<SportsResult> findAllByMeetingId(@Param("meetingId") Long meetingId);

    void deleteByEventId(Long eventId);
}
