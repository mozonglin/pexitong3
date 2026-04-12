package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.service.pe.MorningExerciseService;
import com.example.pexitong2.util.JwtUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 早操管理控制器
 */
@RestController
@RequestMapping("/pe/morning-exercises")
@CrossOrigin(origins = "*")
public class MorningExerciseController {
    
    @Autowired
    private MorningExerciseService morningExerciseService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取早操活动列表
     */
    @GetMapping
    public PeApiResponse<PageResponse<MorningExerciseResponse>> getMorningExercises(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<MorningExerciseResponse> exercises = morningExerciseService.getMorningExercises(
                page, pageSize, date, isActive, startDate, endDate, currentUserId);
            
            return PeApiResponse.success("获取成功", exercises);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    /**
     * 早操出勤率统计大屏（校级：院系+班级；院级：仅班级；出勤=已签退）
     */
    @GetMapping("/attendance-dashboard")
    public PeApiResponse<MorningExerciseAttendanceDashboardResponse> getAttendanceDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {

        try {
            String currentUserId = getCurrentUserId(token);
            MorningExerciseAttendanceDashboardResponse data =
                morningExerciseService.getAttendanceDashboard(startDate, endDate, currentUserId);
            return PeApiResponse.success("获取成功", data);
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取单个早操活动详情
     */
    @GetMapping("/{id}")
    public PeApiResponse<MorningExerciseResponse> getMorningExerciseById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            MorningExerciseResponse exercise = morningExerciseService.getMorningExerciseById(id, currentUserId);
            
            return PeApiResponse.success("获取成功", exercise);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 创建早操活动
     */
    @PostMapping
    public PeApiResponse<MorningExerciseResponse> createMorningExercise(
            @RequestBody MorningExerciseRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            MorningExerciseResponse exercise = morningExerciseService.createMorningExercise(request, currentUserId);
            
            return PeApiResponse.success("早操活动创建成功", exercise);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新早操活动
     */
    @PutMapping("/{id}")
    public PeApiResponse<MorningExerciseResponse> updateMorningExercise(
            @PathVariable String id,
            @RequestBody MorningExerciseRequest request,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            MorningExerciseResponse exercise = morningExerciseService.updateMorningExercise(id, request, currentUserId);
            
            return PeApiResponse.success("早操活动更新成功", exercise);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除早操活动
     */
    @DeleteMapping("/{id}")
    public PeApiResponse<Void> deleteMorningExercise(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            morningExerciseService.deleteMorningExercise(id, currentUserId);
            
            return PeApiResponse.success("早操活动删除成功", null);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取早操考勤记录
     */
    @GetMapping("/{id}/attendance")
    public PeApiResponse<PageResponse<MorningExerciseAttendanceResponse>> getMorningExerciseAttendance(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Boolean isCheckedOut,
            @RequestParam(required = false) String checkedBy,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<MorningExerciseAttendanceResponse> attendance = 
                morningExerciseService.getMorningExerciseAttendance(
                    id, page, pageSize, studentId, isCheckedOut, checkedBy, currentUserId);
            
            return PeApiResponse.success("获取成功", attendance);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 早操考勤统计导出 Excel（最多4个月）
     */
    @GetMapping("/attendance-dashboard/export")
    public ResponseEntity<byte[]> exportAttendanceDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        try {
            String currentUserId = getCurrentUserId(token);

            LocalDate end = (endDate != null && !endDate.isBlank())
                    ? LocalDate.parse(endDate) : LocalDate.now();
            LocalDate fourMonthsAgo = end.minusMonths(4);
            LocalDate start = (startDate != null && !startDate.isBlank())
                    ? LocalDate.parse(startDate) : fourMonthsAgo;
            if (start.isBefore(fourMonthsAgo)) {
                start = fourMonthsAgo;
            }

            String sd = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String ed = end.format(DateTimeFormatter.ISO_LOCAL_DATE);

            MorningExerciseAttendanceDashboardResponse data =
                    morningExerciseService.getAttendanceDashboard(sd, ed, currentUserId);

            List<Map<String, Object>> rows = new ArrayList<>();
            if (data.isShowCollegeStats() && data.getColleges() != null) {
                for (var c : data.getColleges()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("college", c.getCollegeName());
                    m.put("className", "—");
                    m.put("headcount", c.getStudentHeadcount());
                    m.put("totalSlots", c.getTotalSlots());
                    m.put("presentSlots", c.getPresentSlots());
                    m.put("absentSlots", c.getAbsentSlots());
                    m.put("rate", String.format("%.2f%%", c.getAttendanceRatePercent()));
                    rows.add(m);
                }
            }
            if (data.getClasses() != null) {
                for (var c : data.getClasses()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("college", c.getCollegeName());
                    m.put("className", c.getClassName());
                    m.put("headcount", c.getStudentHeadcount());
                    m.put("totalSlots", c.getTotalSlots());
                    m.put("presentSlots", c.getPresentSlots());
                    m.put("absentSlots", c.getAbsentSlots());
                    m.put("rate", String.format("%.2f%%", c.getAttendanceRatePercent()));
                    rows.add(m);
                }
            }

            byte[] bytes = buildExportExcel("早操考勤统计",
                    new String[]{"学院", "班级", "学生人数", "应到人次", "实到人次", "缺勤人次", "出勤率"},
                    rows, new String[]{"college", "className", "headcount", "totalSlots", "presentSlots", "absentSlots", "rate"});

            String filename = java.net.URLEncoder.encode("早操考勤统计.xlsx", java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
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

    /**
     * 从Token中获取当前用户ID
     */
    private String getCurrentUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtUtil.extractUserId(token);
    }
}
