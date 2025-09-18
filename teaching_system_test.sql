-- 教师签到系统测试SQL
-- 用于验证数据库表创建和功能是否正常

USE pexitong2;

-- =========================================
-- 1. 检查表是否正确创建
-- =========================================
SHOW TABLES LIKE '%attendance%';

-- 检查表结构
DESCRIBE teacher_attendance_records;
DESCRIBE teacher_attendance_photos;
DESCRIBE teacher_attendance_statistics;

-- =========================================
-- 2. 检查权限视图
-- =========================================
SELECT * FROM v_teaching_user_permissions LIMIT 5;

-- =========================================
-- 3. 插入测试数据
-- =========================================

-- 确保有测试用户（超级管理员和校级管理员）
INSERT IGNORE INTO users (id, username, password_hash, real_name, student_id, user_type, school, department_name, phone, status) VALUES
('test_super_admin', 'superadmin', '$2a$10$example_hash', '超级管理员', 'SA001', 'super_admin', '测试大学', '管理部门', '13800000001', 'active'),
('test_school_admin', 'schooladmin', '$2a$10$example_hash', '校级管理员', 'SA002', 'school_admin', '测试大学', '管理部门', '13800000002', 'active'),
('test_teacher_001', 'teacher001', '$2a$10$example_hash', '测试教师1', 'T001', 'teacher', '测试大学', '体育教育学院', '13800000003', 'active'),
('test_teacher_002', 'teacher002', '$2a$10$example_hash', '测试教师2', 'T002', 'teacher', '测试大学', '运动训练学院', '13800000004', 'active');

-- 插入测试课程数据（如果courses表中没有这些字段，这里仅作为示例）
-- 注意：需要根据实际的courses表结构调整字段
-- INSERT IGNORE INTO courses (course_name, teacher_id, teacher_name, class_name, classroom, class_time, class_date) VALUES
-- ('体育理论', 'test_teacher_001', '测试教师1', '体教2024级1班', '理论教室A101', '08:00-09:40', CURDATE()),
-- ('运动训练学', 'test_teacher_002', '测试教师2', '运训2024级1班', '训练馆B201', '10:00-11:40', CURDATE()),
-- ('体育心理学', 'test_teacher_001', '测试教师1', '体教2024级2班', '理论教室A102', '14:00-15:40', CURDATE() - INTERVAL 1 DAY),
-- ('运动生理学', 'test_teacher_002', '测试教师2', '运训2024级2班', '实验室C301', '16:00-17:40', CURDATE() - INTERVAL 2 DAY);

-- =========================================
-- 4. 检查签到记录是否自动创建
-- =========================================
SELECT 
    tar.id,
    tar.course_name,
    tar.teacher_name,
    tar.class_date,
    tar.attendance_status,
    c.id as course_id
FROM teacher_attendance_records tar
LEFT JOIN courses c ON tar.course_id = c.id
ORDER BY tar.class_date DESC, tar.created_at DESC;

-- =========================================
-- 5. 模拟一些签到数据
-- =========================================

-- 更新一些签到记录为已签到状态
UPDATE teacher_attendance_records 
SET 
    attendance_status = 'completed',
    attendance_time = CONCAT(class_date, ' ', DATE_SUB(start_time, INTERVAL 5 MINUTE)),
    attendance_location = classroom,
    attendance_note = '正常签到'
WHERE class_date = CURDATE() 
AND teacher_id = 'test_teacher_001';

-- 更新一些签到记录为迟到状态
UPDATE teacher_attendance_records 
SET 
    attendance_status = 'late',
    attendance_time = CONCAT(class_date, ' ', DATE_ADD(start_time, INTERVAL 10 MINUTE)),
    attendance_location = classroom,
    attendance_note = '迟到10分钟'
WHERE class_date = CURDATE() 
AND teacher_id = 'test_teacher_002';

-- 插入一些测试照片记录
INSERT INTO teacher_attendance_photos (
    attendance_record_id, course_id, teacher_id, original_name, file_name, 
    file_path, file_size, file_type, photo_location, photo_note
) 
SELECT 
    tar.id,
    tar.course_id,
    tar.teacher_id,
    CONCAT('attendance_', tar.id, '.jpg'),
    CONCAT('photo_', UNIX_TIMESTAMP(), '_', tar.id, '.jpg'),
    CONCAT('/uploads/teaching/attendance_photos/photo_', UNIX_TIMESTAMP(), '_', tar.id, '.jpg'),
    1024000,
    'image/jpeg',
    tar.classroom,
    '签到照片'
FROM teacher_attendance_records tar
WHERE tar.attendance_status IN ('completed', 'late')
LIMIT 5;

-- =========================================
-- 6. 测试统计查询
-- =========================================

-- 按状态统计今日签到情况
SELECT 
    attendance_status,
    COUNT(*) as count
FROM teacher_attendance_records 
WHERE class_date = CURDATE()
GROUP BY attendance_status;

-- 统计教师签到情况
SELECT 
    teacher_name,
    COUNT(*) as total_courses,
    SUM(CASE WHEN attendance_status = 'completed' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN attendance_status = 'late' THEN 1 ELSE 0 END) as late,
    SUM(CASE WHEN attendance_status = 'pending' THEN 1 ELSE 0 END) as pending
FROM teacher_attendance_records 
WHERE class_date >= CURDATE() - INTERVAL 7 DAY
GROUP BY teacher_id, teacher_name;

-- =========================================
-- 7. 测试分页查询（模拟API查询）
-- =========================================

-- 模拟获取课程列表API的查询
SELECT 
    tar.course_id as id,
    tar.course_name as name,
    tar.teacher_id,
    tar.teacher_name,
    tar.class_date as date,
    TIME_FORMAT(tar.start_time, '%H:%i') as startTime,
    TIME_FORMAT(tar.end_time, '%H:%i') as endTime,
    tar.classroom as location,
    tar.attendance_status,
    tar.attendance_time,
    tar.attendance_location,
    tar.attendance_note,
    (SELECT COUNT(*) FROM teacher_attendance_photos tap WHERE tap.course_id = tar.course_id) as photo_count
FROM teacher_attendance_records tar
JOIN users u ON tar.teacher_id = u.id
WHERE u.school = '测试大学'
ORDER BY tar.class_date DESC, tar.start_time DESC
LIMIT 10 OFFSET 0;

-- =========================================
-- 8. 权限测试查询
-- =========================================

-- 测试超级管理员权限（可以查看所有数据）
SELECT '超级管理员权限测试' as test_name;
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT teacher_id) as total_teachers,
    COUNT(DISTINCT CASE WHEN attendance_status = 'completed' THEN teacher_id END) as active_teachers
FROM teacher_attendance_records tar
JOIN users u ON tar.teacher_id = u.id
-- 超级管理员没有学校限制
WHERE 1=1;

-- 测试校级管理员权限（只能查看本校数据）
SELECT '校级管理员权限测试' as test_name;
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT teacher_id) as total_teachers,
    COUNT(DISTINCT CASE WHEN attendance_status = 'completed' THEN teacher_id END) as active_teachers
FROM teacher_attendance_records tar
JOIN users u ON tar.teacher_id = u.id
WHERE u.school = '测试大学'; -- 校级管理员只能查看本校

-- =========================================
-- 9. 清理测试数据（可选）
-- =========================================
/*
-- 如果需要清理测试数据，可以运行以下SQL：

DELETE FROM teacher_attendance_photos WHERE teacher_id LIKE 'test_%';
DELETE FROM teacher_attendance_records WHERE teacher_id LIKE 'test_%';
DELETE FROM courses WHERE teacher_id LIKE 'test_%';
DELETE FROM users WHERE id LIKE 'test_%';
*/

-- =========================================
-- 10. 检查最终结果
-- =========================================
SELECT '测试完成' as status;

-- 显示签到记录总数
SELECT COUNT(*) as total_attendance_records FROM teacher_attendance_records;

-- 显示照片记录总数
SELECT COUNT(*) as total_photo_records FROM teacher_attendance_photos;

-- 显示测试用户
SELECT id, username, real_name, user_type, school FROM users WHERE id LIKE 'test_%';

-- 显示测试课程
SELECT id, course_name, teacher_name, class_date FROM courses WHERE teacher_id LIKE 'test_%';
