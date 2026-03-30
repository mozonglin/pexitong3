package com.example.pexitong2.service.race;

import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.race.RaceExcelFile;
import com.example.pexitong2.entity.race.RaceResult;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.race.RaceExcelFileRepository;
import com.example.pexitong2.repository.race.RaceResultRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RaceExcelFileService {

    @Value("${race.excel.upload-path}")
    private String uploadPath;

    @Autowired
    private RaceExcelFileRepository repository;

    @Autowired
    private RaceResultRepository raceResultRepository;

    @Autowired
    private UserRepository userRepository;

    // ── 上传并保存 ────────────────────────────────────────────────────────

    @Transactional
    public RaceExcelFile upload(MultipartFile file, String school, String teacherName,
                                String uploadedAt, String uploaderId) throws IOException {
        Path dir = Paths.get(uploadPath);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "result.xlsx";
        String savedFilename = System.currentTimeMillis() + "_" + originalFilename;
        Files.copy(file.getInputStream(), dir.resolve(savedFilename), StandardCopyOption.REPLACE_EXISTING);

        LocalDateTime uploadTime;
        try {
            uploadTime = LocalDateTime.ofInstant(Instant.parse(uploadedAt), ZoneOffset.UTC);
        } catch (Exception e) {
            uploadTime = LocalDateTime.now();
        }

        String fullPath = dir.resolve(savedFilename).toString();

        RaceExcelFile entity = new RaceExcelFile();
        entity.setOriginalFilename(originalFilename);
        entity.setSavedFilename(savedFilename);
        entity.setFilePath(fullPath);
        entity.setSchool(school.trim());
        entity.setTeacherName(teacherName != null ? teacherName.trim() : "");
        entity.setUploaderId(uploaderId);
        entity.setUploadedAt(uploadTime);
        entity.setFileSize(file.getSize());
        return repository.save(entity);
    }

    // ── 列表查询 ─────────────────────────────────────────────────────────

    public Page<RaceExcelFile> list(String currentUserId, String requestedSchool, String teacherName, int page, int pageSize) {
        String scopeSchool = resolveSchoolScope(currentUserId, requestedSchool);
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(pageSize, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findWithFilters(nullIfBlank(scopeSchool), nullIfBlank(teacherName), pageable);
    }

    // ── 按 ID 查询（含学校权限检查）────────────────────────────────────────

    public RaceExcelFile getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
    }

    public RaceExcelFile getByIdScoped(Long id, String currentUserId) {
        RaceExcelFile file = getById(id);
        String scopeSchool = resolveSchoolScope(currentUserId, null);
        if (scopeSchool != null && !scopeSchool.equals(file.getSchool())) {
            throw new RuntimeException("无权访问该文件");
        }
        return file;
    }

    public Resource getFileResource(Long id, String currentUserId) {
        RaceExcelFile meta = getByIdScoped(id, currentUserId);
        Path path = Paths.get(uploadPath, meta.getSavedFilename());
        Resource resource = new FileSystemResource(path.toFile());
        if (!resource.exists()) throw new RuntimeException("文件已被删除");
        return resource;
    }

    // ── 删除 ─────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id, String currentUserId) {
        RaceExcelFile meta = getByIdScoped(id, currentUserId);
        try {
            Files.deleteIfExists(Paths.get(uploadPath, meta.getSavedFilename()));
        } catch (IOException ignored) {}
        repository.deleteById(id);
    }

    // ── 预览 Excel 内容（不写库）─────────────────────────────────────────

    public Map<String, Object> previewExcel(Long id, String currentUserId) throws IOException {
        RaceExcelFile meta = getByIdScoped(id, currentUserId);
        Path filePath = Paths.get(uploadPath, meta.getSavedFilename());
        if (!Files.exists(filePath)) throw new RuntimeException("文件已被删除");

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            // 元信息（第1-3行，0-indexed 0-2）
            Map<String, String> metaInfo = new LinkedHashMap<>();
            metaInfo.put("school",      getCellStr(sheet, 0, 1));
            metaInfo.put("teacherName", getCellStr(sheet, 1, 1));
            metaInfo.put("uploadedAt",  getCellStr(sheet, 2, 1));

            // 数据行（第6行起，0-indexed 5+）
            List<Map<String, Object>> rows = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();
            for (int r = 5; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String name = getCellStr(row, 0);
                if (name.isEmpty()) continue;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name",          name);
                item.put("studentNumber", getCellStr(row, 1));
                item.put("gender",        getCellStr(row, 2));
                String lapsStr = getCellStr(row, 3);
                item.put("totalLaps",     lapsStr.isEmpty() ? 0 : parseIntSafe(lapsStr));
                item.put("finalTime",     getCellStr(row, 4));
                rows.add(item);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("meta", metaInfo);
            result.put("rows", rows);
            return result;
        }
    }

    // ── 解析 Excel 行数据并写入 race_results 表 ──────────────────────────

    @Transactional
    public Map<String, Object> parseToDb(Long id, List<Map<String, Object>> rows, String uploaderId) {
        RaceExcelFile meta = getByIdScoped(id, uploaderId);
        if (meta.getParsedAt() != null) {
            throw new IllegalStateException("该文件已于 " + meta.getParsedAt().toString().replace("T", " ").substring(0, 19) + " 解析入库，不可重复解析");
        }

        int uploaded = 0;
        List<Map<String, String>> failedItems = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            try {
                String name = strVal(row, "name");
                if (name.isEmpty()) throw new IllegalArgumentException("姓名不能为空");

                String gender = strVal(row, "gender");
                if (!"男".equals(gender) && !"女".equals(gender))
                    throw new IllegalArgumentException("性别只能为 男 或 女");

                Object lapsObj = row.get("totalLaps");
                int totalLaps = lapsObj == null ? 0 : (lapsObj instanceof Number ? ((Number) lapsObj).intValue() : parseIntSafe(lapsObj.toString()));

                String finalTimeStr = strVal(row, "finalTime");
                boolean finished = !finalTimeStr.isEmpty() && !"--".equals(finalTimeStr);

                RaceResult entity = new RaceResult();
                String sn = strVal(row, "studentNumber");
                entity.setStudentNumber(sn.isEmpty() || "--".equals(sn) ? null : sn);
                entity.setName(name);
                entity.setSchool(meta.getSchool());
                entity.setGender(gender);
                entity.setTotalLaps(totalLaps);
                entity.setFinalTime(finished ? finalTimeStr : null);
                entity.setFinalTimeMs(finished ? parseTimeToMs(finalTimeStr) : null);
                entity.setTeacherName(meta.getTeacherName() != null ? meta.getTeacherName() : "");
                entity.setUploaderId(uploaderId);
                entity.setUploadedAt(meta.getUploadedAt());

                raceResultRepository.save(entity);
                uploaded++;
            } catch (Exception e) {
                Map<String, String> fail = new LinkedHashMap<>();
                fail.put("name",   strVal(row, "name"));
                fail.put("reason", e.getMessage());
                failedItems.add(fail);
            }
        }

        // 标记已解析（即使有部分失败，也记录解析时间，防止重复提交）
        meta.setParsedAt(LocalDateTime.now());
        repository.save(meta);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uploadedCount", uploaded);
        result.put("failedCount",   failedItems.size());
        result.put("failedItems",   failedItems);
        return result;
    }

    // ── 工具 ─────────────────────────────────────────────────────────────

    private String nullIfBlank(String s) {
        return StringUtils.hasText(s) ? s : null;
    }

    private String getCellStr(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) return "";
        return getCellStr(row, colIdx);
    }

    private String getCellStr(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) yield String.valueOf((long) d);
                yield String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception ex) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    private String strVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    // ── 权限辅助 ──────────────────────────────────────────────────────────

    /**
     * 解析当前用户可访问的学校范围：
     * - super_admin → null（不限制，可进一步用 requestedSchool 过滤）
     * - 其他角色   → 仅本校
     */
    private String resolveSchoolScope(String userId, String requestedSchool) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getUserType() == User.UserType.super_admin) {
            return nullIfBlank(requestedSchool);
        }
        return user.getSchool();
    }

    /** 将 mm:ss.ff 或 mm:ss 格式转为毫秒 */
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+):(\\d+)(?:\\.(\\d+))?");

    private Long parseTimeToMs(String timeStr) {
        if (timeStr == null || timeStr.isEmpty() || "--".equals(timeStr)) return null;
        Matcher m = TIME_PATTERN.matcher(timeStr);
        if (!m.matches()) return null;
        long minutes = Long.parseLong(m.group(1));
        long seconds = Long.parseLong(m.group(2));
        long centis  = m.group(3) != null ? Long.parseLong(m.group(3)) : 0L;
        return minutes * 60_000L + seconds * 1_000L + centis * 10L;
    }
}
