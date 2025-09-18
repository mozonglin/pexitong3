package com.example.pexitong2.repository;

import com.example.pexitong2.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    
    Optional<VerificationCode> findByPhoneAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
        String phone, String code, VerificationCode.CodeType type, LocalDateTime now);
    
    @Query("SELECT COUNT(v) FROM VerificationCode v WHERE v.phone = :phone AND v.createdAt >= :startOfDay")
    long countByPhoneAndCreatedAtAfter(@Param("phone") String phone, @Param("startOfDay") LocalDateTime startOfDay);
    
    List<VerificationCode> findByPhoneAndTypeAndIsUsedFalse(String phone, VerificationCode.CodeType type);
} 