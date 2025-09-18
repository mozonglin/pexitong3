-- 器材管理系统数据库初始化脚本
-- 注意：只有校级管理员(school_admin)和超级管理员(super_admin)有权限管理器材
-- 借阅者信息都从users表中获取

-- 1. 器材分类表
CREATE TABLE IF NOT EXISTS equipment_categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    icon VARCHAR(10) COMMENT '分类图标',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序字段',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_category_name (name),
    KEY idx_category_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='器材分类表';

-- 2. 器材库存表
CREATE TABLE IF NOT EXISTS equipment_items (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '器材名称',
    model VARCHAR(30) COMMENT '型号',
    specification VARCHAR(200) COMMENT '规格描述',
    total_quantity INT NOT NULL COMMENT '总数量',
    available_quantity INT NOT NULL COMMENT '可用数量',
    borrowed_quantity INT NOT NULL DEFAULT 0 COMMENT '借出数量',
    damaged_quantity INT NOT NULL DEFAULT 0 COMMENT '损坏数量',
    unit_price DECIMAL(10,2) COMMENT '单价',
    purchase_date DATE COMMENT '购买日期',
    warranty_period INT COMMENT '保修期（月）',
    storage_location VARCHAR(50) COMMENT '存放位置',
    created_by VARCHAR(36) COMMENT '创建人ID',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    KEY idx_equipment_category (category_id),
    KEY idx_equipment_name (name),
    KEY idx_equipment_created_by (created_by),
    KEY idx_equipment_deleted (is_deleted),
    
    CONSTRAINT fk_equipment_category FOREIGN KEY (category_id) REFERENCES equipment_categories(id),
    CONSTRAINT chk_equipment_quantities CHECK (
        total_quantity >= 0 AND 
        available_quantity >= 0 AND 
        borrowed_quantity >= 0 AND 
        damaged_quantity >= 0 AND
        total_quantity = available_quantity + borrowed_quantity + damaged_quantity
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='器材库存表';

-- 3. 器材借用申请表
CREATE TABLE IF NOT EXISTS equipment_applications (
    id VARCHAR(36) PRIMARY KEY,
    equipment_id VARCHAR(36) NOT NULL COMMENT '器材ID',
    borrower_id VARCHAR(36) NOT NULL COMMENT '借用人ID（users1表）',
    quantity INT NOT NULL COMMENT '借用数量',
    purpose VARCHAR(200) NOT NULL COMMENT '借用目的',
    borrow_date TIMESTAMP NOT NULL COMMENT '借用开始时间',
    expected_return_date TIMESTAMP NOT NULL COMMENT '预期归还时间',
    actual_return_date TIMESTAMP NULL COMMENT '实际归还时间',
    status ENUM('pending', 'approved', 'rejected', 'returned') NOT NULL DEFAULT 'pending' COMMENT '申请状态',
    remark VARCHAR(200) COMMENT '备注',
    approved_by VARCHAR(36) COMMENT '审批人ID',
    approved_at TIMESTAMP NULL COMMENT '审批时间',
    actual_quantity INT COMMENT '实际归还数量',
    return_condition ENUM('good', 'damaged', 'lost') COMMENT '归还状态',
    returned_by VARCHAR(36) COMMENT '归还处理人ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    KEY idx_application_equipment (equipment_id),
    KEY idx_application_borrower (borrower_id),
    KEY idx_application_status (status),
    KEY idx_application_date (borrow_date),
    KEY idx_application_created (created_at),
    
    CONSTRAINT fk_application_equipment FOREIGN KEY (equipment_id) REFERENCES equipment_items(id),
    CONSTRAINT fk_application_borrower FOREIGN KEY (borrower_id) REFERENCES users1(id),
    CONSTRAINT fk_application_approver FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_application_returner FOREIGN KEY (returned_by) REFERENCES users(id),
    CONSTRAINT chk_application_quantity CHECK (quantity > 0),
    CONSTRAINT chk_application_dates CHECK (expected_return_date > borrow_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='器材借用申请表';

-- 4. 器材库存调整记录表
CREATE TABLE IF NOT EXISTS equipment_adjustments (
    id VARCHAR(36) PRIMARY KEY,
    equipment_id VARCHAR(36) NOT NULL COMMENT '器材ID',
    type ENUM('increase', 'decrease', 'damage') NOT NULL COMMENT '调整类型',
    quantity INT NOT NULL COMMENT '调整数量',
    before_quantity INT NOT NULL COMMENT '调整前数量',
    after_quantity INT NOT NULL COMMENT '调整后数量',
    reason VARCHAR(200) NOT NULL COMMENT '调整原因',
    operator VARCHAR(36) NOT NULL COMMENT '操作人ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    KEY idx_adjustment_equipment (equipment_id),
    KEY idx_adjustment_operator (operator),
    KEY idx_adjustment_type (type),
    KEY idx_adjustment_created (created_at),
    
    CONSTRAINT fk_adjustment_equipment FOREIGN KEY (equipment_id) REFERENCES equipment_items(id),
    CONSTRAINT fk_adjustment_operator FOREIGN KEY (operator) REFERENCES users(id),
    CONSTRAINT chk_adjustment_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='器材库存调整记录表';

-- 插入初始器材分类数据
INSERT INTO equipment_categories (id, name, description, icon, sort) VALUES 
('cat001', '球类器材', '各种体育球类器材', '⚽', 1),
('cat002', '健身器材', '健身和力量训练器材', '💪', 2),
('cat003', '球拍器材', '各种球拍类器材', '🏸', 3),
('cat004', '田径器材', '田径运动相关器材', '🏃', 4),
('cat005', '游泳器材', '游泳训练和比赛器材', '🏊', 5),
('cat006', '体操器材', '体操训练器材', '🤸', 6),
('cat007', '其他器材', '其他体育器材', '⚙️', 99);

-- 插入示例器材数据
INSERT INTO equipment_items (
    id, category_id, name, model, specification, 
    total_quantity, available_quantity, unit_price, 
    purchase_date, warranty_period, storage_location, created_by
) VALUES 
('eq001', 'cat001', '标准足球', 'Nike Premier League', '5号球，FIFA认证标准', 
 50, 50, 89.99, '2024-01-15', 12, '器材室A区1号柜', 'admin'),
('eq002', 'cat001', '标准篮球', 'Nike Elite', '7号球，PU皮面', 
 30, 30, 120.00, '2024-01-20', 24, '器材室A区2号柜', 'admin'),
('eq003', 'cat003', '羽毛球拍', 'Yonex Arc 11', '专业级羽毛球拍', 
 20, 20, 299.00, '2024-02-01', 36, '器材室B区1号柜', 'admin'),
('eq004', 'cat002', '哑铃', '5KG哑铃', '铸铁哑铃，5公斤', 
 40, 40, 45.00, '2024-02-10', 60, '器材室C区', 'admin'),
('eq005', 'cat004', '铅球', '标准铅球', '7.26公斤，比赛用', 
 10, 10, 150.00, '2024-02-15', 120, '器材室D区', 'admin');

-- 创建视图：器材库存统计
CREATE OR REPLACE VIEW v_equipment_inventory_summary AS
SELECT 
    ec.name AS category_name,
    COUNT(ei.id) AS equipment_count,
    SUM(ei.total_quantity) AS total_quantity,
    SUM(ei.available_quantity) AS available_quantity,
    SUM(ei.borrowed_quantity) AS borrowed_quantity,
    SUM(ei.damaged_quantity) AS damaged_quantity
FROM equipment_categories ec
LEFT JOIN equipment_items ei ON ec.id = ei.category_id AND ei.is_deleted = FALSE
GROUP BY ec.id, ec.name
ORDER BY ec.sort;

-- 创建视图：借用申请统计
CREATE OR REPLACE VIEW v_application_statistics AS
SELECT 
    DATE(created_at) AS application_date,
    status,
    COUNT(*) AS application_count,
    SUM(quantity) AS total_quantity
FROM equipment_applications
WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
GROUP BY DATE(created_at), status
ORDER BY application_date DESC, status;

-- 创建视图：热门器材排行
CREATE OR REPLACE VIEW v_popular_equipment AS
SELECT 
    ei.id AS equipment_id,
    ei.name AS equipment_name,
    ei.model AS equipment_model,
    ec.name AS category_name,
    COUNT(ea.id) AS borrow_count,
    SUM(ea.quantity) AS total_borrowed_quantity
FROM equipment_items ei
LEFT JOIN equipment_categories ec ON ei.category_id = ec.id
LEFT JOIN equipment_applications ea ON ei.id = ea.equipment_id 
    AND ea.status = 'approved'
WHERE ei.is_deleted = FALSE
GROUP BY ei.id, ei.name, ei.model, ec.name
ORDER BY borrow_count DESC, total_borrowed_quantity DESC
LIMIT 20;

-- 注意：以下触发器和存储过程需要在MySQL命令行中单独执行
-- 如果使用图形化工具，建议将触发器和存储过程部分单独执行

-- 创建触发器：自动更新器材库存（需要在MySQL命令行中执行）
/*
DELIMITER //

CREATE TRIGGER tr_application_approved
AFTER UPDATE ON equipment_applications
FOR EACH ROW
BEGIN
    -- 当申请状态从 pending 变为 approved 时，更新器材库存
    IF OLD.status = 'pending' AND NEW.status = 'approved' THEN
        UPDATE equipment_items 
        SET borrowed_quantity = borrowed_quantity + NEW.quantity,
            available_quantity = available_quantity - NEW.quantity
        WHERE id = NEW.equipment_id;
    END IF;
    
    -- 当申请状态从 approved 变为 rejected 时，恢复器材库存
    IF OLD.status = 'approved' AND NEW.status = 'rejected' THEN
        UPDATE equipment_items 
        SET borrowed_quantity = borrowed_quantity - NEW.quantity,
            available_quantity = available_quantity + NEW.quantity
        WHERE id = NEW.equipment_id;
    END IF;
END //

CREATE TRIGGER tr_application_returned
AFTER UPDATE ON equipment_applications
FOR EACH ROW
BEGIN
    -- 当器材归还时，更新库存
    IF OLD.status = 'approved' AND NEW.status = 'returned' AND NEW.actual_quantity IS NOT NULL THEN
        UPDATE equipment_items 
        SET borrowed_quantity = borrowed_quantity - NEW.actual_quantity,
            available_quantity = CASE 
                WHEN NEW.return_condition = 'good' THEN available_quantity + NEW.actual_quantity
                ELSE available_quantity
            END,
            damaged_quantity = CASE 
                WHEN NEW.return_condition = 'damaged' THEN damaged_quantity + NEW.actual_quantity
                ELSE damaged_quantity
            END
        WHERE id = NEW.equipment_id;
    END IF;
END //

DELIMITER ;
*/

-- 创建存储过程：清理过期申请（需要在MySQL命令行中执行）
/*
DELIMITER //

CREATE PROCEDURE sp_cleanup_expired_applications()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE app_id VARCHAR(36);
    DECLARE app_quantity INT;
    DECLARE app_equipment_id VARCHAR(36);
    
    DECLARE cur CURSOR FOR 
        SELECT id, quantity, equipment_id 
        FROM equipment_applications 
        WHERE status = 'approved' 
        AND actual_return_date IS NULL 
        AND expected_return_date < DATE_SUB(NOW(), INTERVAL 7 DAY);
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO app_id, app_quantity, app_equipment_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 标记为超期
        UPDATE equipment_applications 
        SET remark = CONCAT(IFNULL(remark, ''), ' [系统标记：超期未归还]')
        WHERE id = app_id;
        
    END LOOP;
    
    CLOSE cur;
END //

DELIMITER ;
*/

-- 创建定期任务事件（需要事件调度器开启）
-- SET GLOBAL event_scheduler = ON;

-- CREATE EVENT IF NOT EXISTS ev_cleanup_expired_applications
-- ON SCHEDULE EVERY 1 DAY
-- DO
--   CALL sp_cleanup_expired_applications();

-- 注释说明
/*
器材管理系统数据库设计说明：

1. 权限控制：
   - 只有校级管理员(school_admin)和超级管理员(super_admin)有器材管理权限
   - 只有超级管理员有删除器材权限
   - 借阅者信息都从users1表中获取

2. 核心功能：
   - 器材分类管理
   - 器材库存管理（增删改查、库存调整）
   - 借用申请管理（审批、归还）
   - 统计信息（库存统计、借用统计、热门器材）

3. 数据完整性：
   - 外键约束保证数据一致性
   - 检查约束保证数据有效性
   - 触发器自动维护库存数据
   - 视图提供便捷的统计查询

4. 性能优化：
   - 合理的索引设计
   - 分页查询支持
   - 软删除机制
   - 视图缓存常用统计

5. 扩展性：
   - 支持器材分类层次化扩展
   - 支持更多器材属性扩展
   - 支持工作流扩展
*/
