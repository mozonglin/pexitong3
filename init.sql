-- 校园体育管理系统数据库初始化脚本
-- 主数据库：pexitong2
-- 预导入数据库：checkuser（已存在，不需要创建）

-- =========================================
-- 1. 创建主数据库（如果不存在）
-- =========================================
CREATE DATABASE IF NOT EXISTS pexitong2 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE pexitong2;

-- =========================================
-- 2. 创建主数据库表结构
-- =========================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    student_id VARCHAR(30) UNIQUE NOT NULL,
    user_type ENUM('student', 'teacher', 'department_admin', 'school_admin', 'super_admin') NOT NULL,
    school VARCHAR(100) NOT NULL,
    department_name VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status ENUM('active', 'suspended', 'banned') DEFAULT 'active',
    is_first_login BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    login_count INT DEFAULT 0,
    password_changed_at TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_student_id (student_id),
    INDEX idx_user_type (user_type),
    INDEX idx_school (school),
    INDEX idx_status (status)
);

-- 验证码表
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    code VARCHAR(6) NOT NULL,
    type ENUM('register', 'reset_password', 'change_phone') NOT NULL,
    is_used BOOLEAN DEFAULT false,
    attempt_count INT DEFAULT 0,
    max_attempts INT DEFAULT 5,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP NULL,
    INDEX idx_phone (phone),
    INDEX idx_code (code),
    INDEX idx_type (type),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_used (is_used)
);

-- 角色变更记录表
CREATE TABLE IF NOT EXISTS role_change_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    old_role ENUM('student', 'teacher', 'department_admin', 'school_admin', 'super_admin'),
    new_role ENUM('student', 'teacher', 'department_admin', 'school_admin', 'super_admin') NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_changed_by (changed_by),
    INDEX idx_changed_at (changed_at)
);

-- =========================================
-- 3. 创建默认超级管理员用户
-- =========================================
INSERT INTO users (
    id, 
    username, 
    password_hash, 
    real_name, 
    student_id, 
    user_type, 
    school, 
    department_name,
    status, 
    is_first_login,
    created_at
) VALUES (
    'super_admin_001', 
    'superadmin', 
    '$2a$12$8yKKWGpgcgIRNQ.Uo6/2r.xUaGvsD7Ooaf7U9/FKlGEkF5.Jb5O1i', -- 密码: admin123456
    '系统管理员', 
    'SUPER001', 
    'super_admin', 
    '系统', 
    '系统管理部',
    'active', 
    false,
    NOW()
) ON DUPLICATE KEY UPDATE id = id;

-- =========================================
-- 4. 插入示例学校数据（可选）
-- =========================================
-- 注：根据API文档支持的学校列表
INSERT IGNORE INTO users (id, username, password_hash, real_name, student_id, user_type, school, department_name, status, is_first_login) VALUES
('test_student_001', 'test_student', '$2a$12$8yKKWGpgcgIRNQ.Uo6/2r.xUaGvsD7Ooaf7U9/FKlGEkF5.Jb5O1i', '测试学生', 'STU001', 'student', '北京体育大学', '体育教育学院', 'active', false),
('test_teacher_001', 'test_teacher', '$2a$12$8yKKWGpgcgIRNQ.Uo6/2r.xUaGvsD7Ooaf7U9/FKlGEkF5.Jb5O1i', '测试教师', 'TEA001', 'teacher', '北京体育大学', '体育教育学院', 'active', false);

-- =========================================
-- 5. 数据库配置说明
-- =========================================
/*
数据库配置：
1. 主数据库：pexitong2
   - 存储用户信息、验证码、角色变更日志等业务数据
   - 连接配置在 application.properties 中

2. 预导入数据库：checkuser（已存在）
   - 存储预导入的学生和教师基础信息
   - 表结构：
     * checkstudent: school, college, studentid, name
     * checkteacher: school, teacherid, college, name
   - 通过 CheckUserService 使用 JDBC 直接访问

使用说明：
1. 确保 MySQL 服务已启动
2. 确保两个数据库都存在且可访问
3. 运行此脚本初始化主数据库
4. 预导入数据库 checkuser 由用户维护，系统直接访问验证

默认用户：
- 超级管理员：superadmin / admin123456
- 测试学生：test_student / admin123456  
- 测试教师：test_teacher / admin123456
*/

-- =========================================
-- 6. 权限检查
-- =========================================
SHOW GRANTS FOR CURRENT_USER;

-- =========================================
-- 7. 完成提示
-- =========================================
SELECT 'pexitong2 数据库初始化完成！' as message;
SELECT COUNT(*) as user_count FROM users;
SELECT 'checkuser 数据库连接测试：请确保该数据库已存在并包含 checkstudent 和 checkteacher 表' as note; 