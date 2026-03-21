package com.example.pexitong2.service.training;

import com.example.pexitong2.dto.training.TrainingDTO.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.training.*;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.training.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainingService {

    @Autowired
    private TrainingTaskRepository taskRepository;

    @Autowired
    private TrainingTaskRecordRepository recordRepository;

    @Autowired
    private StudentFitnessProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FitnessAIAnalyzer aiAnalyzer;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 体能档案 ====================

    /** 获取学生体能档案（自动创建） */
    public FitnessProfileResponse getOrCreateProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        StudentFitnessProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    StudentFitnessProfile p = new StudentFitnessProfile();
                    p.setUserId(user.getId());
                    p.setStudentId(user.getStudentId());
                    p.setRealName(user.getRealName());
                    p.setClassName(user.getDepartmentName()); // 用department_name作为班级
                    p.setDepartmentName(user.getDepartmentName());
                    return profileRepository.save(p);
                });

        return toProfileResponse(profile);
    }

    /** 上传体测数据 */
    @Transactional
    public FitnessProfileResponse uploadTiceData(String userId, TiceDataRequest request) {
        StudentFitnessProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("请先建立体能档案"));

        if (request.getVitalCapacity() != null) profile.setVitalCapacity(request.getVitalCapacity());
        if (request.getSitAndReach() != null) profile.setSitAndReach(request.getSitAndReach());
        if (request.getStandingLongJump() != null) profile.setStandingLongJump(request.getStandingLongJump());
        if (request.getSitUps() != null) profile.setSitUps(request.getSitUps());
        if (request.getSprint50m() != null) profile.setSprint50m(request.getSprint50m());
        if (request.getLongRun() != null) profile.setLongRun(request.getLongRun());
        if (request.getPullUps() != null) profile.setPullUps(request.getPullUps());
        if (request.getBmi() != null) profile.setBmi(request.getBmi());
        if (request.getHeight() != null) profile.setHeight(request.getHeight());
        if (request.getWeight() != null) profile.setWeight(request.getWeight());
        if (request.getTiceTotalScore() != null) profile.setTiceTotalScore(request.getTiceTotalScore());
        if (request.getTiceGrade() != null) profile.setTiceGrade(request.getTiceGrade());
        profile.setTiceUpdatedAt(LocalDateTime.now());

        // 自动触发AI分析
        profile = aiAnalyzer.analyzeAndUpdate(profile);
        return toProfileResponse(profile);
    }

    /** 同步阳光跑数据到档案（由系统定时或手动触发） */
    @Transactional
    public void syncRunDataToProfile(String userId, int totalCount, double totalDistance,
                                      Double avgPace, Double bestPace) {
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setRunTotalCount(totalCount);
            profile.setRunTotalDistance(totalDistance);
            profile.setRunAvgPace(avgPace);
            profile.setRunBestPace(bestPace);
            profile.setRunUpdatedAt(LocalDateTime.now());
            aiAnalyzer.analyzeAndUpdate(profile);
        });
    }

    /** 同步课后作业数据到档案 */
    @Transactional
    public void syncHomeworkDataToProfile(String userId, int totalCount,
                                           Integer squatBest, Integer situpBest, Integer pushupBest,
                                           Integer pullupBest, Integer jumpropeBest) {
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setHomeworkTotalCount(totalCount);
            if (squatBest != null) profile.setHomeworkSquatBest(squatBest);
            if (situpBest != null) profile.setHomeworkSitupBest(situpBest);
            if (pushupBest != null) profile.setHomeworkPushupBest(pushupBest);
            if (pullupBest != null) profile.setHomeworkPullupBest(pullupBest);
            if (jumpropeBest != null) profile.setHomeworkJumpropeBest(jumpropeBest);
            profile.setHomeworkUpdatedAt(LocalDateTime.now());
            aiAnalyzer.analyzeAndUpdate(profile);
        });
    }

    /** 同步PE积分数据到档案 */
    @Transactional
    public void syncPointsDataToProfile(String userId, int totalPoints,
                                         int activityPoints, int morningPoints) {
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setPeTotalPoints(totalPoints);
            profile.setPeActivityPoints(activityPoints);
            profile.setPeMorningPoints(morningPoints);
            profile.setPePointsUpdatedAt(LocalDateTime.now());
            aiAnalyzer.analyzeAndUpdate(profile);
        });
    }

    /** 教师查看班级学生档案列表 */
    public Page<FitnessProfileResponse> getClassProfiles(String className, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentId").ascending());
        return profileRepository.findByClassName(className, pageable)
                .map(this::toProfileResponse);
    }

    /** 教师查看多个班级学生档案 */
    public Page<FitnessProfileResponse> getClassesProfiles(List<String> classNames, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentId").ascending());
        return profileRepository.findByClassNameIn(classNames, pageable)
                .map(this::toProfileResponse);
    }

    /** 搜索学生档案 */
    public Page<FitnessProfileResponse> searchProfiles(String keyword, String className, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return profileRepository.searchProfiles(keyword, className, pageable)
                .map(this::toProfileResponse);
    }

    /** 获取班级体能概览 */
    public ClassFitnessOverview getClassOverview(String className) {
        List<StudentFitnessProfile> profiles = profileRepository.findByClassName(className);
        ClassFitnessOverview overview = new ClassFitnessOverview();
        overview.setClassName(className);
        overview.setTotalStudents(profiles.size());
        overview.setProfiledStudents((int) profiles.stream()
                .filter(p -> p.getAiFitnessLevel() != null).count());

        // 体能等级分布
        Map<String, Integer> levelDist = new HashMap<>();
        for (StudentFitnessProfile p : profiles) {
            if (p.getAiFitnessLevel() != null) {
                levelDist.merge(p.getAiFitnessLevel(), 1, Integer::sum);
            }
        }
        overview.setFitnessLevelDistribution(levelDist);

        // 各项平均值
        Map<String, Double> avgMetrics = new HashMap<>();
        avgMetrics.put("avgRunCount", profiles.stream()
                .filter(p -> p.getRunTotalCount() != null)
                .mapToInt(StudentFitnessProfile::getRunTotalCount).average().orElse(0));
        avgMetrics.put("avgHomeworkCount", profiles.stream()
                .filter(p -> p.getHomeworkTotalCount() != null)
                .mapToInt(StudentFitnessProfile::getHomeworkTotalCount).average().orElse(0));
        avgMetrics.put("avgPePoints", profiles.stream()
                .filter(p -> p.getPeTotalPoints() != null)
                .mapToInt(StudentFitnessProfile::getPeTotalPoints).average().orElse(0));
        overview.setAvgMetrics(avgMetrics);

        return overview;
    }

    /** 手动触发AI分析（单个学生） */
    @Transactional
    public FitnessProfileResponse triggerAiAnalysis(String userId) {
        StudentFitnessProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("档案不存在"));
        profile = aiAnalyzer.analyzeAndUpdate(profile);
        return toProfileResponse(profile);
    }

    // ==================== 训练任务 ====================

    /** 教师创建训练任务 */
    @Transactional
    public TaskResponse createTask(String teacherId, CreateTaskRequest request) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("教师不存在"));

        TrainingTask task = new TrainingTask();
        task.setTeacherId(teacherId);
        task.setTeacherName(teacher.getRealName());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTrainingType(TrainingTask.TrainingType.valueOf(request.getTrainingType()));
        task.setDifficulty(TrainingTask.Difficulty.valueOf(request.getDifficulty()));
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setRequirements(request.getRequirements());
        task.setCourseId(request.getCourseId());

        // 序列化目标班级
        try {
            task.setTargetClasses(objectMapper.writeValueAsString(request.getTargetClasses()));
        } catch (Exception e) {
            task.setTargetClasses("[]");
        }

        // AI参考建议
        if (request.isUseAiReference()) {
            List<StudentFitnessProfile> classProfiles = new ArrayList<>();
            for (String className : request.getTargetClasses()) {
                classProfiles.addAll(profileRepository.findByClassName(className));
            }
            String aiRef = aiAnalyzer.generateTaskSuggestion(classProfiles, request.getTrainingType());
            task.setAiReference(aiRef);
            task.setAiGenerated(false); // AI参考，非AI生成
        }

        // 统计目标学生数
        int totalStudents = 0;
        for (String className : request.getTargetClasses()) {
            totalStudents += profileRepository.findByClassName(className).size();
        }
        task.setTotalStudents(totalStudents);

        task = taskRepository.save(task);

        // 为目标班级的学生创建任务记录
        createRecordsForTask(task, request.getTargetClasses());

        return toTaskResponse(task);
    }

    /** 为任务创建学生记录 */
    private void createRecordsForTask(TrainingTask task, List<String> targetClasses) {
        for (String className : targetClasses) {
            List<StudentFitnessProfile> students = profileRepository.findByClassName(className);
            for (StudentFitnessProfile student : students) {
                TrainingTaskRecord record = new TrainingTaskRecord();
                record.setTaskId(task.getId());
                record.setUserId(student.getUserId());
                record.setStudentId(student.getStudentId());
                record.setStudentName(student.getRealName());
                record.setClassName(student.getClassName());
                record.setStatus(TrainingTaskRecord.RecordStatus.pending);
                recordRepository.save(record);
            }
        }
    }

    /** 教师获取自己的任务列表 */
    public Page<TaskResponse> getTeacherTasks(String teacherId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TrainingTask> tasks;
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            tasks = taskRepository.findByTeacherIdAndStatus(
                    teacherId, TrainingTask.TaskStatus.valueOf(status), pageable);
        } else {
            tasks = taskRepository.findByTeacherId(teacherId, pageable);
        }
        return tasks.map(this::toTaskResponse);
    }

    /** 获取任务详情 */
    public TaskResponse getTaskDetail(Long taskId) {
        TrainingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        return toTaskResponse(task);
    }

    /** 获取任务的学生完成记录 */
    public Page<TrainingTaskRecord> getTaskRecords(Long taskId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return recordRepository.findByTaskId(taskId, pageable);
    }

    /** 学生获取自己的待完成任务 */
    public List<TaskResponse> getStudentActiveTasks(String userId) {
        List<TrainingTaskRecord> records = recordRepository.findActiveRecordsByUserId(userId);
        return records.stream()
                .map(r -> {
                    TrainingTask task = taskRepository.findById(r.getTaskId()).orElse(null);
                    if (task == null) return null;
                    TaskResponse resp = toTaskResponse(task);
                    // 附加学生个人完成状态
                    resp.setCompletionRate(r.getCompletionRate() != null ?
                            r.getCompletionRate().doubleValue() : 0.0);
                    return resp;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** 更新任务状态 */
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, String status) {
        TrainingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        task.setStatus(TrainingTask.TaskStatus.valueOf(status));
        task = taskRepository.save(task);
        return toTaskResponse(task);
    }

    /** 删除任务 */
    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    /** 获取AI任务建议 */
    public String getAiTaskSuggestion(AiTaskSuggestionRequest request) {
        List<StudentFitnessProfile> classProfiles = new ArrayList<>();
        if (request.getTargetClasses() != null) {
            for (String className : request.getTargetClasses()) {
                classProfiles.addAll(profileRepository.findByClassName(className));
            }
        }
        return aiAnalyzer.generateTaskSuggestion(classProfiles, request.getTrainingType());
    }

    /** 教师获取统计数据 */
    public Map<String, Object> getTeacherStats(String teacherId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", taskRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId).size());
        stats.put("activeTasks", taskRepository.countByTeacherIdAndStatus(teacherId, TrainingTask.TaskStatus.active));
        stats.put("completedTasks", taskRepository.countByTeacherIdAndStatus(teacherId, TrainingTask.TaskStatus.completed));
        return stats;
    }

    /** 获取可用班级列表 */
    public List<String> getAvailableClasses() {
        return profileRepository.findDistinctClassNames();
    }

    // ==================== 转换方法 ====================

    private FitnessProfileResponse toProfileResponse(StudentFitnessProfile p) {
        FitnessProfileResponse resp = new FitnessProfileResponse();
        resp.setId(p.getId());
        resp.setUserId(p.getUserId());
        resp.setStudentId(p.getStudentId());
        resp.setRealName(p.getRealName());
        resp.setClassName(p.getClassName());
        resp.setDepartmentName(p.getDepartmentName());

        // 体测数据
        Map<String, Object> tice = new LinkedHashMap<>();
        tice.put("vitalCapacity", p.getVitalCapacity());
        tice.put("sitAndReach", p.getSitAndReach());
        tice.put("standingLongJump", p.getStandingLongJump());
        tice.put("sitUps", p.getSitUps());
        tice.put("sprint50m", p.getSprint50m());
        tice.put("longRun", p.getLongRun());
        tice.put("pullUps", p.getPullUps());
        tice.put("bmi", p.getBmi());
        tice.put("height", p.getHeight());
        tice.put("weight", p.getWeight());
        tice.put("totalScore", p.getTiceTotalScore());
        tice.put("grade", p.getTiceGrade());
        tice.put("updatedAt", p.getTiceUpdatedAt());
        resp.setTiceData(tice);

        // 阳光跑数据
        Map<String, Object> run = new LinkedHashMap<>();
        run.put("totalCount", p.getRunTotalCount());
        run.put("totalDistance", p.getRunTotalDistance());
        run.put("avgPace", p.getRunAvgPace());
        run.put("bestPace", p.getRunBestPace());
        run.put("updatedAt", p.getRunUpdatedAt());
        resp.setRunData(run);

        // 课后作业数据
        Map<String, Object> hw = new LinkedHashMap<>();
        hw.put("totalCount", p.getHomeworkTotalCount());
        hw.put("squatBest", p.getHomeworkSquatBest());
        hw.put("situpBest", p.getHomeworkSitupBest());
        hw.put("pushupBest", p.getHomeworkPushupBest());
        hw.put("pullupBest", p.getHomeworkPullupBest());
        hw.put("jumpropeBest", p.getHomeworkJumpropeBest());
        hw.put("updatedAt", p.getHomeworkUpdatedAt());
        resp.setHomeworkData(hw);

        // PE积分数据
        Map<String, Object> pts = new LinkedHashMap<>();
        pts.put("totalPoints", p.getPeTotalPoints());
        pts.put("activityPoints", p.getPeActivityPoints());
        pts.put("morningPoints", p.getPeMorningPoints());
        pts.put("updatedAt", p.getPePointsUpdatedAt());
        resp.setPePointsData(pts);

        // AI分析
        resp.setAiFitnessLevel(p.getAiFitnessLevel());
        if (p.getAiStrengths() != null) {
            try {
                resp.setAiStrengths(objectMapper.readValue(p.getAiStrengths(), new TypeReference<>() {}));
            } catch (Exception e) { resp.setAiStrengths(List.of()); }
        }
        if (p.getAiWeaknesses() != null) {
            try {
                resp.setAiWeaknesses(objectMapper.readValue(p.getAiWeaknesses(), new TypeReference<>() {}));
            } catch (Exception e) { resp.setAiWeaknesses(List.of()); }
        }
        resp.setAiRecommendation(p.getAiRecommendation());
        resp.setAiAnalyzedAt(p.getAiAnalyzedAt());
        resp.setUpdatedAt(p.getUpdatedAt());

        return resp;
    }

    private TaskResponse toTaskResponse(TrainingTask t) {
        TaskResponse resp = new TaskResponse();
        resp.setId(t.getId());
        resp.setTeacherId(t.getTeacherId());
        resp.setTeacherName(t.getTeacherName());
        resp.setTitle(t.getTitle());
        resp.setDescription(t.getDescription());
        resp.setTrainingType(t.getTrainingType().name());
        resp.setDifficulty(t.getDifficulty().name());
        resp.setCourseId(t.getCourseId());
        resp.setStartDate(t.getStartDate());
        resp.setEndDate(t.getEndDate());
        resp.setRequirements(t.getRequirements());
        resp.setAiGenerated(t.getAiGenerated());
        resp.setAiReference(t.getAiReference());
        resp.setStatus(t.getStatus().name());
        resp.setTotalStudents(t.getTotalStudents());
        resp.setCompletedStudents(t.getCompletedStudents());
        resp.setCreatedAt(t.getCreatedAt());

        // 解析目标班级
        try {
            resp.setTargetClasses(objectMapper.readValue(t.getTargetClasses(), new TypeReference<>() {}));
        } catch (Exception e) {
            resp.setTargetClasses(List.of());
        }

        // 计算完成率
        if (t.getTotalStudents() != null && t.getTotalStudents() > 0) {
            resp.setCompletionRate(
                    Math.round((t.getCompletedStudents() * 100.0 / t.getTotalStudents()) * 10) / 10.0);
        } else {
            resp.setCompletionRate(0.0);
        }

        return resp;
    }
}
