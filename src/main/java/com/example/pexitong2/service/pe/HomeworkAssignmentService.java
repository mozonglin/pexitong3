package com.example.pexitong2.service.pe;

import com.example.pexitong2.entity.pe.HomeworkAssignment;
import com.example.pexitong2.repository.pe.HomeworkAssignmentRepository;
import com.example.pexitong2.repository.pe.HomeworkSubmissionRepository;
import com.example.pexitong2.repository.pe.TempClassRepository;
import com.example.pexitong2.service.JPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class HomeworkAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(HomeworkAssignmentService.class);

    @Autowired
    private HomeworkAssignmentRepository homeworkAssignmentRepository;

    @Autowired
    private HomeworkSubmissionRepository homeworkSubmissionRepository;

    @Autowired
    private TempClassRepository tempClassRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JPushService jpushService;

    @Transactional
    public HomeworkAssignment createAssignment(String teacherId, String tempClassId, String school,
                                                String title, String exerciseType,
                                                Integer requiredCount, LocalDateTime startTime,
                                                LocalDateTime deadline) {
        HomeworkAssignment assignment = new HomeworkAssignment();
        assignment.setId(UUID.randomUUID().toString());
        assignment.setTeacherId(teacherId);
        assignment.setTempClassId(tempClassId);
        assignment.setSchool(school);
        assignment.setTitle(title);
        assignment.setExerciseType(exerciseType);
        assignment.setRequiredCount(requiredCount);
        assignment.setStartTime(startTime);
        assignment.setDeadline(deadline);
        assignment.setStatus(HomeworkAssignment.AssignmentStatus.active);
        HomeworkAssignment saved = homeworkAssignmentRepository.save(assignment);

        try {
            List<String> studentIds = jdbcTemplate.queryForList(
                    "SELECT student_id FROM temp_class_enrollments WHERE temp_class_id = ?",
                    String.class, tempClassId);
            if (!studentIds.isEmpty()) {
                Map<String, String> extras = new HashMap<>();
                extras.put("assignmentId", saved.getId());
                extras.put("exerciseType", exerciseType);
                jpushService.pushToStudents(studentIds, "新课后作业", saved.getTitle(), extras);
            }
        } catch (Exception e) {
            log.error("作业创建后推送通知失败: {}", e.getMessage(), e);
        }

        return saved;
    }

    public List<HomeworkAssignment> getAssignmentsByTeacher(String teacherId) {
        return homeworkAssignmentRepository.findByTeacherId(teacherId);
    }

    public List<Map<String, Object>> getAssignmentRowsByTeacher(String teacherId) {
        String sql = "SELECT ha.id, ha.teacher_id AS teacherId, ha.temp_class_id AS tempClassId, " +
                "ha.school, ha.title, ha.exercise_type AS exerciseType, ha.required_count AS requiredCount, " +
                "ha.start_time AS startTime, ha.deadline, ha.status, " +
                "tc.class_name AS tempClassName " +
                "FROM homework_assignments ha " +
                "LEFT JOIN temp_classes tc ON ha.temp_class_id = tc.id " +
                "WHERE ha.teacher_id = ? " +
                "ORDER BY ha.created_at DESC";
        return jdbcTemplate.queryForList(sql, teacherId);
    }

    public List<HomeworkAssignment> getAssignmentsBySchool(String school) {
        return homeworkAssignmentRepository.findBySchool(school);
    }

    public List<Map<String, Object>> getAssignmentRowsBySchool(String school) {
        String sql = "SELECT ha.id, ha.teacher_id AS teacherId, ha.temp_class_id AS tempClassId, " +
                "ha.school, ha.title, ha.exercise_type AS exerciseType, ha.required_count AS requiredCount, " +
                "ha.start_time AS startTime, ha.deadline, ha.status, " +
                "tc.class_name AS tempClassName " +
                "FROM homework_assignments ha " +
                "LEFT JOIN temp_classes tc ON ha.temp_class_id = tc.id " +
                "WHERE ha.school = ? " +
                "ORDER BY ha.created_at DESC";
        return jdbcTemplate.queryForList(sql, school);
    }

    public List<Map<String, Object>> getAssignmentCompletions(String assignmentId) {
        String sql = "SELECT hs.id AS submissionId, hs.student_id AS studentId, u.name AS studentName, " +
                "u.student_id AS studentNumber, hs.completed_count AS completedCount, " +
                "ha.required_count AS requiredCount, hs.status, " +
                "CASE WHEN hs.status IN ('submitted','approved') THEN TRUE ELSE FALSE END AS completed, " +
                "hs.submitted_at AS submittedAt " +
                "FROM homework_submissions hs " +
                "LEFT JOIN users1 u ON hs.student_id = u.student_id " +
                "LEFT JOIN homework_assignments ha ON hs.assignment_id = ha.id " +
                "WHERE hs.assignment_id = ? " +
                "ORDER BY u.name";
        return jdbcTemplate.queryForList(sql, assignmentId);
    }

    public List<String> getCounselorClassNames(String userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT class_name FROM counselor_class_assignments WHERE counselor_id = ?", userId);
        List<String> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            result.add((String) r.get("class_name"));
        }
        return result;
    }

    public List<Map<String, Object>> getCompletionDashboard(String school) {
        String sql = "SELECT tc.class_name AS tempClassName, u.real_name AS teacherName, " +
                "COUNT(DISTINCT tce.student_id) AS totalStudents, " +
                "COUNT(DISTINCT CASE WHEN hs.status IN ('submitted','approved') THEN hs.student_id END) AS submittedCount, " +
                "ROUND(" +
                "  CASE WHEN COUNT(DISTINCT tce.student_id) = 0 THEN 0 " +
                "  ELSE COUNT(DISTINCT CASE WHEN hs.status IN ('submitted','approved') THEN hs.student_id END) * 100.0 " +
                "       / COUNT(DISTINCT tce.student_id) END, 2" +
                ") AS completionRate " +
                "FROM homework_assignments ha " +
                "JOIN temp_classes tc ON ha.temp_class_id = tc.id " +
                "LEFT JOIN users u ON ha.teacher_id = u.id " +
                "LEFT JOIN temp_class_enrollments tce ON tc.id = tce.temp_class_id " +
                "LEFT JOIN homework_submissions hs ON ha.id = hs.assignment_id AND hs.student_id = tce.student_id " +
                "WHERE ha.school = ? AND ha.status = 'active' " +
                "GROUP BY ha.id, tc.class_name, u.real_name " +
                "ORDER BY tc.class_name, u.real_name";
        return jdbcTemplate.queryForList(sql, school);
    }

    @Transactional
    public HomeworkAssignment updateAssignment(String id, HomeworkAssignment updates) {
        HomeworkAssignment existing = homeworkAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("作业不存在"));

        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getExerciseType() != null) existing.setExerciseType(updates.getExerciseType());
        if (updates.getRequiredCount() != null) existing.setRequiredCount(updates.getRequiredCount());
        if (updates.getStartTime() != null) existing.setStartTime(updates.getStartTime());
        if (updates.getDeadline() != null) existing.setDeadline(updates.getDeadline());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getTempClassId() != null) existing.setTempClassId(updates.getTempClassId());

        return homeworkAssignmentRepository.save(existing);
    }

    @Transactional
    public void deleteAssignment(String id) {
        HomeworkAssignment assignment = homeworkAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        homeworkAssignmentRepository.delete(assignment);
    }
}
