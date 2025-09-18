-- 听课系统数据库表结构（简化版）
-- 不使用独立的院系表，直接使用users表中的department_name

USE pexitong2;

-- =========================================
-- 1. 课程表（去掉department_id外键）
-- =========================================
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_id VARCHAR(50) NOT NULL COMMENT '教师ID',
    teacher_name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    classroom VARCHAR(100) COMMENT '教室',
    class_time VARCHAR(20) COMMENT '上课时间，如：08:00-09:40',
    class_date DATE NOT NULL COMMENT '上课日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_teacher_name (teacher_name),
    INDEX idx_class_date (class_date),
    INDEX idx_course_name (course_name)
);

-- =========================================
-- 2. 评价模板表
-- =========================================
CREATE TABLE IF NOT EXISTS evaluation_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '模板名称',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_type VARCHAR(100) NOT NULL COMMENT '文件类型',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认模板',
    uploaded_by VARCHAR(50) NOT NULL COMMENT '上传者ID',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_is_default (is_default),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_name (name)
);

-- =========================================
-- 3. 听课记录表
-- =========================================
CREATE TABLE IF NOT EXISTS listening_observations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    observer_id VARCHAR(50) NOT NULL COMMENT '听课者ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    class_date DATE NOT NULL COMMENT '听课日期',
    
    -- 评价文件信息
    evaluation_file_name VARCHAR(255) COMMENT '评价文件名',
    evaluation_file_path VARCHAR(500) COMMENT '评价文件路径',
    evaluation_file_type VARCHAR(100) COMMENT '评价文件类型',
    evaluation_file_size BIGINT COMMENT '评价文件大小',
    evaluation_uploaded_at TIMESTAMP NULL COMMENT '评价文件上传时间',
    
    -- 视频文件信息
    video_file_name VARCHAR(255) COMMENT '视频文件名',
    video_file_path VARCHAR(500) COMMENT '视频文件路径',
    video_file_type VARCHAR(100) COMMENT '视频文件类型',
    video_file_size BIGINT COMMENT '视频文件大小',
    video_uploaded_at TIMESTAMP NULL COMMENT '视频文件上传时间',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (observer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_observer_id (observer_id),
    INDEX idx_course_id (course_id),
    INDEX idx_class_date (class_date),
    INDEX idx_created_at (created_at)
);

-- =========================================
-- 4. 插入示例课程数据
-- =========================================
-- 注意：需要确保对应的教师用户已存在
INSERT INTO courses (course_name, teacher_id, teacher_name, class_name, classroom, class_time, class_date) VALUES
('体育与健康', 'test_teacher_001', '测试教师', '高一(1)班', '操场', '08:00-09:40', '2024-01-15'),
('运动生理学', 'test_teacher_001', '测试教师', '体教2021级', '理论教室A101', '10:00-11:40', '2024-01-16'),
('武术套路', 'test_teacher_001', '测试教师', '武术2022级', '武术馆', '14:00-15:40', '2024-01-17'),
('体育心理学', 'test_teacher_001', '测试教师', '运训2021级', '理论教室B203', '16:00-17:40', '2024-01-18')
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name);

-- =========================================
-- 5. 院系统计视图（基于users表的department_name）
-- =========================================
CREATE OR REPLACE VIEW v_department_statistics AS
SELECT 
    u.department_name,
    COUNT(DISTINCT u.id) as teacher_count,
    COUNT(DISTINCT lo.id) as observation_count,
    SUM(CASE WHEN lo.evaluation_file_name IS NOT NULL THEN 1 ELSE 0 END) as evaluation_files,
    SUM(CASE WHEN lo.video_file_name IS NOT NULL THEN 1 ELSE 0 END) as video_files
FROM users u
LEFT JOIN courses c ON u.id = c.teacher_id
LEFT JOIN listening_observations lo ON c.id = lo.course_id
WHERE u.user_type IN ('teacher', 'department_admin', 'school_admin')
AND u.department_name IS NOT NULL
GROUP BY u.department_name;

-- =========================================
-- 6. 完成提示
-- =========================================
SELECT '听课系统数据库表初始化完成（简化版）！' as message;
SELECT COUNT(*) as course_count FROM courses; 