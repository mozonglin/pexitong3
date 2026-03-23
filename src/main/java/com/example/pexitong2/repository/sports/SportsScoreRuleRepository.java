package com.example.pexitong2.repository.sports;

import com.example.pexitong2.entity.sports.SportsScoreRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportsScoreRuleRepository extends JpaRepository<SportsScoreRule, Long> {

    List<SportsScoreRule> findByMeetingIdOrderByRankingAsc(Long meetingId);

    Optional<SportsScoreRule> findByMeetingIdAndRanking(Long meetingId, Integer ranking);

    void deleteByMeetingId(Long meetingId);
}
