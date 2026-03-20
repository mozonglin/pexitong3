-- 比赛成绩模块数据库初始化脚本
-- 数据库：pexitong2
-- 说明：该模块完全独立，不依赖 users1 表，是独立的成绩存储表

USE pexitong2;

-- =========================================
-- 创建比赛成绩表
-- =========================================
CREATE TABLE IF NOT EXISTS race_results (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    student_number  VARCHAR(50)     NOT NULL                    COMMENT '学号（来自运动员二维码 sn 字段）',
    name            VARCHAR(100)    NOT NULL                    COMMENT '运动员姓名',
    school          VARCHAR(200)    NOT NULL                    COMMENT '所属学校（取自上传教师的学校）',
    gender          VARCHAR(10)     NOT NULL                    COMMENT '性别：男 / 女',
    total_laps      INT             NOT NULL DEFAULT 0          COMMENT '实际完成圈数',
    final_time      VARCHAR(20)     NULL                        COMMENT '最终成绩格式化字符串，如 03:24.56；未完赛为 NULL',
    final_time_ms   BIGINT          NULL                        COMMENT '最终成绩毫秒数；未完赛为 NULL',
    teacher_name    VARCHAR(100)    NOT NULL                    COMMENT '上传教师姓名（取自登录用户 realName）',
    uploader_id     VARCHAR(50)     NOT NULL                    COMMENT '上传者用户 ID（取自 JWT Token）',
    uploaded_at     DATETIME        NOT NULL                    COMMENT '比赛上传时间（客户端传入）',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',

    INDEX idx_student_number    (student_number),
    INDEX idx_school            (school),
    INDEX idx_gender            (gender),
    INDEX idx_uploaded_at       (uploaded_at),
    INDEX idx_uploader_id       (uploader_id),
    INDEX idx_teacher_name      (teacher_name),
    INDEX idx_final_time_ms     (final_time_ms)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='运动员比赛成绩表（独立模块）';

-- =========================================
-- 完成提示
-- =========================================
SELECT 'race_results 表创建完成！' AS message;
SELECT COUNT(*) AS record_count FROM race_results;
