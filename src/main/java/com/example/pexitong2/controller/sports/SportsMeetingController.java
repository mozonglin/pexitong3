package com.example.pexitong2.controller.sports;

import com.example.pexitong2.entity.sports.*;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.service.sports.SportsMeetingService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/sports")
public class SportsMeetingController {

    @Autowired
    private SportsMeetingService service;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepo;

    // ── 运动会 CRUD ─────────────────────────────────────────────────────

    @PostMapping("/meetings")
    public ResponseEntity<Map<String, Object>> createMeeting(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            SportsMeeting m = new SportsMeeting();
            m.setName((String) body.get("name"));
            m.setDescription((String) body.get("description"));
            if (body.get("startDate") != null) m.setStartDate(LocalDate.parse((String) body.get("startDate")));
            if (body.get("endDate") != null) m.setEndDate(LocalDate.parse((String) body.get("endDate")));
            m.setLocation((String) body.get("location"));
            m.setSchool((String) body.get("school"));
            if (body.get("maxEventsPerPerson") != null)
                m.setMaxEventsPerPerson(((Number) body.get("maxEventsPerPerson")).intValue());
            m.setCreatedBy(userId);

            SportsMeeting saved = service.createMeeting(m);
            return ok("创建成功", service.meetingToMap(saved));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/meetings")
    public ResponseEntity<Map<String, Object>> listMeetings(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            Page<SportsMeeting> result = service.listMeetings(school, status, page, pageSize);
            List<Map<String, Object>> list = result.getContent().stream()
                    .map(service::meetingToMap).collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", result.getTotalElements());
            data.put("totalPages", result.getTotalPages());
            data.put("page", page);
            return ok("获取成功", data);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/meetings/{id}")
    public ResponseEntity<Map<String, Object>> getMeeting(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            SportsMeeting m = service.getMeeting(id);
            Map<String, Object> data = service.meetingToMap(m);
            data.put("statistics", service.getMeetingStatistics(id));
            return ok("获取成功", data);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PutMapping("/meetings/{id}")
    public ResponseEntity<Map<String, Object>> updateMeeting(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            SportsMeeting updated = new SportsMeeting();
            updated.setName((String) body.get("name"));
            updated.setDescription((String) body.get("description"));
            if (body.get("startDate") != null) updated.setStartDate(LocalDate.parse((String) body.get("startDate")));
            if (body.get("endDate") != null) updated.setEndDate(LocalDate.parse((String) body.get("endDate")));
            updated.setLocation((String) body.get("location"));
            updated.setSchool((String) body.get("school"));
            if (body.get("maxEventsPerPerson") != null)
                updated.setMaxEventsPerPerson(((Number) body.get("maxEventsPerPerson")).intValue());

            SportsMeeting saved = service.updateMeeting(id, updated);
            return ok("更新成功", service.meetingToMap(saved));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PutMapping("/meetings/{id}/status")
    public ResponseEntity<Map<String, Object>> updateMeetingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            service.updateMeetingStatus(id, body.get("status"));
            return ok("状态更新成功", null);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @DeleteMapping("/meetings/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeeting(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            service.deleteMeeting(id);
            return ok("删除成功", null);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── 项目管理 ─────────────────────────────────────────────────────────

    @PostMapping("/meetings/{meetingId}/events")
    public ResponseEntity<Map<String, Object>> createEvent(
            @PathVariable Long meetingId,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            SportsEvent event = parseEvent(body);
            SportsEvent saved = service.createEvent(meetingId, event);
            return ok("创建成功", service.eventToMap(saved));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/meetings/{meetingId}/events/batch")
    public ResponseEntity<Map<String, Object>> batchCreateEvents(
            @PathVariable Long meetingId,
            @RequestBody List<Map<String, Object>> bodyList,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            List<SportsEvent> events = bodyList.stream().map(this::parseEvent).collect(Collectors.toList());
            List<SportsEvent> saved = service.batchCreateEvents(meetingId, events);
            List<Map<String, Object>> list = saved.stream().map(service::eventToMap).collect(Collectors.toList());
            return ok("批量创建成功", Map.of("count", list.size(), "events", list));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/meetings/{meetingId}/events")
    public ResponseEntity<Map<String, Object>> listEvents(
            @PathVariable Long meetingId,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            List<SportsEvent> events = service.listEvents(meetingId);
            List<Map<String, Object>> list = events.stream().map(service::eventToMap).collect(Collectors.toList());
            return ok("获取成功", list);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> updateEvent(
            @PathVariable Long eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            SportsEvent updated = parseEvent(body);
            SportsEvent saved = service.updateEvent(eventId, updated);
            return ok("更新成功", service.eventToMap(saved));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> deleteEvent(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            service.deleteEvent(eventId);
            return ok("删除成功", null);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── 报名管理 ─────────────────────────────────────────────────────────

    @GetMapping("/meetings/{meetingId}/registration/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable Long meetingId,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            byte[] data = service.generateRegistrationTemplate(meetingId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registration_template.xlsx")
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/meetings/{meetingId}/registration/import")
    public ResponseEntity<Map<String, Object>> importRegistrations(
            @PathVariable Long meetingId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            if (file == null || file.isEmpty())
                return bad("文件不能为空");
            String fname = file.getOriginalFilename();
            if (fname == null || (!fname.endsWith(".xlsx") && !fname.endsWith(".xls")))
                return bad("仅支持 .xlsx / .xls 文件");

            Map<String, Object> result = service.importRegistrations(meetingId, file, userId);
            return ok("导入完成", result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/meetings/{meetingId}/registrations")
    public ResponseEntity<Map<String, Object>> listRegistrations(
            @PathVariable Long meetingId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String department,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            List<SportsRegistration> regs = service.listRegistrations(meetingId, eventId, department);
            List<Map<String, Object>> list = regs.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("meetingId", r.getMeetingId());
                m.put("eventId", r.getEventId());
                m.put("athleteName", r.getAthleteName());
                m.put("studentNumber", r.getStudentNumber());
                m.put("gender", r.getGender());
                m.put("department", r.getDepartment());
                m.put("className", r.getClassName());
                m.put("phone", r.getPhone());
                m.put("status", r.getStatus());
                m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
                return m;
            }).collect(Collectors.toList());
            return ok("获取成功", Map.of("list", list, "total", list.size()));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @DeleteMapping("/registrations/{regId}")
    public ResponseEntity<Map<String, Object>> deleteRegistration(
            @PathVariable Long regId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            service.deleteRegistration(regId);
            return ok("删除成功", null);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @DeleteMapping("/meetings/{meetingId}/registrations")
    public ResponseEntity<Map<String, Object>> clearRegistrations(
            @PathVariable Long meetingId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            service.clearRegistrations(meetingId);
            return ok("清空成功", null);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── 编排管理 ─────────────────────────────────────────────────────────

    @PostMapping("/events/{eventId}/schedule")
    public ResponseEntity<Map<String, Object>> autoScheduleEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "8") int lanesPerHeat,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            Map<String, Object> result = service.autoScheduleEvent(eventId, lanesPerHeat);
            return ok("编排成功", result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/meetings/{meetingId}/schedule-all")
    public ResponseEntity<Map<String, Object>> autoScheduleAll(
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "8") int lanesPerHeat,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            Map<String, Object> result = service.autoScheduleAll(meetingId, lanesPerHeat);
            return ok("全部编排成功", result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/events/{eventId}/heats")
    public ResponseEntity<Map<String, Object>> getEventHeats(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            List<Map<String, Object>> heats = service.getEventHeats(eventId);
            return ok("获取成功", heats);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PutMapping("/heats/{heatId}/lanes")
    public ResponseEntity<Map<String, Object>> updateHeatLanes(
            @PathVariable Long heatId,
            @RequestBody List<Map<String, Object>> lanes,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            service.updateHeatLanes(heatId, lanes);
            return ok("更新成功", null);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── 成绩管理 ─────────────────────────────────────────────────────────

    @PostMapping("/events/{eventId}/results")
    public ResponseEntity<Map<String, Object>> saveResult(
            @PathVariable Long eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            Long registrationId = ((Number) body.get("registrationId")).longValue();
            String resultValue = (String) body.get("resultValue");
            Long resultMs = body.get("resultMs") != null ? ((Number) body.get("resultMs")).longValue() : null;
            Integer resultCm = body.get("resultCm") != null ? ((Number) body.get("resultCm")).intValue() : null;
            String remark = (String) body.get("remark");

            SportsResult saved = service.saveResult(eventId, registrationId, resultValue, resultMs, resultCm, remark, userId);
            return ok("保存成功", Map.of("id", saved.getId()));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/events/{eventId}/results/batch")
    public ResponseEntity<Map<String, Object>> batchSaveResults(
            @PathVariable Long eventId,
            @RequestBody List<Map<String, Object>> bodyList,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);

            int count = 0;
            for (Map<String, Object> body : bodyList) {
                Long registrationId = ((Number) body.get("registrationId")).longValue();
                String resultValue = (String) body.get("resultValue");
                Long resultMs = body.get("resultMs") != null ? ((Number) body.get("resultMs")).longValue() : null;
                Integer resultCm = body.get("resultCm") != null ? ((Number) body.get("resultCm")).intValue() : null;
                String remark = (String) body.get("remark");
                service.saveResult(eventId, registrationId, resultValue, resultMs, resultCm, remark, userId);
                count++;
            }
            return ok("批量保存成功", Map.of("count", count));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/events/{eventId}/results/calculate")
    public ResponseEntity<Map<String, Object>> calculateRankings(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            List<SportsResult> results = service.calculateRankings(eventId);
            return ok("排名计算完成", Map.of("count", results.size()));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/events/{eventId}/results")
    public ResponseEntity<Map<String, Object>> getEventResults(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            List<Map<String, Object>> results = service.getEventResults(eventId);
            return ok("获取成功", results);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── 计分规则 ─────────────────────────────────────────────────────────

    @PostMapping("/meetings/{meetingId}/score-rules")
    public ResponseEntity<Map<String, Object>> saveScoreRules(
            @PathVariable Long meetingId,
            @RequestBody List<Map<String, Integer>> rules,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            checkSchoolAdmin(userId);
            List<SportsScoreRule> saved = service.saveScoreRules(meetingId, rules);
            return ok("保存成功", Map.of("count", saved.size()));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/meetings/{meetingId}/score-rules")
    public ResponseEntity<Map<String, Object>> getScoreRules(
            @PathVariable Long meetingId,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            List<SportsScoreRule> rules = service.getScoreRules(meetingId);
            List<Map<String, Object>> list = rules.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("ranking", r.getRanking());
                m.put("score", r.getScore());
                return m;
            }).collect(Collectors.toList());
            return ok("获取成功", list);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── 团体排名 & 统计 ─────────────────────────────────────────────────

    @GetMapping("/meetings/{meetingId}/rankings")
    public ResponseEntity<Map<String, Object>> getTeamRankings(
            @PathVariable Long meetingId,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            List<Map<String, Object>> rankings = service.getTeamRankings(meetingId);
            return ok("获取成功", rankings);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/meetings/{meetingId}/statistics")
    public ResponseEntity<Map<String, Object>> getFullStatistics(
            @PathVariable Long meetingId,
            @RequestHeader("Authorization") String token) {
        try {
            extractUserId(token);
            Map<String, Object> stats = service.getFullStatistics(meetingId);
            return ok("获取成功", stats);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── 工具方法 ─────────────────────────────────────────────────────────

    private String extractUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
        return jwtUtil.extractUserId(token);
    }

    private void checkSchoolAdmin(String userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getUserType() != User.UserType.school_admin && user.getUserType() != User.UserType.super_admin) {
            throw new RuntimeException("权限不足，仅校级管理员可操作");
        }
    }

    private SportsEvent parseEvent(Map<String, Object> body) {
        SportsEvent e = new SportsEvent();
        e.setName((String) body.get("name"));
        e.setEventCategory((String) body.get("eventCategory"));
        e.setGender((String) body.get("gender"));
        if (body.get("maxPerTeam") != null) e.setMaxPerTeam(((Number) body.get("maxPerTeam")).intValue());
        if (body.get("isRelay") != null) e.setIsRelay((Boolean) body.get("isRelay"));
        if (body.get("relayMembers") != null) e.setRelayMembers(((Number) body.get("relayMembers")).intValue());
        if (body.get("eventDate") != null) e.setEventDate(LocalDate.parse((String) body.get("eventDate")));
        e.setEventTime((String) body.get("eventTime"));
        e.setVenue((String) body.get("venue"));
        if (body.get("sortOrder") != null) e.setSortOrder(((Number) body.get("sortOrder")).intValue());
        e.setScoringType((String) body.get("scoringType"));
        if (body.get("status") != null) e.setStatus((String) body.get("status"));
        return e;
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("message", message);
        if (data != null) body.put("data", data);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> bad(String message) {
        return ResponseEntity.badRequest().body(Map.of("code", 400, "message", message));
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.internalServerError().body(Map.of(
                "code", 500, "message", message != null ? message : "服务器内部错误"));
    }
}
