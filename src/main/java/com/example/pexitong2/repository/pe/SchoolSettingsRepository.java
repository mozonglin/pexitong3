package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.SchoolSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, String> {

    Optional<SchoolSettings> findBySchool(String school);
}
