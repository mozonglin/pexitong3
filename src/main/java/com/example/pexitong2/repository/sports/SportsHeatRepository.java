package com.example.pexitong2.repository.sports;

import com.example.pexitong2.entity.sports.SportsHeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportsHeatRepository extends JpaRepository<SportsHeat, Long> {

    List<SportsHeat> findByEventIdOrderByHeatNumberAsc(Long eventId);

    void deleteByEventId(Long eventId);

    long countByEventId(Long eventId);
}
