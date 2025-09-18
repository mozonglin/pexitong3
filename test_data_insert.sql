-- 测试数据插入脚本
-- 用于测试教师签到系统

USE pexitong2;

-- 插入测试用户（如果不存在）
INSERT IGNORE INTO users (id, username, password_hash, real_name, student_id, user_type, school, department_name, phone, status) VALUES
('test_super_admin', 'superadmin', '$2a$10$example_hash', '超级管理员', 'SA001', 'super_admin', '测试大学', '管理部门', '13800000001', 'active'),
('test_school_admin', 'schooladmin', '$2a$10$example_hash', '校级管理员', 'SA002', 'school_admin', '测试大学', '管理部门', '13800000002', 'active'),
('test_teacher_001', 'teacher001', '$2a$10$example_hash', '测试教师1', 'T001', 'teacher', '测试大学', '体育教育学院', '13800000003', 'active'),
('test_teacher_002', 'teacher002', '$2a$10$example_hash', '测试教师2', 'T002', 'teacher', '测试大学', '运动训练学院', '13800000004', 'active');

-- 插入测试课程数据（根据现有courses表结构）
-- 注意：这里需要根据您实际的courses表结构来调整
INSERT IGNORE INTO courses (id, course_name, teacher_id, teacher_name, class_name, classroom, class_time, class_date) VALUES
(1001, '体育理论', 'test_teacher_001', '测试教师1', '体教2024级1班', '理论教室A101', '08:00-09:40', CURDATE()),
(1002, '运动训练学', 'test_teacher_002', '测试教师2', '运训2024级1班', '训练馆B201', '10:00-11:40', CURDATE()),
(1003, '体育心理学', 'test_teacher_001', '测试教师1', '体教2024级2班', '理论教室A102', '14:00-15:40', CURDATE() - INTERVAL 1 DAY),
(1004, '运动生理学', 'test_teacher_002', '测试教师2', '运训2024级2班', '实验室C301', '16:00-17:40', CURDATE() - INTERVAL 2 DAY);

-- 手动插入签到记录（如果触发器没有工作）
INSERT IGNORE INTO teacher_attendance_records (
    course_id, teacher_id, teacher_name, course_name, class_name, 
    classroom, class_date, start_time, end_time, attendance_status
) VALUES
(1001, 'test_teacher_001', '测试教师1', '体育理论', '体教2024级1班', '理论教室A101', CURDATE(), '08:00', '09:40', 'completed'),
(1002, 'test_teacher_002', '测试教师2', '运动训练学', '运训2024级1班', '训练馆B201', CURDATE(), '10:00', '11:40', 'late'),
(1003, 'test_teacher_001', '测试教师1', '体育心理学', '体教2024级2班', '理论教室A102', CURDATE() - INTERVAL 1 DAY, '14:00', '15:40', 'completed'),
(1004, 'test_teacher_002', '测试教师2', '运动生理学', '运训2024级2班', '实验室C301', CURDATE() - INTERVAL 2 DAY, '16:00', '17:40', 'pending');

-- 更新一些签到记录的签到时间
UPDATE teacher_attendance_records 
SET 
    attendance_time = CONCAT(class_date, ' ', DATE_SUB(start_time, INTERVAL 5 MINUTE)),
    attendance_location = classroom,
    attendance_note = '正常签到'
WHERE attendance_status = 'completed';

UPDATE teacher_attendance_records 
SET 
    attendance_time = CONCAT(class_date, ' ', DATE_ADD(start_time, INTERVAL 10 MINUTE)),
    attendance_location = classroom,
    attendance_note = '迟到10分钟'
WHERE attendance_status = 'late';

-- 查看插入的数据
SELECT '=== 插入的用户 ===' as info;
SELECT id, username, real_name, user_type, school FROM users WHERE id LIKE 'test_%';

SELECT '=== 插入的课程 ===' as info;
SELECT id, course_name, teacher_name, class_date FROM courses WHERE id >= 1001;

SELECT '=== 插入的签到记录 ===' as info;
SELECT 
    id, course_name, teacher_name, class_date, 
    attendance_status, attendance_time 
FROM teacher_attendance_records 
ORDER BY class_date DESC;

-- 统计数据检查
SELECT '=== 按状态统计 ===' as info;
SELECT 
    attendance_status,
    COUNT(*) as count
FROM teacher_attendance_records 
GROUP BY attendance_status;

SELECT '=== 按日期统计 ===' as info;
SELECT 
    class_date,
    COUNT(*) as total_courses,
    SUM(CASE WHEN attendance_status = 'completed' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN attendance_status = 'late' THEN 1 ELSE 0 END) as late,
    SUM(CASE WHEN attendance_status = 'pending' THEN 1 ELSE 0 END) as pending
FROM teacher_attendance_records 
GROUP BY class_date
ORDER BY class_date DESC;




