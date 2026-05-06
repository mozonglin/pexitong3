package com.example.pexitong2.service.pe;

import com.example.pexitong2.entity.pe.SchoolSettings;
import com.example.pexitong2.repository.pe.SchoolSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SchoolSettingsService {

    @Autowired
    private SchoolSettingsRepository schoolSettingsRepository;

    public SchoolSettings getOrCreateSettings(String school) {
        return schoolSettingsRepository.findBySchool(school)
                .orElseGet(() -> {
                    SchoolSettings settings = new SchoolSettings();
                    settings.setId(UUID.randomUUID().toString());
                    settings.setSchool(school);
                    return schoolSettingsRepository.save(settings);
                });
    }

    @Transactional
    public SchoolSettings updateSunshineRunDistance(String school, int distance) {
        SchoolSettings settings = getOrCreateSettings(school);
        settings.setSunshineRunDistance(distance);
        return schoolSettingsRepository.save(settings);
    }

    @Transactional
    public SchoolSettings updateSunshineRunSettings(String school,
            Integer distanceMale, Integer distanceFemale,
            Double paceMinMale, Double paceMaxMale,
            Double paceMinFemale, Double paceMaxFemale) {
        SchoolSettings settings = getOrCreateSettings(school);
        if (distanceMale != null) settings.setSunshineRunDistanceMale(distanceMale);
        if (distanceFemale != null) settings.setSunshineRunDistanceFemale(distanceFemale);
        if (paceMinMale != null) settings.setSunshineRunPaceMinMale(paceMinMale);
        if (paceMaxMale != null) settings.setSunshineRunPaceMaxMale(paceMaxMale);
        if (paceMinFemale != null) settings.setSunshineRunPaceMinFemale(paceMinFemale);
        if (paceMaxFemale != null) settings.setSunshineRunPaceMaxFemale(paceMaxFemale);
        if (distanceMale != null && distanceFemale != null) {
            settings.setSunshineRunDistance(Math.max(distanceMale, distanceFemale));
        }
        return schoolSettingsRepository.save(settings);
    }
}
