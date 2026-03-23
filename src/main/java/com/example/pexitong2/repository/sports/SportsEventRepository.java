package com.example.pexitong2.repository.sports;

import com.example.pexitong2.entity.sports.SportsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportsEventRepository extends JpaRepository<SportsEvent, Long> {

    List<SportsEvent> findByMeetingIdOrderBySortOrderAsc(Long meetingId);

    List<SportsEvent> findByMeetingIdAndGenderOrderBySortOrderAsc(Long meetingId, String gender);

    long countByMeetingId(Long meetingId);

    void deleteByMeetingId(Long meetingId);
}
