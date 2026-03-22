package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.entity.Equipment;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.Venue;
import com.example.pexitong2.repository.EquipmentCategoryRepository;
import com.example.pexitong2.repository.EquipmentRepository;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.VenueRepository;
import com.example.pexitong2.service.equipment.EquipmentPermissionService;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/import")
public class ImportController {

    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private EquipmentCategoryRepository categoryRepository;
    @Autowired private EquipmentPermissionService permissionService;
    @Autowired private JwtUtil jwtUtil;

    // ==================== 场馆导入 ====================

    /** 下载场馆导入模板 */
    @GetMapping("/venues/template")
    public ResponseEntity<byte[]> venueTemplate(HttpServletRequest req) {
        try {
            extractUserId(req); // auth check
            byte[] data = buildVenueTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"venue_import_template.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** 批量导入场馆 */
    @PostMapping("/venues")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importVenues(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest req) {
        try {
            String userId = extractUserId(req);
            permissionService.validateEquipmentManagementPermission(userId);

            User operator = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String school = operator.getSchool();

            List<String> errors = new ArrayList<>();
            List<Venue> toSave = new ArrayList<>();
            int rowNum = 1;

            try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = wb.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // skip header
                    rowNum++;
                    String name = getString(row, 0);
                    if (name == null || name.isBlank()) continue;

                    String type = getString(row, 1);
                    if (type == null || type.isBlank()) {
                        errors.add("第" + rowNum + "行：场馆类型不能为空"); continue;
                    }
                    Integer capacity = getInt(row, 2);
                    if (capacity == null) {
                        errors.add("第" + rowNum + "行：容量格式错误"); continue;
                    }

                    Venue venue = new Venue();
                    venue.setId(UUID.randomUUID().toString());
                    venue.setName(name);
                    venue.setType(mapVenueType(type));
                    venue.setCapacity(capacity);
                    venue.setLocation(getString(row, 3));
                    String priceStr = getString(row, 4);
                    venue.setPrice(priceStr != null ? new BigDecimal(priceStr) : BigDecimal.ZERO);
                    venue.setOpenTime(getString(row, 5));
                    venue.setDescription(getString(row, 6));
                    venue.setStatus("available");
                    venue.setSchool(school);
                    venue.setCreatedBy(userId);
                    toSave.add(venue);
                }
            }

            if (!toSave.isEmpty()) venueRepository.saveAll(toSave);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("imported", toSave.size());
            result.put("errors", errors);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 器材导入 ====================

    /** 下载器材导入模板 */
    @GetMapping("/equipment/template")
    public ResponseEntity<byte[]> equipmentTemplate(HttpServletRequest req) {
        try {
            extractUserId(req);
            byte[] data = buildEquipmentTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"equipment_import_template.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** 批量导入器材 */
    @PostMapping("/equipment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importEquipment(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest req) {
        try {
            String userId = extractUserId(req);
            permissionService.validateEquipmentManagementPermission(userId);

            User operator = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            String school = operator.getSchool();

            // 构建分类名->ID映射
            Map<String, String> categoryMap = new HashMap<>();
            categoryRepository.findAll().forEach(c -> categoryMap.put(c.getName(), c.getId()));

            List<String> errors = new ArrayList<>();
            List<Equipment> toSave = new ArrayList<>();
            int rowNum = 1;

            try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = wb.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    rowNum++;
                    String name = getString(row, 0);
                    if (name == null || name.isBlank()) continue;

                    String categoryName = getString(row, 1);
                    String categoryId = categoryMap.get(categoryName);
                    if (categoryId == null) {
                        errors.add("第" + rowNum + "行：分类「" + categoryName + "」不存在，请使用正确的分类名"); continue;
                    }
                    Integer totalQty = getInt(row, 2);
                    if (totalQty == null || totalQty < 0) {
                        errors.add("第" + rowNum + "行：总数量格式错误"); continue;
                    }

                    Equipment eq = new Equipment();
                    eq.setCategoryId(categoryId);
                    eq.setName(name);
                    eq.setModel(getString(row, 3));
                    eq.setSpecification(getString(row, 4));
                    eq.setTotalQuantity(totalQty);
                    eq.setAvailableQuantity(totalQty);
                    String priceStr = getString(row, 5);
                    if (priceStr != null) { try { eq.setUnitPrice(new BigDecimal(priceStr)); } catch (Exception ignored) {} }
                    String dateStr = getString(row, 6);
                    if (dateStr != null) { try { eq.setPurchaseDate(LocalDate.parse(dateStr)); } catch (Exception ignored) {} }
                    eq.setStorageLocation(getString(row, 7));
                    eq.setSchool(school);
                    eq.setCreatedBy(userId);
                    toSave.add(eq);
                }
            }

            if (!toSave.isEmpty()) equipmentRepository.saveAll(toSave);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("imported", toSave.size());
            result.put("errors", errors);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 工具方法 ====================

    private byte[] buildVenueTemplate() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("场馆导入模板");
            CellStyle headerStyle = headerStyle(wb);
            String[] headers = {"场馆名称*", "场馆类型*", "容量(人)*", "位置", "价格(元/时，0为免费)", "开放时间", "设施描述"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            // 说明行
            Row note = sheet.createRow(1);
            note.createCell(0).setCellValue("示例：第一篮球场");
            note.createCell(1).setCellValue("篮球场");
            note.createCell(2).setCellValue(120);
            note.createCell(3).setCellValue("体育馆一楼");
            note.createCell(4).setCellValue("0");
            note.createCell(5).setCellValue("06:00-22:00");
            note.createCell(6).setCellValue("木地板，配备计分板");
            // 类型说明
            Row typeNote = sheet.createRow(3);
            typeNote.createCell(0).setCellValue("场馆类型可选值：篮球场/足球场/网球场/羽毛球场/健身房/游泳池/乒乓球馆");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] buildEquipmentTemplate() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("器材导入模板");
            CellStyle headerStyle = headerStyle(wb);
            String[] headers = {"器材名称*", "分类名称*", "总数量*", "型号", "规格描述", "单价(元)", "购买日期(yyyy-MM-dd)", "存放位置"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            Row note = sheet.createRow(1);
            note.createCell(0).setCellValue("篮球");
            note.createCell(1).setCellValue("球类器材");
            note.createCell(2).setCellValue(20);
            note.createCell(3).setCellValue("SP-7A");
            note.createCell(4).setCellValue("标准比赛用球");
            note.createCell(5).setCellValue("150");
            note.createCell(6).setCellValue("2024-01-01");
            note.createCell(7).setCellValue("器材库A区");
            Row catNote = sheet.createRow(3);
            catNote.createCell(0).setCellValue("分类可选值：球类器材/健身器材/球拍器材/田径器材/游泳器材/体操器材/其他器材");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Integer getInt(Row row, int col) {
        try {
            Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) return null;
            if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
            return Integer.parseInt(cell.getStringCellValue().trim());
        } catch (Exception e) { return null; }
    }

    private String mapVenueType(String input) {
        return switch (input.trim()) {
            case "篮球场" -> "basketball";
            case "足球场" -> "football";
            case "网球场" -> "tennis";
            case "羽毛球场" -> "badminton";
            case "健身房" -> "gym";
            case "游泳池" -> "swimming";
            case "乒乓球馆" -> "pingpong";
            default -> input.trim().toLowerCase();
        };
    }

    private String extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(token.substring(7));
        }
        throw new RuntimeException("未提供有效的认证令牌");
    }
}
