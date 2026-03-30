package com.example.pexitong2.controller.race;

import com.example.pexitong2.entity.race.RaceExcelFile;
import com.example.pexitong2.service.race.RaceExcelFileService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

/**
 * 比赛成绩 Excel 文件管理接口
 *
 *   POST   /race/results/upload            - 上传文件（上位机 & 管理端共用）
 *   GET    /race/excel/files               - 分页列表
 *   GET    /race/excel/files/{id}/download - 下载文件
 *   DELETE /race/excel/files/{id}          - 删除文件
 */
@RestController
@CrossOrigin(origins = "*")
public class RaceExcelFileController {

    @Autowired
    private RaceExcelFileService excelFileService;

    @Autowired
    private JwtUtil jwtUtil;

    // ── 上传 ─────────────────────────────────────────────────────────────

    @PostMapping("/race/results/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file")                                    MultipartFile file,
            @RequestParam("school")                                  String school,
            @RequestParam(value = "teacherName", defaultValue = "") String teacherName,
            @RequestParam("uploadedAt")                              String uploadedAt,
            @RequestHeader("Authorization")                          String token) {

        try {
            if (file == null || file.isEmpty())
                return bad("文件不能为空");
            String fname = file.getOriginalFilename();
            if (fname == null || !fname.toLowerCase().endsWith(".xlsx"))
                return bad("文件格式错误，仅支持 .xlsx");
            if (!StringUtils.hasText(school))
                return bad("缺少必填字段 school");

            String uploaderId = extractUserId(token);
            RaceExcelFile record = excelFileService.upload(file, school, teacherName, uploadedAt, uploaderId);

            Map<String, Object> uploadData = new HashMap<>();
            uploadData.put("id",       record.getId());
            uploadData.put("fileName", record.getOriginalFilename());
            uploadData.put("fileSize", record.getFileSize() != null ? record.getFileSize() : 0L);
            uploadData.put("filePath", record.getFilePath() != null ? record.getFilePath() : "");

            Map<String, Object> uploadBody = new HashMap<>();
            uploadBody.put("code",    200);
            uploadBody.put("message", "上传成功");
            uploadBody.put("data",    uploadData);
            return ResponseEntity.ok(uploadBody);
        } catch (IllegalArgumentException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500, "message", "服务器内部错误", "error", "INTERNAL_SERVER_ERROR"));
        }
    }

    // ── 列表 ─────────────────────────────────────────────────────────────

    @GetMapping("/race/excel/files")
    public ResponseEntity<Map<String, Object>> getList(
            @RequestParam(required = false)        String school,
            @RequestParam(required = false)        String teacherName,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader("Authorization")        String token) {

        try {
            String userId = extractUserId(token);
            Page<RaceExcelFile> result = excelFileService.list(userId, school, teacherName, page, pageSize);

            List<Map<String, Object>> list = result.getContent().stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("list",       list);
            data.put("total",      result.getTotalElements());
            data.put("totalPages", result.getTotalPages());
            data.put("page",       page);

            Map<String, Object> body = new HashMap<>();
            body.put("code",    200);
            body.put("message", "获取成功");
            body.put("data",    data);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500, "message", e.getMessage() != null ? e.getMessage() : "服务器内部错误"));
        }
    }

    // ── 下载 ─────────────────────────────────────────────────────────────

    @GetMapping("/race/excel/files/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            String userId          = extractUserId(token);
            RaceExcelFile record   = excelFileService.getByIdScoped(id, userId);
            Resource      resource = excelFileService.getFileResource(id, userId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(record.getOriginalFilename(), StandardCharsets.UTF_8)
                                    .build().toString())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── 预览 Excel 内容 ───────────────────────────────────────────────────

    @GetMapping("/race/excel/files/{id}/preview")
    public ResponseEntity<Map<String, Object>> previewFile(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            String userId = extractUserId(token);
            Map<String, Object> data = excelFileService.previewExcel(id, userId);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("code",    200);
            body.put("message", "获取成功");
            body.put("data",    data);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500, "message", e.getMessage() != null ? e.getMessage() : "服务器内部错误"));
        }
    }

    // ── 解析 Excel 入库 ───────────────────────────────────────────────────

    @PostMapping("/race/excel/files/{id}/parse")
    public ResponseEntity<Map<String, Object>> parseToDb(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String token) {

        try {
            String uploaderId = extractUserId(token);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) body.get("rows");
            if (rows == null || rows.isEmpty())
                return bad("rows 不能为空");

            Map<String, Object> result = excelFileService.parseToDb(id, rows, uploaderId);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("code",    200);
            resp.put("message", "解析完成");
            resp.put("data",    result);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500, "message", e.getMessage() != null ? e.getMessage() : "服务器内部错误"));
        }
    }

    // ── 删除 ─────────────────────────────────────────────────────────────

    @DeleteMapping("/race/excel/files/{id}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            String userId = extractUserId(token);
            excelFileService.delete(id, userId);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500, "message", e.getMessage() != null ? e.getMessage() : "服务器内部错误"));
        }
    }

    // ── 工具 ─────────────────────────────────────────────────────────────

    private String extractUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
        return jwtUtil.extractUserId(token);
    }

    private ResponseEntity<Map<String, Object>> bad(String msg) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", 400, "message", msg, "error", "VALIDATION_ERROR"));
    }

    private Map<String, Object> toMap(RaceExcelFile f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               f.getId());
        m.put("originalFilename", f.getOriginalFilename() != null ? f.getOriginalFilename() : "");
        m.put("school",           f.getSchool()      != null ? f.getSchool()      : "");
        m.put("teacherName",      f.getTeacherName() != null ? f.getTeacherName() : "");
        m.put("uploadedAt",       f.getUploadedAt()  != null ? f.getUploadedAt().toString()  : "");
        m.put("fileSize",         f.getFileSize()    != null ? f.getFileSize()    : 0L);
        m.put("createdAt",        f.getCreatedAt()   != null ? f.getCreatedAt().toString()   : "");
        m.put("parsedAt",         f.getParsedAt()    != null ? f.getParsedAt().toString()    : null);
        m.put("parsed",           f.getParsedAt()    != null);
        return m;
    }
}
