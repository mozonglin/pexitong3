package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.pe.PeStatisticsService;
import com.example.pexitong2.service.pe.PeStatsCacheService;
import com.example.pexitong2.util.JwtUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * PE统计管理控制器
 * 仅开放给校级管理员和院级管理员
 */
@RestController
@RequestMapping("/pe/admin/statistics")
@CrossOrigin(origins = "*")
public class PeStatisticsController {
    
    @Autowired
    private PeStatisticsService peStatisticsService;

    @Autowired
    private PeStatsCacheService peStatsCacheService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;
    
    /**
     * 校级管理员设置PE积分指标
     */
    @PostMapping("/targets")
    public PeApiResponse<String> setSchoolPeTargets(
            @RequestBody PeTargetRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            peStatisticsService.setSchoolPeTargets(currentUserId, request);
            
            return PeApiResponse.success("PE积分指标设置成功", null);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取校级管理员统计数据
     * 包括：学校总体达标率、各院系达标率、院系排名、学生总数、各院人数
     */
    @GetMapping("/school")
    public PeApiResponse<SchoolStatisticsResponse> getSchoolStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            SchoolStatisticsResponse statistics = peStatisticsService.getSchoolStatistics(currentUserId);
            
            return PeApiResponse.success("获取成功", statistics);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取院级管理员统计数据
     * 包括：本院整体达标率、各班达标率、班级排名
     */
    @GetMapping("/college")
    public PeApiResponse<CollegeStatisticsResponse> getCollegeStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            CollegeStatisticsResponse statistics = peStatisticsService.getCollegeStatistics(currentUserId);
            
            return PeApiResponse.success("获取成功", statistics);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取校级管理员阳光跑统计数据（按院系统计和排名）
     */
    @GetMapping("/sunshine-run/school")
    public PeApiResponse<SunshineRunStatisticsResponse> getSchoolSunshineRunStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String period) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            if (hasPeriodFilter(period)) {
                String cacheKey = currentUserId + ":school:" + period;
                return PeApiResponse.success("获取成功",
                        peStatsCacheService.buildFilteredSunshineRunStats(cacheKey, currentUserId, period, "school"));
            }
            SunshineRunStatisticsResponse statistics = peStatisticsService.getSchoolSunshineRunStatistics(currentUserId);
            return PeApiResponse.success("获取成功", statistics);
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取院级管理员阳光跑统计数据（按班级统计和排名）
     * 校级管理员访问时返回全校所有班级的数据
     */
    @GetMapping("/sunshine-run/college")
    public PeApiResponse<SunshineRunStatisticsResponse> getCollegeSunshineRunStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String period) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            if (hasPeriodFilter(period)) {
                String cacheKey = currentUserId + ":college:" + period;
                return PeApiResponse.success("获取成功",
                        peStatsCacheService.buildFilteredSunshineRunStats(cacheKey, currentUserId, period, "college"));
            }
            SunshineRunStatisticsResponse statistics = peStatisticsService.getCollegeSunshineRunStatistics(currentUserId);
            return PeApiResponse.success("获取成功", statistics);
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    // ── 阳光跑导出 Excel ──────────────────────────────────────────────────────

    @GetMapping("/sunshine-run/export")
    public ResponseEntity<byte[]> exportSunshineRun(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false, defaultValue = "school") String scope) {
        try {
            String currentUserId = getCurrentUserId(token);
            User admin = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new SecurityException("用户不存在"));

            if (admin.getUserType() != User.UserType.school_admin
                    && admin.getUserType() != User.UserType.department_admin
                    && admin.getUserType() != User.UserType.super_admin) {
                return ResponseEntity.status(403).build();
            }

            String school = admin.getSchool();
            String sql;
            Object[] params;

            if (admin.getUserType() == User.UserType.department_admin
                    && admin.getDepartmentName() != null && !admin.getDepartmentName().isBlank()) {
                sql = "SELECT name, student_id, school, college, class_name, " +
                      "sunshine_total_runs, sunshine_total_distance, sunshine_total_duration " +
                      "FROM users1 WHERE school = ? AND college = ? AND role = 'STUDENT' " +
                      "ORDER BY sunshine_total_distance DESC";
                params = new Object[]{school, admin.getDepartmentName().trim()};
            } else {
                sql = "SELECT name, student_id, school, college, class_name, " +
                      "sunshine_total_runs, sunshine_total_distance, sunshine_total_duration " +
                      "FROM users1 WHERE school = ? AND role = 'STUDENT' " +
                      "ORDER BY sunshine_total_distance DESC";
                params = new Object[]{school};
            }

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

            for (Map<String, Object> row : rows) {
                Object dist = row.get("sunshine_total_distance");
                if (dist instanceof Number) {
                    row.put("sunshine_total_distance", String.format("%.2f", ((Number) dist).doubleValue()));
                }
                Object dur = row.get("sunshine_total_duration");
                if (dur instanceof Number) {
                    long totalSec = ((Number) dur).longValue() / 1000;
                    row.put("sunshine_total_duration", String.format("%d:%02d:%02d", totalSec / 3600, (totalSec % 3600) / 60, totalSec % 60));
                }
            }

            byte[] bytes = buildExportExcel("阳光跑统计",
                    new String[]{"姓名", "学号", "学校", "学院", "班级", "跑步次数", "总距离(m)", "总时长"},
                    rows, new String[]{"name", "student_id", "school", "college", "class_name",
                            "sunshine_total_runs", "sunshine_total_distance", "sunshine_total_duration"});

            String filename = java.net.URLEncoder.encode("阳光跑统计.xlsx", java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] buildExportExcel(String sheetName, String[] headers, List<Map<String, Object>> rows, String[] keys) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(sheetName);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                Map<String, Object> data = rows.get(r);
                for (int c = 0; c < keys.length; c++) {
                    Object val = data.get(keys[c]);
                    Cell cell = row.createCell(c);
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── 工具方法 ────────────────────────────────────────────────────────────────

    private boolean hasPeriodFilter(String period) {
        return period != null && !period.isBlank() && !"all".equalsIgnoreCase(period);
    }

    private String getCurrentUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.extractUserId(token);
    }
}



