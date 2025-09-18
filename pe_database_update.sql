-- PEXITONG3数据库更新脚本
-- 用于在现有数据库基础上添加新字段和表
-- 执行前请备份数据库

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 检查并添加早操考勤记录表的签退字段
-- ----------------------------

-- 检查 check_out_time 字段是否存在
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercise_attendance'
                   AND column_name = 'check_out_time');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercise_attendance` ADD COLUMN `check_out_time` timestamp NULL DEFAULT NULL COMMENT ''签退时间'' AFTER `is_valid`',
    'SELECT ''字段 check_out_time 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 check_out_location 字段是否存在
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercise_attendance'
                   AND column_name = 'check_out_location');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercise_attendance` ADD COLUMN `check_out_location` varchar(200) DEFAULT '''' COMMENT ''签退地点'' AFTER `check_out_time`',
    'SELECT ''字段 check_out_location 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 checked_out_by 字段是否存在
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercise_attendance'
                   AND column_name = 'checked_out_by');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercise_attendance` ADD COLUMN `checked_out_by` varchar(36) NULL DEFAULT NULL COMMENT ''签退操作者ID'' AFTER `check_out_location`',
    'SELECT ''字段 checked_out_by 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 checked_out_by_name 字段是否存在
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercise_attendance'
                   AND column_name = 'checked_out_by_name');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercise_attendance` ADD COLUMN `checked_out_by_name` varchar(50) DEFAULT '''' COMMENT ''签退操作者姓名'' AFTER `checked_out_by`',
    'SELECT ''字段 checked_out_by_name 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 is_checked_out 字段是否存在
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercise_attendance'
                   AND column_name = 'is_checked_out');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercise_attendance` ADD COLUMN `is_checked_out` tinyint(1) NOT NULL DEFAULT ''0'' COMMENT ''是否已签退'' AFTER `checked_out_by_name`',
    'SELECT ''字段 is_checked_out 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 points_earned 字段是否存在
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercise_attendance'
                   AND column_name = 'points_earned');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercise_attendance` ADD COLUMN `points_earned` int(11) NOT NULL DEFAULT ''0'' COMMENT ''获得积分(早操固定1分)'' AFTER `is_checked_out`',
    'SELECT ''字段 points_earned 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 updated_at 字段是否存在（morning_exercise_attendance表）
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercise_attendance'
                   AND column_name = 'updated_at');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercise_attendance` ADD COLUMN `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER `created_at`',
    'SELECT ''字段 updated_at 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 检查并添加早操考勤活动表的签退统计字段
-- ----------------------------

-- 检查 checked_out_count 字段是否存在
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'morning_exercises'
                   AND column_name = 'checked_out_count');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `morning_exercises` ADD COLUMN `checked_out_count` int(11) DEFAULT ''0'' COMMENT ''签退人数'' AFTER `checked_in_count`',
    'SELECT ''字段 checked_out_count 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 检查并创建新的表（如果不存在）
-- ----------------------------

-- 这些表在主SQL文件中已经用CREATE TABLE IF NOT EXISTS创建
-- 所以这里不需要额外处理

-- ----------------------------
-- 显示更新完成信息
-- ----------------------------
SELECT '数据库更新完成！' as message;
SELECT '积分计算规则：PE活动 = floor(参与时长(分钟) / 60)，早操 = 固定1积分' as rule;

-- PE校园数据库更新脚本 - 添加活动审核功能
-- 更新日期: 2025-01-09
-- 功能：为activities表添加审核状态相关字段

-- ----------------------------
-- 添加PE统计管理相关字段
-- ----------------------------

-- 为users表添加PE指标字段（校级管理员设置用）
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'users'
                   AND column_name = 'weekly_target');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `users` ADD COLUMN `weekly_target` int(11) DEFAULT NULL COMMENT ''周PE积分指标'' AFTER `password_changed_at`',
    'SELECT ''字段 weekly_target 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'users'
                   AND column_name = 'monthly_target');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `users` ADD COLUMN `monthly_target` int(11) DEFAULT NULL COMMENT ''月PE积分指标'' AFTER `weekly_target`',
    'SELECT ''字段 monthly_target 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'users'
                   AND column_name = 'total_target');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `users` ADD COLUMN `total_target` int(11) DEFAULT NULL COMMENT ''总PE积分指标'' AFTER `monthly_target`',
    'SELECT ''字段 total_target 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为users1表添加班级字段
SET @col_exists = (SELECT COUNT(*)
                   FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 'users1'
                   AND column_name = 'class_name');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `users1` ADD COLUMN `class_name` varchar(100) DEFAULT NULL COMMENT ''班级名称'' AFTER `college`',
    'SELECT ''字段 class_name 已存在'' as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1; 