-- 听课系统数据库表结构
-- 基于 listening-api-v2.md 文档

USE pexitong2;

-- =========================================
-- 1. 院系表
-- =========================================
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '院系名称',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '院系代码',
    description VARCHAR(255) COMMENT '院系描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_code (code)
);

-- =========================================
-- 2. 课程表
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
    department_id BIGINT COMMENT '院系ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_teacher_name (teacher_name),
    INDEX idx_class_date (class_date),
    INDEX idx_course_name (course_name),
    INDEX idx_department_id (department_id)
);

-- =========================================
-- 3. 评价模板表
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
-- 4. 听课记录表
-- =========================================
CREATE TABLE IF NOT EXISTS listening_observations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    observer_id VARCHAR(50) NOT NULL COMMENT '听课者ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    class_date DATE NOT NULL COMMENT '听课日期',
    
    -- 评价文件信息（JSON格式存储）
    evaluation_file_name VARCHAR(255) COMMENT '评价文件名',
    evaluation_file_path VARCHAR(500) COMMENT '评价文件路径',
    evaluation_file_type VARCHAR(100) COMMENT '评价文件类型',
    evaluation_file_size BIGINT COMMENT '评价文件大小',
    evaluation_uploaded_at TIMESTAMP NULL COMMENT '评价文件上传时间',
    
    -- 视频文件信息（JSON格式存储）
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
-- 5. 插入默认院系数据
-- =========================================
INSERT INTO departments (name, code, description) VALUES
('体育教育学院', 'PE', '体育教育专业院系'),
('运动训练学院', 'ST', '运动训练专业院系'),
('体育人文学院', 'SH', '体育人文社会学专业院系'),
('武术与民族传统体育学院', 'MA', '武术与民族传统体育专业院系'),
('运动人体科学学院', 'EHS', '运动人体科学专业院系'),
('体育商学院', 'SB', '体育经济与管理专业院系'),
('国际体育组织学院', 'ISO', '国际体育组织学院'),
('艺术学院', 'ART', '舞蹈表演等艺术专业院系'),
('马克思主义学院', 'MXS', '思想政治理论课教学院系'),
('外国语学院', 'FL', '外语教学院系'),
('心理学院', 'PSY', '应用心理学专业院系'),
('数据科学与人工智能学院', 'AI', '数据科学与人工智能院系')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =========================================
-- 6. 插入示例课程数据
-- =========================================
-- 注意：需要确保对应的教师用户已存在
INSERT INTO courses (course_name, teacher_id, teacher_name, class_name, classroom, class_time, class_date, department_id) VALUES
('体育与健康', 'test_teacher_001', '测试教师', '高一(1)班', '操场', '08:00-09:40', '2024-01-15', 1),
('运动生理学', 'test_teacher_001', '测试教师', '体教2021级', '理论教室A101', '10:00-11:40', '2024-01-16', 1),
('武术套路', 'test_teacher_001', '测试教师', '武术2022级', '武术馆', '14:00-15:40', '2024-01-17', 4),
('体育心理学', 'test_teacher_001', '测试教师', '运训2021级', '理论教室B203', '16:00-17:40', '2024-01-18', 2)
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name);

-- =========================================
-- 7. 创建文件存储目录（应用程序需要创建）
-- =========================================
/*
应用程序需要创建以下目录结构：
/var/www/storage/listening/
├── evaluations/           # 评价文件
├── videos/               # 听课视频
└── templates/            # 模板文件

或者在 Windows 环境下：
C:\app_data\listening\
├── evaluations\
├── videos\
└── templates\
*/

-- =========================================
-- 8. 更新文件上传配置说明
-- =========================================
/*
需要在 application.properties 中添加：

# 文件上传配置（已存在，需要调整大小）
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# 听课系统文件存储路径
listening.file.base-path=/var/www/storage/listening
# Windows 环境下可能是：
# listening.file.base-path=C:/app_data/listening

# 文件访问URL前缀
listening.file.url-prefix=/api/files/download
*/

-- =========================================
-- 9. 权限检查视图（可选）
-- =========================================
CREATE OR REPLACE VIEW v_user_permissions AS
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.user_type,
    u.department_name,
    CASE 
        WHEN u.user_type = 'teacher' THEN '教师'
        WHEN u.user_type = 'department_admin' THEN '院级管理员'
        WHEN u.user_type = 'school_admin' THEN '校级管理员'
        WHEN u.user_type = 'super_admin' THEN '超级管理员'
        ELSE '学生'
    END as role_name,
    CASE 
        WHEN u.user_type IN ('department_admin', 'school_admin', 'super_admin') THEN TRUE
        ELSE FALSE
    END as can_manage_observations,
    CASE 
        WHEN u.user_type IN ('school_admin', 'super_admin') THEN TRUE
        ELSE FALSE
    END as can_manage_templates
FROM users u
WHERE u.status = 'active';

-- =========================================
-- 10. 初始化完成提示
-- =========================================
SELECT '听课系统数据库表初始化完成！' as message;
SELECT COUNT(*) as department_count FROM departments;
SELECT COUNT(*) as course_count FROM courses; 