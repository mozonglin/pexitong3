# 器材借阅管理系统 API 使用说明

## 概述

器材借阅管理系统为校级管理员和超级管理员提供完整的器材管理功能，包括器材库存管理、借用申请审批、库存调整等功能。[[memory:5122586]]

### 权限说明

- **校级管理员 (school_admin)**: 可以管理器材、审批申请、处理归还
- **超级管理员 (super_admin)**: 拥有所有权限，包括删除器材
- **借阅者信息**: 都存储在 `users1` 表中（PE用户表），包含学生姓名、学号、联系方式等信息

### 基础信息

- **API 前缀**: `/api/equipment`
- **认证方式**: Bearer Token (JWT)
- **数据库**: 使用主数据库 `pexitong3`

## API 端点列表

### 1. 器材分类管理

#### 1.1 获取器材分类列表
```http
GET /api/equipment/categories
Authorization: Bearer <jwt-token>
```

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "cat001",
      "name": "球类器材",
      "description": "各种体育球类器材",
      "icon": "⚽",
      "sort": 1,
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ]
}
```

### 2. 器材库存管理

#### 2.1 获取器材列表
```http
GET /api/equipment/items?page=1&limit=10&category_id=cat001&keyword=足球&status=available
Authorization: Bearer <jwt-token>
```

**查询参数:**
- `page`: 页码 (默认: 1)
- `limit`: 每页数量 (默认: 10, 最大: 100)
- `category_id`: 分类ID筛选
- `keyword`: 关键词搜索 (器材名称、型号)
- `status`: 状态筛选 (all/available/shortage)

#### 2.2 添加器材
```http
POST /api/equipment/items
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "categoryId": "cat001",
  "name": "标准足球",
  "model": "Nike Premier League",
  "specification": "5号球，FIFA认证标准",
  "totalQuantity": 50,
  "unitPrice": 89.99,
  "purchaseDate": "2024-01-15",
  "warrantyPeriod": 12,
  "storageLocation": "器材室A区1号柜"
}
```

#### 2.3 更新器材信息
```http
PUT /api/equipment/items/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "高级足球",
  "model": "Nike Elite Pro",
  "unitPrice": 150.00,
  "storageLocation": "器材室A区2号柜"
}
```

#### 2.4 删除器材 (仅超级管理员)
```http
DELETE /api/equipment/items/{id}
Authorization: Bearer <jwt-token>
```

#### 2.5 库存调整
```http
POST /api/equipment/items/{id}/adjust
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "type": "increase",
  "quantity": 10,
  "reason": "采购新批次器材"
}
```

**调整类型:**
- `increase`: 增加库存
- `decrease`: 减少库存  
- `damage`: 损坏报废

### 3. 借用申请管理

#### 3.1 获取借用申请列表
```http
GET /api/equipment/applications?status=pending&page=1&limit=10
Authorization: Bearer <jwt-token>
```

**查询参数:**
- `status`: 状态筛选 (pending/approved/rejected/returned)
- `equipment_id`: 器材ID筛选
- `borrower_id`: 借用人ID筛选
- `date_from`: 开始日期 (YYYY-MM-DD)
- `date_to`: 结束日期 (YYYY-MM-DD)

#### 3.2 审批借用申请
```http
POST /api/equipment/applications/{id}/approve
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "status": "approved",
  "remark": "申请已通过，请按时归还"
}
```

**审批状态:**
- `approved`: 同意
- `rejected`: 拒绝

#### 3.3 器材归还
```http
POST /api/equipment/applications/{id}/return
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "actualQuantity": 5,
  "condition": "good",
  "remark": "器材状态良好，已全部归还"
}
```

**归还状态:**
- `good`: 状态良好
- `damaged`: 有损坏
- `lost`: 丢失

### 4. 统计信息

#### 4.1 获取器材统计
```http
GET /api/equipment/statistics?period=month
Authorization: Bearer <jwt-token>
```

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "equipmentSummary": {
      "totalItems": 150,
      "totalCategories": 8,
      "availableItems": 120,
      "borrowedItems": 25,
      "damagedItems": 5
    },
    "applicationSummary": {
      "pendingApplications": 3,
      "todayApplications": 8,
      "weekApplications": 45,
      "monthApplications": 180
    },
    "popularEquipment": [
      {
        "equipmentId": "eq001",
        "equipmentName": "标准足球",
        "borrowCount": 25,
        "categoryName": "球类器材"
      }
    ]
  }
}
```

## 数据库设计

### 核心表结构

1. **equipment_categories** - 器材分类表
2. **equipment_items** - 器材库存表
3. **equipment_applications** - 借用申请表
4. **equipment_adjustments** - 库存调整记录表

### 视图和存储过程

- `v_equipment_inventory_summary` - 器材库存统计视图
- `v_application_statistics` - 借用申请统计视图
- `v_popular_equipment` - 热门器材排行视图
- `sp_cleanup_expired_applications()` - 清理过期申请存储过程

## 业务逻辑

### 库存管理
- 总数量 = 可用数量 + 借出数量 + 损坏数量
- 每次借用/归还都自动更新库存
- 支持库存调整操作记录

### 申请审批流程
1. 用户提交借用申请
2. 管理员审批 (检查库存是否充足)
3. 审批通过后自动扣减库存
4. 器材归还时更新库存状态

### 权限控制
- 校级管理员和超级管理员可以管理器材
- 只有超级管理员可以删除器材
- 所有操作都记录操作人信息

## 错误处理

常见错误码:
- `400`: 请求参数错误
- `401`: 未授权 (Token无效)
- `403`: 权限不足
- `404`: 资源不存在
- `422`: 业务逻辑错误 (如库存不足)

错误响应格式:
```json
{
  "code": 422,
  "message": "库存不足，当前可用数量：5，申请数量：10",
  "data": null
}
```

## 测试示例

### 完整流程测试

1. **获取器材列表**
```bash
curl -X GET "http://localhost:8080/api/equipment/items" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

2. **添加器材**
```bash
curl -X POST "http://localhost:8080/api/equipment/items" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "cat001",
    "name": "标准足球",
    "totalQuantity": 20,
    "unitPrice": 89.99
  }'
```

3. **审批申请**
```bash
curl -X POST "http://localhost:8080/api/equipment/applications/app001/approve" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "approved",
    "remark": "申请已通过"
  }'
```

4. **器材归还**
```bash
curl -X POST "http://localhost:8080/api/equipment/applications/app001/return" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "actualQuantity": 5,
    "condition": "good",
    "remark": "器材状态良好"
  }'
```

## 部署说明

1. **运行SQL初始化脚本**

**选项1：使用基础脚本（推荐）**
```bash
mysql -u username -p database_name < equipment_management_basic.sql
```

**选项2：如果需要触发器，在MySQL命令行中执行**
```bash
mysql -u username -p database_name
# 然后逐个执行触发器创建语句
```

2. **确保数据库连接配置正确**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/pexitong3
```

3. **验证权限配置**
- 确保users表中有校级管理员和超级管理员用户
- 确保users1表中有学生用户信息用于借阅
- 验证JWT Token生成和验证正常工作

## 注意事项

1. **权限验证**: 每个API都会验证用户权限
2. **数据一致性**: 使用事务保证数据一致性
3. **库存自动管理**: 借用和归还操作会自动更新库存
4. **软删除**: 器材删除使用软删除机制
5. **审计追踪**: 所有操作都记录操作人和时间

## 扩展功能

系统支持以下扩展:
- 器材二维码管理
- 借用提醒和催还
- 器材保养记录
- 更详细的统计报表
- 移动端APP支持
