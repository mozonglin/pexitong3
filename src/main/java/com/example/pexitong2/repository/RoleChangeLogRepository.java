package com.example.pexitong2.repository;

import com.example.pexitong2.entity.RoleChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleChangeLogRepository extends JpaRepository<RoleChangeLog, Long> {
    
    List<RoleChangeLog> findByUserIdOrderByChangedAtDesc(String userId);
    
    List<RoleChangeLog> findByChangedByOrderByChangedAtDesc(String changedBy);
} 