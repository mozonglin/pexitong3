# PE校园管理统计功能 - 部署和使用说明

## 功能概述

PE校园管理统计功能为校级管理员和院级管理员提供了完整的PE积分统计和分析能力：

### 校级管理员功能
- 设置本校PE积分指标（周、月、总指标）
- 查看全校学生PE积分达标率
- 查看各院系达标率和排名
- 查看学生总数和各院人数

### 院级管理员功能  
- 查看本院整体达标率
- 查看本院各班达标率和排名
- 基于校级管理员设置的指标进行统计

### 校级管理员额外权限
- 访问院系统计接口时，可查看全校所有院系的所有班级数据
- 获得跨院系的班级达标率排名

## 数据库变更

### 新增字段

#### users表（管理员表）
```sql
ALTER TABLE users ADD COLUMN weekly_target int(11) DEFAULT NULL COMMENT '周PE积分指标';
ALTER TABLE users ADD COLUMN monthly_target int(11) DEFAULT NULL COMMENT '月PE积分指标';  
ALTER TABLE users ADD COLUMN total_target int(11) DEFAULT NULL COMMENT '总PE积分指标';
```

#### users1表（学生表）
```sql
ALTER TABLE users1 ADD COLUMN class_name varchar(100) DEFAULT NULL COMMENT '班级名称';
```

## 部署步骤

### 1. 数据库更新
```bash
# 执行数据库更新脚本
mysql -u username -p database_name < pe_database_update.sql
```

### 2. 代码部署
新增的文件：
- `src/main/java/com/example/pexitong2/dto/pe/PeTargetRequest.java`
- `src/main/java/com/example/pexitong2/dto/pe/SchoolStatisticsResponse.java`
- `src/main/java/com/example/pexitong2/dto/pe/CollegeStatisticsResponse.java`
- `src/main/java/com/example/pexitong2/service/pe/PeStatisticsService.java`
- `src/main/java/com/example/pexitong2/controller/pe/PeStatisticsController.java`

修改的文件：
- `src/main/java/com/example/pexitong2/entity/User.java` - 添加PE指标字段
- `src/main/java/com/example/pexitong2/entity/pe/PeUser.java` - 添加班级字段
- `src/main/java/com/example/pexitong2/repository/UserRepository.java` - 添加查询方法
- `src/main/java/com/example/pexitong2/repository/pe/PeUserRepository.java` - 添加查询方法

### 3. 重新编译和启动
```bash
# 重新编译项目
./gradlew build

# 启动应用
./gradlew bootRun
```

## API接口地址

- **设置PE积分指标**: `POST /pe/admin/statistics/targets`
- **获取学校统计**: `GET /pe/admin/statistics/school`  
- **获取院系统计**: `GET /pe/admin/statistics/college`

详细API文档请参考 `pe-statistics-api.md`

## 权限配置

### 角色映射
- 校级管理员：`user_type = 'school_admin'`
- 院级管理员：`user_type = 'department_admin'`

### 数据关联
- 管理员表的`department_name`字段对应院系名称
- 学生表的`college`字段对应院系名称
- 学生表的`class_name`字段对应班级名称

## 测试验证

### 1. 使用测试脚本
```bash
# Linux/Mac
./test-pe-statistics-api.sh

# Windows  
test-pe-statistics-api.bat
```

### 2. 手动测试步骤

#### 步骤1：校级管理员设置指标
```bash
curl -X POST "http://localhost:8080/pe/admin/statistics/targets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <校级管理员JWT>" \
  -d '{
    "weeklyTarget": 10,
    "monthlyTarget": 40, 
    "totalTarget": 100
  }'
```

#### 步骤2：校级管理员查看统计
```bash
curl -X GET "http://localhost:8080/pe/admin/statistics/school" \
  -H "Authorization: Bearer <校级管理员JWT>"
```

#### 步骤3：院级管理员查看统计  
```bash
curl -X GET "http://localhost:8080/pe/admin/statistics/college" \
  -H "Authorization: Bearer <院级管理员JWT>"
```

## 数据准备

### 测试数据建议

#### 1. 管理员数据
```sql
-- 校级管理员
INSERT INTO users (id, username, password_hash, real_name, student_id, user_type, school, department_name) 
VALUES ('admin_school_001', 'school_admin', '$2a$10$...', '张校长', 'ADM001', 'school_admin', '北京体育大学', NULL);

-- 院级管理员  
INSERT INTO users (id, username, password_hash, real_name, student_id, user_type, school, department_name)
VALUES ('admin_dept_001', 'dept_admin', '$2a$10$...', '李院长', 'ADM002', 'department_admin', '北京体育大学', '体育教育学院');
```

#### 2. 学生数据
```sql
-- 学生数据（需要包含school, college, class_name, points字段）
INSERT INTO users1 (id, name, student_id, school, college, class_name, phone_number, points) 
VALUES 
('stu_001', '张三', '2021001', '北京体育大学', '体育教育学院', '体教2021级1班', '13800138001', 85),
('stu_002', '李四', '2021002', '北京体育大学', '体育教育学院', '体教2021级1班', '13800138002', 92),
('stu_003', '王五', '2021003', '北京体育大学', '运动训练学院', '运训2021级1班', '13800138003', 78);
```

## 注意事项

### 1. 权限控制
- 院级管理员只能查看本院数据（基于`department_name`匹配`college`）
- 校级管理员可以查看全校数据
- 只有校级管理员可以设置PE积分指标

### 2. 数据一致性
- 确保管理员的`department_name`与学生的`college`字段值一致
- 学生的`class_name`字段用于班级统计，建议统一命名规范

### 3. 达标率计算
- 基于学生账户创建时间估算周/月积分
- 实际生产环境中可根据需求优化计算逻辑
- 综合达标率取三项（周、月、总）的平均值

### 4. 性能考虑
- 大量学生数据时建议添加数据库索引：
```sql
ALTER TABLE users1 ADD INDEX idx_school_college (school, college);
ALTER TABLE users1 ADD INDEX idx_school_college_class (school, college, class_name);
ALTER TABLE users1 ADD INDEX idx_points (points);
```

## 故障排除

### 常见问题

#### 1. 权限不足错误
- 检查JWT Token是否有效
- 确认用户角色是否正确
- 验证用户是否属于正确的学校/院系

#### 2. 未设置指标错误
- 确保校级管理员已设置PE积分指标
- 检查数据库中users表的target字段是否有值

#### 3. 数据为空
- 检查学生数据是否正确导入
- 验证school、college、class_name字段是否填写
- 确认字段映射关系是否正确

#### 4. 达标率异常
- 检查学生积分数据是否正确
- 验证创建时间字段是否有效
- 确认指标设置是否合理

## 维护建议

1. **定期备份**：部署前备份数据库
2. **监控日志**：关注应用日志中的错误信息
3. **性能监控**：监控统计接口的响应时间
4. **数据校验**：定期检查数据一致性
5. **权限审计**：定期审查管理员权限设置

## 技术支持

如遇到问题，请检查：
1. 应用日志文件
2. 数据库连接状态
3. JWT Token配置
4. 网络连接情况

详细的API文档和错误码说明请参考 `pe-statistics-api.md` 文件。
