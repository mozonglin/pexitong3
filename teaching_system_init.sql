-- 教师签到系统数据库表结构
-- 基于 teaching-api-updated.md 文档
-- 注意：不依赖departments表，直接使用department_name字段

USE pexitong2;

-- =========================================
-- 1. 教师签到记录表
-- =========================================
CREATE TABLE IF NOT EXISTS teacher_attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id VARCHAR(50) NOT NULL COMMENT '教师ID',
    teacher_name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    
    -- 签到信息
    attendance_status ENUM('completed', 'pending', 'late', 'missed') NOT NULL DEFAULT 'pending' COMMENT '签到状态',
    attendance_time TIMESTAMP NULL COMMENT '签到时间',
    attendance_location VARCHAR(200) COMMENT '签到地点',
    attendance_note TEXT COMMENT '签到备注',
    
    -- 课程信息（冗余存储，提高查询效率）
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    classroom VARCHAR(100) COMMENT '教室',
    class_date DATE NOT NULL COMMENT '上课日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    
    -- 系统信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_course_id (course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_teacher_name (teacher_name),
    INDEX idx_class_date (class_date),
    INDEX idx_attendance_status (attendance_status),
    INDEX idx_attendance_time (attendance_time),
    INDEX idx_created_at (created_at),
    UNIQUE KEY uk_course_teacher_date (course_id, teacher_id, class_date)
);

-- =========================================
-- 2. 签到照片表
-- =========================================
CREATE TABLE IF NOT EXISTS teacher_attendance_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_record_id BIGINT NOT NULL COMMENT '签到记录ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id VARCHAR(50) NOT NULL COMMENT '教师ID',
    
    -- 照片信息
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(100) NOT NULL COMMENT '文件类型',
    
    -- 缩略图信息
    thumbnail_path VARCHAR(500) COMMENT '缩略图路径',
    thumbnail_size BIGINT COMMENT '缩略图大小',
    
    -- 拍摄信息
    photo_location VARCHAR(200) COMMENT '拍摄地点',
    photo_note VARCHAR(500) COMMENT '照片说明',
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    
    -- 系统信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (attendance_record_id) REFERENCES teacher_attendance_records(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_attendance_record_id (attendance_record_id),
    INDEX idx_course_id (course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_upload_time (upload_time),
    INDEX idx_file_name (file_name)
);

-- =========================================
-- 4. 签到统计缓存表（可选，用于提高统计查询性能）
-- =========================================
CREATE TABLE IF NOT EXISTS teacher_attendance_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type ENUM('day', 'week', 'month') NOT NULL COMMENT '统计类型',
    school VARCHAR(100) NOT NULL COMMENT '学校名称',
    
    -- 统计数据
    total_courses INT DEFAULT 0 COMMENT '总课程数',
    completed_count INT DEFAULT 0 COMMENT '已签到数',
    pending_count INT DEFAULT 0 COMMENT '未签到数',
    late_count INT DEFAULT 0 COMMENT '迟到数',
    missed_count INT DEFAULT 0 COMMENT '缺勤数',
    attendance_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '签到率',
    on_time_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '准时率',
    
    -- 教师数据
    total_teachers INT DEFAULT 0 COMMENT '教师总数',
    active_teachers INT DEFAULT 0 COMMENT '活跃教师数',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_stat_date (stat_date),
    INDEX idx_stat_type (stat_type),
    INDEX idx_school (school),
    UNIQUE KEY uk_stat_date_type_school (stat_date, stat_type, school)
);

-- =========================================
-- 5. 权限管理视图（针对教学签到系统）
-- =========================================
CREATE OR REPLACE VIEW v_teaching_user_permissions AS
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.user_type,
    u.school,
    u.department_name,
    CASE 
        WHEN u.user_type = 'teacher' THEN '教师'
        WHEN u.user_type = 'department_admin' THEN '院级管理员'
        WHEN u.user_type = 'school_admin' THEN '校级管理员'
        WHEN u.user_type = 'super_admin' THEN '超级管理员'
        ELSE '学生'
    END as role_name,
    CASE 
        WHEN u.user_type IN ('school_admin', 'super_admin') THEN TRUE
        ELSE FALSE
    END as can_view_all_attendance,
    CASE 
        WHEN u.user_type = 'school_admin' THEN u.school
        WHEN u.user_type = 'super_admin' THEN 'ALL'
        ELSE NULL
    END as school_scope,
    NULL as department_scope
FROM users u
WHERE u.status = 'active';

-- =========================================
-- 6. 创建触发器：自动创建签到记录
-- =========================================
DELIMITER $$

CREATE TRIGGER tr_create_attendance_record 
    AFTER INSERT ON courses
    FOR EACH ROW
BEGIN
    -- 为新课程自动创建签到记录
    INSERT INTO teacher_attendance_records (
        course_id, teacher_id, teacher_name, course_name, class_name, 
        classroom, class_date, start_time, end_time,
        attendance_status
    ) VALUES (
        NEW.id, NEW.teacher_id, NEW.teacher_name, NEW.course_name, NEW.class_name,
        NEW.classroom, NEW.class_date, 
        TIME(SUBSTRING_INDEX(NEW.class_time, '-', 1)),
        TIME(SUBSTRING_INDEX(NEW.class_time, '-', -1)),
        'pending'
    );
END$$

DELIMITER ;

-- =========================================
-- 7. 创建存储过程：更新统计数据
-- =========================================
DELIMITER $$

CREATE PROCEDURE sp_update_attendance_statistics(
    IN p_date DATE,
    IN p_type ENUM('day', 'week', 'month'),
    IN p_school VARCHAR(100)
)
BEGIN
    DECLARE v_start_date DATE;
    DECLARE v_end_date DATE;
    
    -- 计算统计日期范围
    CASE p_type
        WHEN 'day' THEN
            SET v_start_date = p_date;
            SET v_end_date = p_date;
        WHEN 'week' THEN
            SET v_start_date = DATE_SUB(p_date, INTERVAL WEEKDAY(p_date) DAY);
            SET v_end_date = DATE_ADD(v_start_date, INTERVAL 6 DAY);
        WHEN 'month' THEN
            SET v_start_date = DATE_FORMAT(p_date, '%Y-%m-01');
            SET v_end_date = LAST_DAY(p_date);
    END CASE;
    
    -- 删除旧的统计数据
    DELETE FROM teacher_attendance_statistics 
    WHERE stat_date = p_date AND stat_type = p_type AND school = p_school;
    
    -- 插入全校统计数据
    INSERT INTO teacher_attendance_statistics (
        stat_date, stat_type, school,
        total_courses, completed_count, pending_count, late_count, missed_count,
        attendance_rate, on_time_rate, total_teachers, active_teachers
    )
    SELECT 
        p_date as stat_date,
        p_type as stat_type,
        p_school as school,
        COUNT(*) as total_courses,
        SUM(CASE WHEN attendance_status = 'completed' THEN 1 ELSE 0 END) as completed_count,
        SUM(CASE WHEN attendance_status = 'pending' THEN 1 ELSE 0 END) as pending_count,
        SUM(CASE WHEN attendance_status = 'late' THEN 1 ELSE 0 END) as late_count,
        SUM(CASE WHEN attendance_status = 'missed' THEN 1 ELSE 0 END) as missed_count,
        CASE WHEN COUNT(*) > 0 THEN 
            ROUND((SUM(CASE WHEN attendance_status IN ('completed', 'late') THEN 1 ELSE 0 END) / COUNT(*)) * 100, 2)
            ELSE 0.00 
        END as attendance_rate,
        CASE WHEN COUNT(*) > 0 THEN 
            ROUND((SUM(CASE WHEN attendance_status = 'completed' THEN 1 ELSE 0 END) / COUNT(*)) * 100, 2)
            ELSE 0.00 
        END as on_time_rate,
        (SELECT COUNT(DISTINCT u.id) FROM users u WHERE u.user_type = 'teacher' AND u.school = p_school AND u.status = 'active') as total_teachers,
        COUNT(DISTINCT tar.teacher_id) as active_teachers
    FROM teacher_attendance_records tar
    JOIN users u ON tar.teacher_id = u.id
    WHERE tar.class_date BETWEEN v_start_date AND v_end_date
    AND u.school = p_school;
    
END$$

DELIMITER ;

-- =========================================
-- 8. 初始化示例数据（基于现有课程创建签到记录）
-- =========================================
-- 为现有课程创建签到记录
INSERT INTO teacher_attendance_records (
    course_id, teacher_id, teacher_name, course_name, class_name, 
    classroom, class_date, start_time, end_time,
    attendance_status
)
SELECT 
    c.id,
    c.teacher_id,
    c.teacher_name,
    c.course_name,
    c.class_name,
    c.classroom,
    c.class_date,
    TIME(SUBSTRING_INDEX(c.class_time, '-', 1)) as start_time,
    TIME(SUBSTRING_INDEX(c.class_time, '-', -1)) as end_time,
    'pending' as attendance_status
FROM courses c
WHERE NOT EXISTS (
    SELECT 1 FROM teacher_attendance_records tar 
    WHERE tar.course_id = c.id AND tar.teacher_id = c.teacher_id AND tar.class_date = c.class_date
);

-- =========================================
-- 9. 创建文件存储目录说明
-- =========================================
/*
应用程序需要创建以下目录结构：
/var/www/storage/teaching/
├── attendance_photos/     # 签到照片
├── attendance_reports/    # 签到报告
└── downloads/            # 下载文件临时目录

或者在 Windows 环境下：
C:\app_data\teaching\
├── attendance_photos\
├── attendance_reports\
└── downloads\
*/

-- =========================================
-- 10. 配置说明
-- =========================================
/*
需要在 application.properties 中添加：

# 教学系统文件存储路径
teaching.file.base-path=/var/www/storage/teaching
# Windows 环境下可能是：
# teaching.file.base-path=C:/app_data/teaching

# 文件访问URL前缀
teaching.file.url-prefix=/api/teaching/files

# 签到时间规则配置
teaching.attendance.early-minutes=30    # 课前多少分钟可以签到
teaching.attendance.late-minutes=15     # 课后多少分钟内算迟到
teaching.attendance.photo-max-size=20MB  # 单张照片最大大小
teaching.attendance.photo-max-count=5    # 每次签到最多照片数量

# 统计任务配置
teaching.statistics.auto-update=true     # 是否自动更新统计
teaching.statistics.update-time=02:00    # 每日统计更新时间
*/

-- =========================================
-- 11. 初始化完成
-- =========================================
SELECT '教师签到系统数据库表初始化完成！' as message;
SELECT COUNT(*) as attendance_record_count FROM teacher_attendance_records;
SELECT COUNT(*) as course_count FROM courses;
SELECT '注意：本系统不使用departments表和院系字段，仅限单一学院使用' as note;
