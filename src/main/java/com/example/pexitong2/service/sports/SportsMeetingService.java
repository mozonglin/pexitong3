package com.example.pexitong2.service.sports;

import com.example.pexitong2.entity.sports.*;
import com.example.pexitong2.repository.sports.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SportsMeetingService {

    @Autowired
    private SportsMeetingRepository meetingRepo;
    @Autowired
    private SportsEventRepository eventRepo;
    @Autowired
    private SportsRegistrationRepository regRepo;
    @Autowired
    private SportsHeatRepository heatRepo;
    @Autowired
    private SportsHeatLaneRepository laneRepo;
    @Autowired
    private SportsResultRepository resultRepo;
    @Autowired
    private SportsScoreRuleRepository scoreRuleRepo;

    // ═══════════════════════════════════════════════════════════════════
    //  运动会 CRUD
    // ═══════════════════════════════════════════════════════════════════

    public SportsMeeting createMeeting(SportsMeeting meeting) {
        meeting.setStatus("draft");
        return meetingRepo.save(meeting);
    }

    public SportsMeeting getMeeting(Long id) {
        return meetingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("运动会不存在"));
    }

    public Page<SportsMeeting> listMeetings(String school, String status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1));
        return meetingRepo.findWithFilters(
                StringUtils.hasText(school) ? school : null,
                StringUtils.hasText(status) ? status : null,
                pageable);
    }

    public SportsMeeting updateMeeting(Long id, SportsMeeting updated) {
        SportsMeeting m = getMeeting(id);
        if (updated.getName() != null) m.setName(updated.getName());
        if (updated.getDescription() != null) m.setDescription(updated.getDescription());
        if (updated.getStartDate() != null) m.setStartDate(updated.getStartDate());
        if (updated.getEndDate() != null) m.setEndDate(updated.getEndDate());
        if (updated.getLocation() != null) m.setLocation(updated.getLocation());
        if (updated.getSchool() != null) m.setSchool(updated.getSchool());
        if (updated.getMaxEventsPerPerson() != null) m.setMaxEventsPerPerson(updated.getMaxEventsPerPerson());
        return meetingRepo.save(m);
    }

    public void updateMeetingStatus(Long id, String status) {
        SportsMeeting m = getMeeting(id);
        m.setStatus(status);
        meetingRepo.save(m);
    }

    @Transactional
    public void deleteMeeting(Long id) {
        List<SportsEvent> events = eventRepo.findByMeetingIdOrderBySortOrderAsc(id);
        for (SportsEvent ev : events) {
            deleteEventCascade(ev.getId());
        }
        scoreRuleRepo.deleteByMeetingId(id);
        regRepo.deleteByMeetingId(id);
        eventRepo.deleteByMeetingId(id);
        meetingRepo.deleteById(id);
    }

    public Map<String, Object> getMeetingStatistics(Long meetingId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("eventCount", eventRepo.countByMeetingId(meetingId));
        stats.put("registrationCount", regRepo.countByMeetingId(meetingId));
        stats.put("athleteCount", regRepo.countDistinctAthletesByMeetingId(meetingId));
        stats.put("departments", regRepo.findDistinctDepartmentsByMeetingId(meetingId));
        return stats;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  项目管理
    // ═══════════════════════════════════════════════════════════════════

    public SportsEvent createEvent(Long meetingId, SportsEvent event) {
        getMeeting(meetingId);
        event.setMeetingId(meetingId);
        return eventRepo.save(event);
    }

    public List<SportsEvent> listEvents(Long meetingId) {
        return eventRepo.findByMeetingIdOrderBySortOrderAsc(meetingId);
    }

    public SportsEvent updateEvent(Long eventId, SportsEvent updated) {
        SportsEvent e = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (updated.getName() != null) e.setName(updated.getName());
        if (updated.getEventCategory() != null) e.setEventCategory(updated.getEventCategory());
        if (updated.getGender() != null) e.setGender(updated.getGender());
        if (updated.getMaxPerTeam() != null) e.setMaxPerTeam(updated.getMaxPerTeam());
        if (updated.getIsRelay() != null) e.setIsRelay(updated.getIsRelay());
        if (updated.getRelayMembers() != null) e.setRelayMembers(updated.getRelayMembers());
        if (updated.getEventDate() != null) e.setEventDate(updated.getEventDate());
        if (updated.getEventTime() != null) e.setEventTime(updated.getEventTime());
        if (updated.getVenue() != null) e.setVenue(updated.getVenue());
        if (updated.getSortOrder() != null) e.setSortOrder(updated.getSortOrder());
        if (updated.getScoringType() != null) e.setScoringType(updated.getScoringType());
        if (updated.getStatus() != null) e.setStatus(updated.getStatus());
        return eventRepo.save(e);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        deleteEventCascade(eventId);
        eventRepo.deleteById(eventId);
    }

    private void deleteEventCascade(Long eventId) {
        List<SportsHeat> heats = heatRepo.findByEventIdOrderByHeatNumberAsc(eventId);
        for (SportsHeat h : heats) {
            laneRepo.deleteByHeatId(h.getId());
        }
        heatRepo.deleteByEventId(eventId);
        resultRepo.deleteByEventId(eventId);
        regRepo.deleteByEventId(eventId);
    }

    @Transactional
    public List<SportsEvent> batchCreateEvents(Long meetingId, List<SportsEvent> events) {
        getMeeting(meetingId);
        for (SportsEvent e : events) {
            e.setMeetingId(meetingId);
        }
        return eventRepo.saveAll(events);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  报名管理 - Excel 导入
    // ═══════════════════════════════════════════════════════════════════

    public byte[] generateRegistrationTemplate(Long meetingId) throws IOException {
        List<SportsEvent> events = eventRepo.findByMeetingIdOrderBySortOrderAsc(meetingId);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("报名表");

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"姓名", "学号", "性别(男/女)", "学院(代表队)", "班级", "联系电话", "项目名称"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            if (!events.isEmpty()) {
                Sheet refSheet = wb.createSheet("可选项目参考");
                Row refHeader = refSheet.createRow(0);
                String[] refHeaders = {"项目名称", "类型", "性别", "每队限报人数"};
                for (int i = 0; i < refHeaders.length; i++) {
                    Cell c = refHeader.createCell(i);
                    c.setCellValue(refHeaders[i]);
                    c.setCellStyle(headerStyle);
                    refSheet.setColumnWidth(i, 5000);
                }
                for (int i = 0; i < events.size(); i++) {
                    SportsEvent ev = events.get(i);
                    Row row = refSheet.createRow(i + 1);
                    row.createCell(0).setCellValue(ev.getName());
                    row.createCell(1).setCellValue("track".equals(ev.getEventCategory()) ? "径赛" : "田赛");
                    row.createCell(2).setCellValue(genderLabel(ev.getGender()));
                    row.createCell(3).setCellValue(ev.getMaxPerTeam());
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Transactional
    public Map<String, Object> importRegistrations(Long meetingId, MultipartFile file, String importerId) throws IOException {
        SportsMeeting meeting = getMeeting(meetingId);
        List<SportsEvent> events = eventRepo.findByMeetingIdOrderBySortOrderAsc(meetingId);
        Map<String, SportsEvent> eventMap = events.stream()
                .collect(Collectors.toMap(SportsEvent::getName, e -> e, (a, b) -> a));

        List<SportsRegistration> toSave = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        int successCount = 0;

        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellString(row, 0);
                String studentNumber = getCellString(row, 1);
                String gender = getCellString(row, 2);
                String department = getCellString(row, 3);
                String className = getCellString(row, 4);
                String phone = getCellString(row, 5);
                String eventName = getCellString(row, 6);

                if (!StringUtils.hasText(name) || !StringUtils.hasText(eventName)) {
                    if (StringUtils.hasText(name) || StringUtils.hasText(eventName)) {
                        errors.add(Map.of("row", i + 1, "reason", "姓名和项目名称不能为空"));
                    }
                    continue;
                }

                SportsEvent event = eventMap.get(eventName.trim());
                if (event == null) {
                    errors.add(Map.of("row", i + 1, "reason", "项目\"" + eventName + "\"不存在"));
                    continue;
                }

                String normalizedGender = normalizeGender(gender);
                if (!"mixed".equals(event.getGender()) && !event.getGender().equals(normalizedGender)) {
                    errors.add(Map.of("row", i + 1, "reason",
                            "性别不匹配，项目\"" + eventName + "\"要求" + genderLabel(event.getGender())));
                    continue;
                }

                if (StringUtils.hasText(studentNumber)) {
                    long personCount = regRepo.countByMeetingIdAndStudentNumber(meetingId, studentNumber.trim());
                    long pendingSameStudent = toSave.stream()
                            .filter(r -> studentNumber.trim().equals(r.getStudentNumber()))
                            .count();
                    if (personCount + pendingSameStudent >= meeting.getMaxEventsPerPerson()) {
                        errors.add(Map.of("row", i + 1, "reason",
                                "学号" + studentNumber + "已报满" + meeting.getMaxEventsPerPerson() + "个项目"));
                        continue;
                    }
                }

                if (StringUtils.hasText(department)) {
                    long teamCount = regRepo.countByEventIdAndDepartment(event.getId(), department.trim());
                    long pendingSameTeam = toSave.stream()
                            .filter(r -> event.getId().equals(r.getEventId()) && department.trim().equals(r.getDepartment()))
                            .count();
                    if (teamCount + pendingSameTeam >= event.getMaxPerTeam()) {
                        errors.add(Map.of("row", i + 1, "reason",
                                "\"" + department + "\"在项目\"" + eventName + "\"中已报满" + event.getMaxPerTeam() + "人"));
                        continue;
                    }
                }

                SportsRegistration reg = new SportsRegistration();
                reg.setMeetingId(meetingId);
                reg.setEventId(event.getId());
                reg.setAthleteName(name.trim());
                reg.setStudentNumber(StringUtils.hasText(studentNumber) ? studentNumber.trim() : null);
                reg.setGender(normalizedGender);
                reg.setDepartment(StringUtils.hasText(department) ? department.trim() : null);
                reg.setClassName(StringUtils.hasText(className) ? className.trim() : null);
                reg.setPhone(StringUtils.hasText(phone) ? phone.trim() : null);
                reg.setImportedBy(importerId);
                reg.setStatus("approved");

                toSave.add(reg);
                successCount++;
            }
        }

        if (!toSave.isEmpty()) {
            regRepo.saveAll(toSave);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        result.put("totalProcessed", successCount + errors.size());
        return result;
    }

    public List<SportsRegistration> listRegistrations(Long meetingId, Long eventId, String department) {
        List<SportsRegistration> all = regRepo.findByMeetingId(meetingId);
        return all.stream()
                .filter(r -> eventId == null || eventId.equals(r.getEventId()))
                .filter(r -> !StringUtils.hasText(department) || department.equals(r.getDepartment()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRegistration(Long regId) {
        regRepo.deleteById(regId);
    }

    @Transactional
    public void clearRegistrations(Long meetingId) {
        regRepo.deleteByMeetingId(meetingId);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  编排管理
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public Map<String, Object> autoScheduleEvent(Long eventId, int lanesPerHeat) {
        SportsEvent event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        List<SportsHeat> oldHeats = heatRepo.findByEventIdOrderByHeatNumberAsc(eventId);
        for (SportsHeat h : oldHeats) {
            laneRepo.deleteByHeatId(h.getId());
        }
        heatRepo.deleteByEventId(eventId);

        List<SportsRegistration> regs = regRepo.findByEventIdAndStatus(eventId, "approved");
        if (regs.isEmpty()) {
            return Map.of("heatCount", 0, "message", "没有报名记录，无法编排");
        }

        Collections.shuffle(regs);

        int lanes = "track".equals(event.getEventCategory()) ? Math.max(lanesPerHeat, 1) : Math.max(lanesPerHeat, 1);
        int heatCount = (int) Math.ceil((double) regs.size() / lanes);

        List<Map<String, Object>> heatDetails = new ArrayList<>();
        for (int h = 0; h < heatCount; h++) {
            SportsHeat heat = new SportsHeat();
            heat.setEventId(eventId);
            heat.setHeatNumber(h + 1);
            heat.setHeatType(heatCount == 1 ? "final" : "preliminary");
            heat = heatRepo.save(heat);

            int start = h * lanes;
            int end = Math.min(start + lanes, regs.size());
            List<SportsRegistration> heatRegs = regs.subList(start, end);

            List<Map<String, Object>> laneDetails = new ArrayList<>();
            for (int l = 0; l < heatRegs.size(); l++) {
                SportsHeatLane lane = new SportsHeatLane();
                lane.setHeatId(heat.getId());
                lane.setRegistrationId(heatRegs.get(l).getId());
                lane.setLaneNumber(l + 1);
                laneRepo.save(lane);

                laneDetails.add(Map.of(
                        "lane", l + 1,
                        "registrationId", heatRegs.get(l).getId(),
                        "athleteName", heatRegs.get(l).getAthleteName(),
                        "department", heatRegs.get(l).getDepartment() != null ? heatRegs.get(l).getDepartment() : ""
                ));
            }

            heatDetails.add(Map.of(
                    "heatId", heat.getId(),
                    "heatNumber", h + 1,
                    "heatType", heat.getHeatType(),
                    "lanes", laneDetails
            ));
        }

        event.setStatus("scheduled");
        eventRepo.save(event);

        Map<String, Object> result = new HashMap<>();
        result.put("heatCount", heatCount);
        result.put("totalAthletes", regs.size());
        result.put("lanesPerHeat", lanes);
        result.put("heats", heatDetails);
        return result;
    }

    @Transactional
    public Map<String, Object> autoScheduleAll(Long meetingId, int lanesPerHeat) {
        List<SportsEvent> events = eventRepo.findByMeetingIdOrderBySortOrderAsc(meetingId);
        List<Map<String, Object>> eventResults = new ArrayList<>();
        int totalHeats = 0;

        for (SportsEvent event : events) {
            Map<String, Object> r = autoScheduleEvent(event.getId(), lanesPerHeat);
            r.put("eventId", event.getId());
            r.put("eventName", event.getName());
            eventResults.add(r);
            totalHeats += (int) r.get("heatCount");
        }

        updateMeetingStatus(meetingId, "scheduling");

        Map<String, Object> result = new HashMap<>();
        result.put("eventCount", events.size());
        result.put("totalHeats", totalHeats);
        result.put("events", eventResults);
        return result;
    }

    public List<Map<String, Object>> getEventHeats(Long eventId) {
        List<SportsHeat> heats = heatRepo.findByEventIdOrderByHeatNumberAsc(eventId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SportsHeat heat : heats) {
            List<SportsHeatLane> lanes = laneRepo.findByHeatIdOrderByLaneNumberAsc(heat.getId());
            List<Map<String, Object>> laneList = new ArrayList<>();
            for (SportsHeatLane lane : lanes) {
                SportsRegistration reg = regRepo.findById(lane.getRegistrationId()).orElse(null);
                Map<String, Object> laneMap = new HashMap<>();
                laneMap.put("id", lane.getId());
                laneMap.put("laneNumber", lane.getLaneNumber());
                laneMap.put("registrationId", lane.getRegistrationId());
                if (reg != null) {
                    laneMap.put("athleteName", reg.getAthleteName());
                    laneMap.put("studentNumber", reg.getStudentNumber());
                    laneMap.put("department", reg.getDepartment());
                    laneMap.put("className", reg.getClassName());
                }
                laneList.add(laneMap);
            }

            Map<String, Object> heatMap = new HashMap<>();
            heatMap.put("id", heat.getId());
            heatMap.put("heatNumber", heat.getHeatNumber());
            heatMap.put("heatType", heat.getHeatType());
            heatMap.put("scheduledTime", heat.getScheduledTime());
            heatMap.put("status", heat.getStatus());
            heatMap.put("lanes", laneList);
            result.add(heatMap);
        }
        return result;
    }

    @Transactional
    public void updateHeatLanes(Long heatId, List<Map<String, Object>> laneUpdates) {
        laneRepo.deleteByHeatId(heatId);
        for (Map<String, Object> lu : laneUpdates) {
            SportsHeatLane lane = new SportsHeatLane();
            lane.setHeatId(heatId);
            lane.setRegistrationId(((Number) lu.get("registrationId")).longValue());
            lane.setLaneNumber(((Number) lu.get("laneNumber")).intValue());
            laneRepo.save(lane);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  成绩管理
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public SportsResult saveResult(Long eventId, Long registrationId, String resultValue,
                                   Long resultMs, Integer resultCm, String remark, String recordedBy) {
        SportsEvent event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        Optional<SportsResult> existing = resultRepo.findByEventIdAndRegistrationId(eventId, registrationId);
        SportsResult sr;
        if (existing.isPresent()) {
            sr = existing.get();
        } else {
            sr = new SportsResult();
            sr.setEventId(eventId);
            sr.setRegistrationId(registrationId);
        }
        sr.setResultValue(resultValue);
        sr.setResultMs(resultMs != null ? resultMs : 0L);
        sr.setResultCm(resultCm != null ? resultCm : 0);
        sr.setRemark(remark);
        sr.setRecordedBy(recordedBy);
        return resultRepo.save(sr);
    }

    @Transactional
    public List<SportsResult> calculateRankings(Long eventId) {
        SportsEvent event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        List<SportsResult> results = resultRepo.findByEventIdOrderByRankingAsc(eventId);

        results = results.stream()
                .filter(r -> !"DNF".equals(r.getRemark()) && !"DQ".equals(r.getRemark()))
                .collect(Collectors.toList());

        if ("time".equals(event.getScoringType())) {
            results.sort(Comparator.comparingLong(SportsResult::getResultMs));
        } else {
            results.sort((a, b) -> Integer.compare(b.getResultCm(), a.getResultCm()));
        }

        List<SportsScoreRule> rules = scoreRuleRepo.findByMeetingIdOrderByRankingAsc(event.getMeetingId());
        Map<Integer, Integer> scoreMap = rules.stream()
                .collect(Collectors.toMap(SportsScoreRule::getRanking, SportsScoreRule::getScore));

        for (int i = 0; i < results.size(); i++) {
            SportsResult r = results.get(i);
            r.setRanking(i + 1);
            r.setScore(scoreMap.getOrDefault(i + 1, 0));
        }

        resultRepo.saveAll(results);

        event.setStatus("completed");
        eventRepo.save(event);

        return results;
    }

    public List<Map<String, Object>> getEventResults(Long eventId) {
        List<SportsResult> results = resultRepo.findByEventIdOrderByRankingAsc(eventId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (SportsResult r : results) {
            SportsRegistration reg = regRepo.findById(r.getRegistrationId()).orElse(null);
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("eventId", r.getEventId());
            map.put("registrationId", r.getRegistrationId());
            map.put("resultValue", r.getResultValue());
            map.put("resultMs", r.getResultMs());
            map.put("resultCm", r.getResultCm());
            map.put("ranking", r.getRanking());
            map.put("score", r.getScore());
            map.put("remark", r.getRemark());
            if (reg != null) {
                map.put("athleteName", reg.getAthleteName());
                map.put("studentNumber", reg.getStudentNumber());
                map.put("department", reg.getDepartment());
                map.put("className", reg.getClassName());
                map.put("gender", reg.getGender());
            }
            list.add(map);
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  计分规则
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public List<SportsScoreRule> saveScoreRules(Long meetingId, List<Map<String, Integer>> rules) {
        scoreRuleRepo.deleteByMeetingId(meetingId);
        List<SportsScoreRule> saved = new ArrayList<>();
        for (Map<String, Integer> rule : rules) {
            SportsScoreRule sr = new SportsScoreRule();
            sr.setMeetingId(meetingId);
            sr.setRanking(rule.get("ranking"));
            sr.setScore(rule.get("score"));
            saved.add(scoreRuleRepo.save(sr));
        }
        return saved;
    }

    public List<SportsScoreRule> getScoreRules(Long meetingId) {
        return scoreRuleRepo.findByMeetingIdOrderByRankingAsc(meetingId);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  团体积分排名 & 统计
    // ═══════════════════════════════════════════════════════════════════

    public List<Map<String, Object>> getTeamRankings(Long meetingId) {
        List<String> departments = regRepo.findDistinctDepartmentsByMeetingId(meetingId);
        List<SportsResult> allResults = resultRepo.findAllByMeetingId(meetingId);

        Map<Long, SportsRegistration> regCache = new HashMap<>();
        for (SportsResult r : allResults) {
            if (!regCache.containsKey(r.getRegistrationId())) {
                regRepo.findById(r.getRegistrationId()).ifPresent(reg -> regCache.put(reg.getId(), reg));
            }
        }

        List<Map<String, Object>> rankings = new ArrayList<>();
        for (String dept : departments) {
            if (dept == null) continue;
            int totalScore = 0;
            int gold = 0, silver = 0, bronze = 0;

            for (SportsResult r : allResults) {
                SportsRegistration reg = regCache.get(r.getRegistrationId());
                if (reg != null && dept.equals(reg.getDepartment())) {
                    totalScore += r.getScore();
                    if (r.getRanking() != null) {
                        if (r.getRanking() == 1) gold++;
                        else if (r.getRanking() == 2) silver++;
                        else if (r.getRanking() == 3) bronze++;
                    }
                }
            }

            Map<String, Object> rankItem = new HashMap<>();
            rankItem.put("department", dept);
            rankItem.put("totalScore", totalScore);
            rankItem.put("gold", gold);
            rankItem.put("silver", silver);
            rankItem.put("bronze", bronze);
            rankItem.put("totalMedals", gold + silver + bronze);
            rankings.add(rankItem);
        }

        rankings.sort((a, b) -> {
            int cmp = Integer.compare((int) b.get("totalScore"), (int) a.get("totalScore"));
            if (cmp != 0) return cmp;
            cmp = Integer.compare((int) b.get("gold"), (int) a.get("gold"));
            if (cmp != 0) return cmp;
            return Integer.compare((int) b.get("silver"), (int) a.get("silver"));
        });

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).put("rank", i + 1);
        }
        return rankings;
    }

    public Map<String, Object> getFullStatistics(Long meetingId) {
        SportsMeeting meeting = getMeeting(meetingId);
        Map<String, Object> stats = getMeetingStatistics(meetingId);
        stats.put("meeting", meetingToMap(meeting));
        stats.put("rankings", getTeamRankings(meetingId));

        List<SportsEvent> events = eventRepo.findByMeetingIdOrderBySortOrderAsc(meetingId);
        List<Map<String, Object>> eventStats = new ArrayList<>();
        for (SportsEvent e : events) {
            Map<String, Object> es = new HashMap<>();
            es.put("id", e.getId());
            es.put("name", e.getName());
            es.put("category", e.getEventCategory());
            es.put("gender", e.getGender());
            es.put("status", e.getStatus());
            es.put("registrationCount", regRepo.countByEventId(e.getId()));
            es.put("resultCount", resultRepo.countByEventId(e.getId()));
            es.put("heatCount", heatRepo.countByEventId(e.getId()));
            eventStats.add(es);
        }
        stats.put("events", eventStats);
        return stats;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  工具方法
    // ═══════════════════════════════════════════════════════════════════

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String val = cell.getStringCellValue();
        return StringUtils.hasText(val) ? val.trim() : null;
    }

    private String normalizeGender(String gender) {
        if (gender == null) return "male";
        gender = gender.trim();
        if ("女".equals(gender) || "female".equalsIgnoreCase(gender) || "F".equalsIgnoreCase(gender)) return "female";
        return "male";
    }

    private String genderLabel(String gender) {
        if ("female".equals(gender)) return "女";
        if ("mixed".equals(gender)) return "混合";
        return "男";
    }

    public Map<String, Object> meetingToMap(SportsMeeting m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("description", m.getDescription());
        map.put("startDate", m.getStartDate() != null ? m.getStartDate().toString() : null);
        map.put("endDate", m.getEndDate() != null ? m.getEndDate().toString() : null);
        map.put("location", m.getLocation());
        map.put("status", m.getStatus());
        map.put("school", m.getSchool());
        map.put("maxEventsPerPerson", m.getMaxEventsPerPerson());
        map.put("createdBy", m.getCreatedBy());
        map.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
        map.put("updatedAt", m.getUpdatedAt() != null ? m.getUpdatedAt().toString() : null);
        return map;
    }

    public Map<String, Object> eventToMap(SportsEvent e) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", e.getId());
        map.put("meetingId", e.getMeetingId());
        map.put("name", e.getName());
        map.put("eventCategory", e.getEventCategory());
        map.put("gender", e.getGender());
        map.put("maxPerTeam", e.getMaxPerTeam());
        map.put("isRelay", e.getIsRelay());
        map.put("relayMembers", e.getRelayMembers());
        map.put("eventDate", e.getEventDate() != null ? e.getEventDate().toString() : null);
        map.put("eventTime", e.getEventTime());
        map.put("venue", e.getVenue());
        map.put("sortOrder", e.getSortOrder());
        map.put("scoringType", e.getScoringType());
        map.put("status", e.getStatus());
        return map;
    }
}
