package com.example.pexitong2.controller.race;

import com.example.pexitong2.dto.race.*;
import com.example.pexitong2.dto.pe.PeApiResponse;
import com.example.pexitong2.service.race.RaceResultService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 比赛成绩控制器
 *
 * 上位机接口：
 *   POST   /race/results                  - 上传成绩
 *
 * 管理端接口（teacher / department_admin / school_admin / super_admin）：
 *   GET    /race/results                  - 分页查询成绩列表
 *   GET    /race/results/statistics       - 统计数据
 *   GET    /race/results/{id}             - 查询单条成绩详情
 *   DELETE /race/results/{id}             - 删除单条成绩（仅管理员）
 *   DELETE /race/results/batch            - 批量删除（仅管理员）
 */
@RestController
@RequestMapping("/race/results")
@CrossOrigin(origins = "*")
public class RaceResultController {

    @Autowired
    private RaceResultService raceResultService;

    @Autowired
    private JwtUtil jwtUtil;

    // ============================================================
    //  上位机上传接口
    // ============================================================

    /**
     * POST /race/results
     * 上传比赛成绩（上位机在比赛结束后自动调用）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadResults(
            @RequestBody RaceResultUploadRequest request,
            @RequestHeader("Authorization") String token) {

        try {
            String uploaderId = extractUserId(token);
            RaceResultUploadResponse data = raceResultService.uploadResults(request, uploaderId);

            return ResponseEntity.ok(Map.of(
                    "code",    200,
                    "message", "成绩上传成功",
                    "data",    data
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code",    400,
                    "message", "请求参数错误",
                    "error",   "VALIDATION_ERROR",
                    "details", List.of(Map.of("field", "results", "message", e.getMessage()))
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "code",    500,
                    "message", "服务器内部错误",
                    "error",   "INTERNAL_SERVER_ERROR"
            ));
        }
    }

    // ============================================================
    //  管理端接口
    // ============================================================

    /**
     * GET /race/results/statistics
     * 查询成绩统计数据（必须在 /{id} 路由之前声明，防止路径冲突）
     */
    @GetMapping("/statistics")
    public PeApiResponse<RaceResultStatisticsResponse> getStatistics(
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            return PeApiResponse.success("获取成功", raceResultService.getStatistics(userId));
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    /**
     * GET /race/results
     * 分页查询成绩列表
     *
     * @param school      学校过滤（非超管时自动限定本校，传入值仅超管生效）
     * @param gender      性别：男 / 女
     * @param teacherName 教师姓名（模糊）
     * @param keyword     关键词（学号或姓名，模糊）
     * @param startDate   上传日期起，格式 yyyy-MM-dd
     * @param endDate     上传日期止，格式 yyyy-MM-dd
     * @param page        页码，从 1 开始，默认 1
     * @param pageSize    每页条数，默认 20
     */
    @GetMapping
    public PeApiResponse<RaceResultPageResponse> queryResults(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            RaceResultPageResponse data = raceResultService.queryResults(
                    userId, school, gender, teacherName, keyword,
                    startDate, endDate, page, pageSize);
            return PeApiResponse.success("获取成功", data);
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    /**
     * GET /race/results/{id}
     * 查询单条成绩详情
     */
    @GetMapping("/{id}")
    public PeApiResponse<RaceResultResponse> getById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            return PeApiResponse.success("获取成功", raceResultService.getById(id, userId));
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    /**
     * DELETE /race/results/{id}
     * 删除单条成绩（仅 department_admin / school_admin / super_admin）
     */
    @DeleteMapping("/{id}")
    public PeApiResponse<Void> deleteById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            raceResultService.deleteById(id, userId);
            return PeApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    /**
     * DELETE /race/results/batch
     * 批量删除成绩（仅 department_admin / school_admin / super_admin）
     *
     * 请求体：{ "ids": [1, 2, 3] }
     */
    @DeleteMapping("/batch")
    public PeApiResponse<Map<String, Object>> deleteBatch(
            @RequestBody Map<String, List<Long>> body,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserId(token);
            List<Long> ids = body.get("ids");
            if (ids == null || ids.isEmpty()) {
                return PeApiResponse.error(400, "ids 不能为空");
            }
            int deleted = raceResultService.deleteBatch(ids, userId);
            return PeApiResponse.success("批量删除完成", Map.of("deletedCount", deleted));
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }

    // ============================================================
    //  工具方法
    // ============================================================

    private String extractUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.extractUserId(token);
    }
}
