package com.example.pexitong2.controller.training;

import com.example.pexitong2.dto.training.TrainingDTO.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.training.TrainingTaskRecord;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.service.training.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private UserRepository userRepository;

    // ==================== 体能档案 API ====================

    /** 获取当前用户体能档案 */
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(success(trainingService.getOrCreateProfile(userId)));
    }

    /** 获取指定学生体能档案（教师/管理员） */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getStudentProfile(@PathVariable String userId, Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.getOrCreateProfile(userId)));
    }

    /** 学生上传体测数据 */
    @PostMapping("/profile/tice-data")
    public ResponseEntity<?> uploadTiceData(@RequestBody TiceDataRequest request, Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(success(trainingService.uploadTiceData(userId, request)));
    }

    /** 教师查看班级学生档案列表 */
    @GetMapping("/profiles/class")
    public ResponseEntity<?> getClassProfiles(
            @RequestParam String className,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkTeacherOrAdmin(auth);
        Page<FitnessProfileResponse> profiles = trainingService.getClassProfiles(className, page, size);
        return ResponseEntity.ok(successPage(profiles));
    }

    /** 教师查看多个班级学生档案 */
    @GetMapping("/profiles/classes")
    public ResponseEntity<?> getClassesProfiles(
            @RequestParam List<String> classNames,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkTeacherOrAdmin(auth);
        Page<FitnessProfileResponse> profiles = trainingService.getClassesProfiles(classNames, page, size);
        return ResponseEntity.ok(successPage(profiles));
    }

    /** 搜索学生档案 */
    @GetMapping("/profiles/search")
    public ResponseEntity<?> searchProfiles(
            @RequestParam String keyword,
            @RequestParam(required = false) String className,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(successPage(trainingService.searchProfiles(keyword, className, page, size)));
    }

    /** 获取班级体能概览 */
    @GetMapping("/profiles/class-overview")
    public ResponseEntity<?> getClassOverview(@RequestParam String className, Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.getClassOverview(className)));
    }

    /** 手动触发AI分析 */
    @PostMapping("/profile/{userId}/analyze")
    public ResponseEntity<?> triggerAiAnalysis(@PathVariable String userId, Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.triggerAiAnalysis(userId)));
    }

    /** 获取可用班级列表 */
    @GetMapping("/classes")
    public ResponseEntity<?> getAvailableClasses(Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.getAvailableClasses()));
    }

    // ==================== 训练任务 API ====================

    /** 教师创建训练任务 */
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request, Authentication auth) {
        String teacherId = auth.getName();
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.createTask(teacherId, request)));
    }

    /** 教师获取自己的任务列表 */
    @GetMapping("/tasks")
    public ResponseEntity<?> getMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        String teacherId = auth.getName();
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(successPage(trainingService.getTeacherTasks(teacherId, status, page, size)));
    }

    /** 获取任务详情 */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskDetail(@PathVariable Long taskId, Authentication auth) {
        return ResponseEntity.ok(success(trainingService.getTaskDetail(taskId)));
    }

    /** 获取任务的学生完成记录 */
    @GetMapping("/tasks/{taskId}/records")
    public ResponseEntity<?> getTaskRecords(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(successPage(trainingService.getTaskRecords(taskId, page, size)));
    }

    /** 学生获取自己的待完成任务 */
    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyActiveTasks(Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(success(trainingService.getStudentActiveTasks(userId)));
    }

    /** 更新任务状态 */
    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.updateTaskStatus(taskId, body.get("status"))));
    }

    /** 删除任务 */
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId, Authentication auth) {
        checkTeacherOrAdmin(auth);
        trainingService.deleteTask(taskId);
        return ResponseEntity.ok(success("删除成功"));
    }

    /** 获取AI任务建议 */
    @PostMapping("/tasks/ai-suggestion")
    public ResponseEntity<?> getAiTaskSuggestion(@RequestBody AiTaskSuggestionRequest request, Authentication auth) {
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.getAiTaskSuggestion(request)));
    }

    /** 教师获取统计数据 */
    @GetMapping("/stats")
    public ResponseEntity<?> getTeacherStats(Authentication auth) {
        String teacherId = auth.getName();
        checkTeacherOrAdmin(auth);
        return ResponseEntity.ok(success(trainingService.getTeacherStats(teacherId)));
    }

    // ==================== 辅助方法 ====================

    private void checkTeacherOrAdmin(Authentication auth) {
        String userId = auth.getName();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        User.UserType type = user.getUserType();
        if (type != User.UserType.teacher && type != User.UserType.department_admin
                && type != User.UserType.school_admin && type != User.UserType.super_admin) {
            throw new RuntimeException("权限不足：需要教师或管理员权限");
        }
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }

    private Map<String, Object> successPage(Page<?> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        Map<String, Object> data = new HashMap<>();
        data.put("content", page.getContent());
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages", page.getTotalPages());
        data.put("currentPage", page.getNumber());
        data.put("pageSize", page.getSize());
        result.put("data", data);
        return result;
    }
}
