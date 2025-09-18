-- PE校园数据库结构
-- 数据库：pexitong3

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `users1` (
  `id` varchar(36) NOT NULL COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '用户姓名',
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `school` varchar(100) DEFAULT NULL COMMENT '学校',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `phone_number` varchar(20) NOT NULL COMMENT '手机号',
  `avatar` text COMMENT '头像URL',
  `points` int(11) NOT NULL DEFAULT '0' COMMENT '总积分',
  `pe_activity_points` int(11) NOT NULL DEFAULT '0' COMMENT 'PE活动积分',
  `morning_exercise_points` int(11) NOT NULL DEFAULT '0' COMMENT '早操积分',
  `study_hours` int(11) NOT NULL DEFAULT '0' COMMENT '学时',
  `integrity_score` int(11) NOT NULL DEFAULT '100' COMMENT '诚信度',
  `role` enum('STUDENT','CHECKER','SUB_CHECKER','ADMIN') NOT NULL DEFAULT 'STUDENT' COMMENT '用户角色',
  `is_logged_in` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已登录',
  `points_last_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '积分最后更新时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  UNIQUE KEY `uk_phone_number` (`phone_number`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_phone_number` (`phone_number`),
  KEY `idx_points` (`points`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 活动表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `activities` (
  `id` varchar(36) NOT NULL COMMENT '活动ID',
  `title` varchar(200) NOT NULL COMMENT '活动标题',
  `description` text COMMENT '活动描述',
  `location` varchar(200) DEFAULT NULL COMMENT '活动地点',
  `max_participants` int(11) DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` int(11) NOT NULL DEFAULT '0' COMMENT '当前参与人数',
  `registration_start_time` timestamp NULL DEFAULT NULL COMMENT '报名开始时间',
  `registration_end_time` timestamp NULL DEFAULT NULL COMMENT '报名结束时间',
  `activity_start_time` timestamp NULL DEFAULT NULL COMMENT '活动开始时间',
  `activity_end_time` timestamp NULL DEFAULT NULL COMMENT '活动结束时间',
  `organizer` varchar(100) DEFAULT NULL COMMENT '组织者',
  `organizer_id` varchar(36) DEFAULT NULL COMMENT '组织者ID',
  `category` varchar(50) DEFAULT NULL COMMENT '活动类别',
  `points` int(11) NOT NULL DEFAULT '0' COMMENT '积分奖励',
  `image_url` text COMMENT '活动图片URL',
  `approval_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-审核中，APPROVED-已通过，REJECTED-已拒绝，DRAFT-草稿',
  `reviewed_by` varchar(36) DEFAULT NULL COMMENT '审核人ID',
  `reviewed_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `review_comment` text COMMENT '审核意见',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_organizer_id` (`organizer_id`),
  KEY `idx_category` (`category`),
  KEY `idx_activity_time` (`activity_start_time`, `activity_end_time`),
  KEY `idx_registration_time` (`registration_start_time`, `registration_end_time`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_organizer_approval` (`organizer_id`, `approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- ----------------------------
-- 活动报名表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `activity_registrations` (
  `id` varchar(36) NOT NULL COMMENT '报名记录ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `activity_id` varchar(36) NOT NULL COMMENT '活动ID',
  `status` enum('REGISTERED','ATTENDED','ABSENT','CANCELLED') NOT NULL DEFAULT 'REGISTERED' COMMENT '报名状态',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';

-- ----------------------------
-- 签到记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `attendance_records` (
  `id` varchar(36) NOT NULL COMMENT '签到记录ID',
  `activity_id` varchar(36) NOT NULL COMMENT '活动ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户姓名',
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `check_in_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
  `check_in_location` varchar(200) DEFAULT NULL COMMENT '签到地点',
  `checked_in_by` varchar(36) DEFAULT NULL COMMENT '签到操作者ID',
  `check_out_time` timestamp NULL DEFAULT NULL COMMENT '签退时间',
  `check_out_location` varchar(200) DEFAULT NULL COMMENT '签退地点',
  `checked_out_by` varchar(36) DEFAULT NULL COMMENT '签退操作者ID',
  `is_checked_out` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已签退',
  `duration` int(11) NOT NULL DEFAULT '0' COMMENT '参与时长(分钟)',
  `points_earned` int(11) NOT NULL DEFAULT '0' COMMENT '获得积分',
  `qr_code_data` text COMMENT '二维码数据',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_check_in_time` (`check_in_time`),
  KEY `idx_check_out_time` (`check_out_time`),
  KEY `idx_is_checked_out` (`is_checked_out`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ----------------------------
-- 活动签到码表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `activity_checkin_codes` (
  `id` varchar(36) NOT NULL COMMENT '签到码ID',
  `unique_code` varchar(100) NOT NULL COMMENT '唯一码',
  `activity_id` varchar(36) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(200) NOT NULL COMMENT '活动名称',
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名',
  `timestamp` bigint(20) NOT NULL COMMENT '时间戳',
  `expires_at` bigint(20) NOT NULL COMMENT '过期时间戳',
  `is_valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已使用',
  `used_at` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `used_by` varchar(36) DEFAULT NULL COMMENT '使用者ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_unique_code` (`unique_code`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_expires_at` (`expires_at`),
  KEY `idx_valid_used` (`is_valid`, `is_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动签到码表';

-- ----------------------------
-- JWT Token黑名单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `token_blacklist` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `token_jti` varchar(255) NOT NULL COMMENT 'Token JTI',
  `user_id` varchar(36) DEFAULT NULL COMMENT '用户ID',
  `expires_at` timestamp NOT NULL COMMENT '过期时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token_jti` (`token_jti`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JWT Token黑名单表';

-- ----------------------------
-- 早操考勤活动表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `morning_exercises` (
  `id` varchar(36) NOT NULL COMMENT '早操活动ID',
  `title` varchar(200) NOT NULL DEFAULT '早操考勤' COMMENT '活动标题',
  `description` text COMMENT '活动描述',
  `location` varchar(200) DEFAULT '操场' COMMENT '活动地点',
  `date` date NOT NULL COMMENT '日期',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `type` enum('MORNING_EXERCISE') DEFAULT 'MORNING_EXERCISE' COMMENT '类型',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否活跃',
  `total_participants` int(11) DEFAULT '0' COMMENT '总参与人数',
  `checked_in_count` int(11) DEFAULT '0' COMMENT '签到人数',
  `created_by` varchar(36) NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_date` (`date`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='早操考勤活动表';

-- ----------------------------
-- 早操考勤记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `morning_exercise_attendance` (
  `id` varchar(36) NOT NULL COMMENT '考勤记录ID',
  `exercise_id` varchar(36) NOT NULL COMMENT '早操活动ID',
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名',
  `check_in_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
  `check_in_location` varchar(200) DEFAULT '' COMMENT '签到地点',
  `checked_by` varchar(36) NOT NULL COMMENT '签到员ID',
  `checked_by_name` varchar(50) NOT NULL COMMENT '签到员姓名',
  `qr_code_data` text NOT NULL COMMENT '二维码数据',
  `is_valid` tinyint(1) DEFAULT '1' COMMENT '是否有效',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exercise_student` (`exercise_id`, `student_id`),
  KEY `idx_exercise_id` (`exercise_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_checked_by` (`checked_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='早操考勤记录表';

-- ----------------------------
-- 签到员授权表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `checker_authorizations` (
  `id` varchar(36) NOT NULL COMMENT '授权ID',
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名',
  `role` enum('CHECKER','SUB_CHECKER') NOT NULL COMMENT '角色：CHECKER-签到员，SUB_CHECKER-二级管理员',
  `authorized_by` varchar(36) NOT NULL COMMENT '授权人ID',
  `authorized_by_name` varchar(50) NOT NULL COMMENT '授权人姓名',
  `authorized_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否活跃',
  `exercise_id` varchar(36) NOT NULL COMMENT '早操活动ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_exercise` (`student_id`, `exercise_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_authorized_by` (`authorized_by`),
  KEY `idx_exercise_id` (`exercise_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到员授权表';

-- ----------------------------
-- WebSocket连接表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `websocket_connections` (
  `id` varchar(36) NOT NULL COMMENT '连接ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `connection_id` varchar(100) NOT NULL COMMENT '连接标识',
  `connected_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '连接时间',
  `last_ping_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后心跳时间',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否活跃',
  PRIMARY KEY (`id`),
  KEY `idx_user_connection` (`user_id`, `connection_id`),
  KEY `idx_active_ping` (`is_active`, `last_ping_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WebSocket连接表';

-- ----------------------------
-- 积分兑换记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `point_exchanges` (
  `id` varchar(36) NOT NULL COMMENT '兑换记录ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `type` enum('PARTICIPATE_ACTIVITY','CREATE_ACTIVITY') NOT NULL COMMENT '兑换类型',
  `points` int(11) NOT NULL COMMENT '使用积分',
  `study_hours` int(11) NOT NULL COMMENT '获得学时',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

-- ----------------------------
-- 权限设置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_permissions` (
  `id` varchar(36) NOT NULL COMMENT '权限设置ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `camera_permission` tinyint(1) DEFAULT '0' COMMENT '相机权限',
  `location_permission` tinyint(1) DEFAULT '0' COMMENT '位置权限',
  `storage_permission` tinyint(1) DEFAULT '0' COMMENT '存储权限',
  `notification_permission` tinyint(1) DEFAULT '0' COMMENT '通知权限',
  `phone_permission` tinyint(1) DEFAULT '0' COMMENT '电话权限',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限设置表';

-- ----------------------------
-- 插入示例数据
-- ----------------------------

-- 插入示例用户（使用INSERT IGNORE避免重复插入）
INSERT IGNORE INTO `users` (`id`, `name`, `student_id`, `school`, `college`, `phone_number`, `points`, `study_hours`, `integrity_score`, `is_logged_in`) VALUES
('user_001', '张三', '2021001', '某大学', '计算机学院', '13800138000', 100, 5, 95, 0),
('user_002', '李四', '2021002', '某大学', '体育学院', '13900139000', 80, 3, 98, 0),
('user_003', '王五', '2021003', '某大学', '计算机学院', '13700137000', 120, 8, 92, 0);

-- 插入示例活动（使用INSERT IGNORE避免重复插入）
INSERT IGNORE INTO `activities` (`id`, `title`, `description`, `location`, `max_participants`, `current_participants`, `registration_start_time`, `registration_end_time`, `activity_start_time`, `activity_end_time`, `organizer`, `organizer_id`, `category`, `points`, `image_url`) VALUES
('activity_001', '篮球比赛', '校内篮球比赛，欢迎参加', '体育馆', 20, 15, '2024-01-01 09:00:00', '2024-01-05 18:00:00', '2024-01-06 14:00:00', '2024-01-06 16:00:00', '体育部', 'user_002', '球类运动', 10, ''),
('activity_002', '跑步活动', '晨跑活动，强身健体', '操场', 50, 25, '2024-01-02 09:00:00', '2024-01-06 18:00:00', '2024-01-07 06:30:00', '2024-01-07 07:30:00', '体育部', 'user_002', '跑步', 10, '');

-- 插入示例权限设置（使用INSERT IGNORE避免重复插入）
INSERT IGNORE INTO `user_permissions` (`id`, `user_id`, `camera_permission`, `location_permission`, `storage_permission`, `notification_permission`, `phone_permission`) VALUES
('perm_001', 'user_001', 1, 0, 1, 1, 0),
('perm_002', 'user_002', 1, 1, 1, 1, 1),
('perm_003', 'user_003', 0, 0, 1, 1, 0);

-- ----------------------------
-- 活动签退码表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `activity_checkout_codes` (
  `id` varchar(36) NOT NULL COMMENT '签退码ID',
  `unique_code` varchar(100) NOT NULL COMMENT '唯一码',
  `activity_id` varchar(36) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(200) NOT NULL COMMENT '活动名称',
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名',
  `timestamp` bigint(20) NOT NULL COMMENT '时间戳',
  `expires_at` bigint(20) NOT NULL COMMENT '过期时间戳',
  `is_valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已使用',
  `used_at` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `used_by` varchar(36) DEFAULT NULL COMMENT '使用者ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_unique_code` (`unique_code`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_expires_at` (`expires_at`),
  KEY `idx_valid_used` (`is_valid`, `is_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动签退码表';

-- ----------------------------
-- 积分记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `points_records` (
  `id` varchar(36) NOT NULL COMMENT '记录ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `activity_id` varchar(36) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(200) NOT NULL COMMENT '活动名称',
  `activity_type` varchar(20) NOT NULL COMMENT '活动类型',
  `points_earned` int(11) NOT NULL COMMENT '获得积分',
  `earned_reason` varchar(200) DEFAULT NULL COMMENT '获得原因',
  `calculation_rule` varchar(200) DEFAULT NULL COMMENT '计算规则',
  `participation_duration` int(11) NOT NULL DEFAULT '0' COMMENT '参与时长(分钟)',
  `earned_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_earned_at` (`user_id`, `earned_at`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_activity_type` (`activity_type`),
  KEY `idx_points_earned` (`points_earned`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- ----------------------------
-- 用户积分汇总表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_points_summary` (
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `total_points` int(11) NOT NULL DEFAULT '0' COMMENT '总积分',
  `pe_activity_points` int(11) NOT NULL DEFAULT '0' COMMENT 'PE活动积分',
  `morning_exercise_points` int(11) NOT NULL DEFAULT '0' COMMENT '早操积分',
  `last_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`),
  KEY `idx_total_points` (`total_points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分汇总表';

-- ----------------------------
-- 早操签退码表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `morning_exercise_checkout_codes` (
  `id` varchar(36) NOT NULL COMMENT '签退码ID',
  `unique_code` varchar(100) NOT NULL COMMENT '唯一码',
  `exercise_id` varchar(36) NOT NULL COMMENT '早操ID',
  `exercise_name` varchar(200) NOT NULL COMMENT '早操名称',
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名',
  `timestamp` bigint(20) NOT NULL COMMENT '时间戳',
  `expires_at` bigint(20) NOT NULL COMMENT '过期时间戳',
  `is_valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已使用',
  `used_at` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `used_by` varchar(36) DEFAULT NULL COMMENT '使用者ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_unique_code` (`unique_code`),
  KEY `idx_exercise_id` (`exercise_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_expires_at` (`expires_at`),
  KEY `idx_valid_used` (`is_valid`, `is_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='早操签退码表';

-- ----------------------------
-- 更新早操考勤记录表，添加签退字段
-- ----------------------------
-- 请注意：如果字段已存在，需要手动删除重复的ALTER语句
-- ALTER TABLE `morning_exercise_attendance` 
-- ADD COLUMN `check_out_time` timestamp NULL DEFAULT NULL COMMENT '签退时间' AFTER `is_valid`,
-- ADD COLUMN `check_out_location` varchar(200) DEFAULT '' COMMENT '签退地点' AFTER `check_out_time`,
-- ADD COLUMN `checked_out_by` varchar(36) NULL DEFAULT NULL COMMENT '签退操作者ID' AFTER `check_out_location`,
-- ADD COLUMN `checked_out_by_name` varchar(50) DEFAULT '' COMMENT '签退操作者姓名' AFTER `checked_out_by`,
-- ADD COLUMN `is_checked_out` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已签退' AFTER `checked_out_by_name`,
-- ADD COLUMN `points_earned` int(11) NOT NULL DEFAULT '0' COMMENT '获得积分(早操固定1分)' AFTER `is_checked_out`,
-- ADD COLUMN `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;

-- ----------------------------
-- 更新早操考勤活动表，添加签退统计字段
-- ----------------------------
-- 请注意：如果字段已存在，需要手动删除重复的ALTER语句
-- ALTER TABLE `morning_exercises` 
-- ADD COLUMN `checked_out_count` int(11) DEFAULT '0' COMMENT '签退人数' AFTER `checked_in_count`;

-- ----------------------------
-- 积分计算规则说明（最新版本）
-- ----------------------------
-- PE活动积分计算规则：积分 = floor(参与时长(分钟) / 60)
-- 早操积分计算规则：完成签到和签退获得固定1积分
-- 所有积分计算由后端完成，通过WebSocket实时推送给前端
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1; 