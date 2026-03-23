package com.example.pexitong2.repository.sports;

import com.example.pexitong2.entity.sports.SportsHeatLane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportsHeatLaneRepository extends JpaRepository<SportsHeatLane, Long> {

    List<SportsHeatLane> findByHeatIdOrderByLaneNumberAsc(Long heatId);

    void deleteByHeatId(Long heatId);
}
