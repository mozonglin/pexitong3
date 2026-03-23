package com.example.pexitong2.service.race;

import com.example.pexitong2.entity.race.RaceExcelFile;
import com.example.pexitong2.repository.race.RaceExcelFileRepository;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class RaceExcelFileService {

    @Value("${race.excel.upload-path}")
    private String uploadPath;

    @Autowired
    private RaceExcelFileRepository repository;

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

        RaceExcelFile entity = new RaceExcelFile();
        entity.setOriginalFilename(originalFilename);
        entity.setSavedFilename(savedFilename);
        entity.setSchool(school.trim());
        entity.setTeacherName(teacherName != null ? teacherName.trim() : "");
        entity.setUploaderId(uploaderId);
        entity.setUploadedAt(uploadTime);
        entity.setFileSize(file.getSize());
        return repository.save(entity);
    }

    // ── 列表查询 ─────────────────────────────────────────────────────────

    public Page<RaceExcelFile> list(String school, String teacherName, int page, int pageSize) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(pageSize, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findWithFilters(nullIfBlank(school), nullIfBlank(teacherName), pageable);
    }

    // ── 下载 ─────────────────────────────────────────────────────────────

    public RaceExcelFile getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
    }

    public Resource getFileResource(Long id) {
        RaceExcelFile meta = getById(id);
        Path path = Paths.get(uploadPath, meta.getSavedFilename());
        Resource resource = new FileSystemResource(path.toFile());
        if (!resource.exists()) throw new RuntimeException("文件已被删除");
        return resource;
    }

    // ── 删除 ─────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        RaceExcelFile meta = getById(id);
        try {
            Files.deleteIfExists(Paths.get(uploadPath, meta.getSavedFilename()));
        } catch (IOException ignored) {}
        repository.deleteById(id);
    }

    // ── 工具 ─────────────────────────────────────────────────────────────

    private String nullIfBlank(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}
