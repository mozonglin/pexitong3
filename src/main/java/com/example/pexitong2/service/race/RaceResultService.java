package com.example.pexitong2.service.race;

import com.example.pexitong2.dto.race.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.race.RaceResult;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.race.RaceResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 比赛成绩业务逻辑服务
 */
@Service
public class RaceResultService {

    @Autowired
    private RaceResultRepository raceResultRepository;

    @Autowired
    private UserRepository userRepository;

    // ============================================================
    //  上位机上传接口
    // ============================================================

    /**
     * 上传一批比赛成绩
     *
     * @param request    上传请求体
     * @param uploaderId 当前登录用户 ID（从 JWT 中提取）
     * @return 上传结果统计
     */
    @Transactional
    public RaceResultUploadResponse uploadResults(RaceResultUploadRequest request, String uploaderId) {

        // 校验请求体基本字段
        if (request.getUploadedAt() == null) {
            throw new IllegalArgumentException("uploadedAt 不能为空");
        }
        if (request.getResults() == null || request.getResults().isEmpty()) {
            throw new IllegalArgumentException("results 不能为空");
        }

        int uploadedCount = 0;
        List<RaceResultUploadResponse.FailedItem> failedItems = new ArrayList<>();

        for (RaceResultItemDto item : request.getResults()) {
            try {
                validateItem(item);

                RaceResult entity = new RaceResult();
                entity.setStudentNumber(StringUtils.hasText(item.getStudentNumber()) ? item.getStudentNumber().trim() : null);
                entity.setName(item.getName().trim());
                entity.setSchool(item.getSchool() != null ? item.getSchool().trim() : "");
                entity.setGender(item.getGender().trim());
                entity.setTotalLaps(item.getTotalLaps());
                entity.setFinalTime(item.getFinalTime());
                entity.setFinalTimeMs(item.getFinalTimeMs());
                entity.setTeacherName(item.getTeacherName() != null ? item.getTeacherName().trim() : "");
                entity.setUploaderId(uploaderId);
                entity.setUploadedAt(LocalDateTime.ofInstant(request.getUploadedAt(), ZoneOffset.UTC));

                raceResultRepository.save(entity);
                uploadedCount++;

            } catch (Exception e) {
                String sn = (item.getStudentNumber() != null) ? item.getStudentNumber() : "";
                failedItems.add(new RaceResultUploadResponse.FailedItem(sn, e.getMessage()));
            }
        }

        return new RaceResultUploadResponse(uploadedCount, failedItems.size(), failedItems);
    }

    private void validateItem(RaceResultItemDto item) {
        if (!StringUtils.hasText(item.getName())) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (!StringUtils.hasText(item.getGender())) {
            throw new IllegalArgumentException("性别不能为空");
        }
        if (!"男".equals(item.getGender()) && !"女".equals(item.getGender())) {
            throw new IllegalArgumentException("性别只能为 男 或 女");
        }
        if (item.getTotalLaps() == null || item.getTotalLaps() < 0) {
            throw new IllegalArgumentException("totalLaps 不合法");
        }
        // finalTime 和 finalTimeMs 允许同时为 null（未完赛）
        if ((item.getFinalTime() == null) != (item.getFinalTimeMs() == null)) {
            throw new IllegalArgumentException("finalTime 与 finalTimeMs 必须同时为 null 或同时有值");
        }
    }

    // ============================================================
    //  管理端查询接口
    // ============================================================

    /**
     * 分页查询成绩列表
     *
     * @param currentUserId 当前登录用户 ID
     * @param school        学校过滤（为空时由权限决定）
     * @param gender        性别过滤
     * @param teacherName   教师姓名过滤
     * @param keyword       关键词（学号或姓名）
     * @param startDate     上传时间起
     * @param endDate       上传时间止
     * @param page          页码（从 1 开始）
     * @param pageSize      每页条数
     */
    public RaceResultPageResponse queryResults(
            String currentUserId,
            String school,
            String gender,
            String teacherName,
            String keyword,
            String startDate,
            String endDate,
            int page,
            int pageSize) {

        String scopeSchool = resolveSchoolScope(currentUserId, school);

        LocalDateTime start = parseDateTime(startDate, true);
        LocalDateTime end   = parseDateTime(endDate, false);

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(pageSize, 1),
                Sort.by(Sort.Direction.DESC, "uploadedAt"));

        Page<RaceResult> resultPage = raceResultRepository.findWithFilters(
                scopeSchool,
                nullIfBlank(gender),
                nullIfBlank(teacherName),
                nullIfBlank(keyword),
                start,
                end,
                pageable);

        List<RaceResultResponse> list = resultPage.getContent()
                .stream()
                .map(RaceResultResponse::from)
                .collect(Collectors.toList());

        return new RaceResultPageResponse(list, resultPage.getTotalElements(), page, pageSize);
    }

    /**
     * 查询单条成绩详情
     */
    public RaceResultResponse getById(Long id, String currentUserId) {
        RaceResult result = raceResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));

        String scopeSchool = resolveSchoolScope(currentUserId, null);
        if (scopeSchool != null && !scopeSchool.equals(result.getSchool())) {
            throw new RuntimeException("无权查看该条成绩");
        }
        return RaceResultResponse.from(result);
    }

    /**
     * 删除单条成绩（仅管理员可操作）
     */
    @Transactional
    public void deleteById(Long id, String currentUserId) {
        ensureAdminPermission(currentUserId);

        RaceResult result = raceResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));

        String scopeSchool = resolveSchoolScope(currentUserId, null);
        if (scopeSchool != null && !scopeSchool.equals(result.getSchool())) {
            throw new RuntimeException("无权删除该条成绩");
        }

        raceResultRepository.deleteById(id);
    }

    /**
     * 批量删除成绩（仅管理员可操作）
     */
    @Transactional
    public int deleteBatch(List<Long> ids, String currentUserId) {
        ensureAdminPermission(currentUserId);

        String scopeSchool = resolveSchoolScope(currentUserId, null);
        int deleted = 0;
        for (Long id : ids) {
            RaceResult result = raceResultRepository.findById(id).orElse(null);
            if (result == null) continue;
            if (scopeSchool != null && !scopeSchool.equals(result.getSchool())) continue;
            raceResultRepository.deleteById(id);
            deleted++;
        }
        return deleted;
    }

    /**
     * 获取成绩统计数据
     */
    public RaceResultStatisticsResponse getStatistics(String currentUserId) {
        String scopeSchool = resolveSchoolScope(currentUserId, null);

        long total, finished, unfinished;
        if (scopeSchool != null) {
            total      = raceResultRepository.countBySchool(scopeSchool);
            finished   = raceResultRepository.countBySchoolAndFinalTimeMsIsNotNull(scopeSchool);
            unfinished = raceResultRepository.countBySchoolAndFinalTimeMsIsNull(scopeSchool);
        } else {
            total      = raceResultRepository.countAll();
            finished   = raceResultRepository.countByFinalTimeMsIsNotNull();
            unfinished = raceResultRepository.countByFinalTimeMsIsNull();
        }

        return new RaceResultStatisticsResponse(total, finished, unfinished, scopeSchool);
    }

    // ============================================================
    //  权限辅助方法
    // ============================================================

    /**
     * 解析当前用户可访问的学校范围
     * - super_admin：返回 null（不限制）
     * - school_admin / department_admin / teacher：返回本人学校
     * 若传入了明确的 school 参数，且当前用户有权限（或是 super_admin），则以传入值为准。
     */
    private String resolveSchoolScope(String userId, String requestedSchool) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getUserType() == User.UserType.super_admin) {
            return nullIfBlank(requestedSchool);
        }

        // 非超管：只能看本校，忽略传入的 school 参数
        return user.getSchool();
    }

    /**
     * 确保当前用户是管理员（department_admin / school_admin / super_admin）
     */
    private void ensureAdminPermission(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        boolean isAdmin = user.getUserType() == User.UserType.department_admin
                || user.getUserType() == User.UserType.school_admin
                || user.getUserType() == User.UserType.super_admin;

        if (!isAdmin) {
            throw new RuntimeException("无权执行此操作，需要管理员权限");
        }
    }

    // ============================================================
    //  工具方法
    // ============================================================

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LocalDateTime parseDateTime(String date, boolean isStart) {
        if (!StringUtils.hasText(date)) return null;
        try {
            if (isStart) {
                return LocalDateTime.parse(date + "T00:00:00");
            } else {
                return LocalDateTime.parse(date + "T23:59:59");
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String nullIfBlank(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}
