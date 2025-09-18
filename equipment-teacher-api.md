# 器材教师端 API 文档

## 概述

器材教师端API为体育教师提供器材管理功能，包括器材库存管理、借用申请审批、库存调整等核心功能。

### 基础信息

- **API版本**: v1.0
- **基础URL**: `http://your-domain/api`
- **认证方式**: Bearer Token
- **内容类型**: application/json

### 认证说明

所有API请求都需要在Header中携带JWT Token：

```http
Authorization: Bearer <your-jwt-token>
```

---

## 1. 器材分类管理

### 1.1 获取器材分类列表

获取所有器材分类信息，用于分类筛选和器材添加时的分类选择。

**请求信息**
- **URL**: `/equipment/categories`
- **方法**: `GET`
- **权限**: 需要教师或管理员权限

**请求参数**
无

**响应示例**
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
    },
    {
      "id": "cat002", 
      "name": "健身器材",
      "description": "健身和力量训练器材",
      "icon": "💪",
      "sort": 2,
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ]
}
```

---

## 2. 器材库存管理

### 2.1 获取器材列表

获取器材库存列表，支持分页、搜索和分类筛选。

**请求信息**
- **URL**: `/equipment/items`
- **方法**: `GET`
- **权限**: 需要教师或管理员权限

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | 否 | 页码，默认1 |
| limit | int | 否 | 每页数量，默认10，最大100 |
| category_id | string | 否 | 分类ID筛选 |
| keyword | string | 否 | 关键词搜索（器材名称、型号） |
| status | string | 否 | 状态筛选：all/available/shortage |

**请求示例**
```http
GET /equipment/items?page=1&limit=10&category_id=cat001&keyword=足球
```

**响应示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "id": "eq001",
        "category_id": "cat001",
        "category_name": "球类器材",
        "name": "标准足球",
        "model": "Nike Premier League",
        "specification": "5号球，FIFA认证标准",
        "total_quantity": 50,
        "available_quantity": 35,
        "borrowed_quantity": 12,
        "damaged_quantity": 3,
        "unit_price": 89.99,
        "purchase_date": "2024-01-15",
        "warranty_period": 12,
        "storage_location": "器材室A区1号柜",
        "created_by": "teacher001",
        "createdAt": "2024-01-15T08:00:00Z",
        "updatedAt": "2024-03-01T14:30:00Z"
      }
    ],
    "pagination": {
      "total": 1,
      "page": 1,
      "limit": 10,
      "pages": 1
    }
  }
}
```

### 2.2 添加器材

添加新的器材到库存中。

**请求信息**
- **URL**: `/equipment/items`
- **方法**: `POST`
- **权限**: 需要教师或管理员权限

**请求体**
```json
{
  "category_id": "cat001",
  "name": "标准篮球",
  "model": "Nike Elite",
  "specification": "7号球，PU皮面",
  "total_quantity": 30,
  "unit_price": 120.00,
  "purchase_date": "2024-03-01",
  "warranty_period": 24,
  "storage_location": "器材室B区2号柜"
}
```

**字段说明**
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category_id | string | 是 | 器材分类ID |
| name | string | 是 | 器材名称，最大50字符 |
| model | string | 否 | 型号，最大30字符 |
| specification | string | 否 | 规格描述，最大200字符 |
| total_quantity | int | 是 | 总数量，必须大于0 |
| unit_price | float | 否 | 单价（元） |
| purchase_date | string | 否 | 购买日期，格式：YYYY-MM-DD |
| warranty_period | int | 否 | 保修期（月） |
| storage_location | string | 否 | 存放位置，最大50字符 |

**响应示例**
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": "eq002",
    "category_id": "cat001",
    "name": "标准篮球",
    "total_quantity": 30,
    "available_quantity": 30,
    "borrowed_quantity": 0,
    "damaged_quantity": 0,
    "createdAt": "2024-03-01T10:00:00Z"
  }
}
```

### 2.3 更新器材信息

更新指定器材的基本信息。

**请求信息**
- **URL**: `/equipment/items/{id}`
- **方法**: `PUT`
- **权限**: 需要教师或管理员权限

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 器材ID |

**请求体**
```json
{
  "name": "高级篮球",
  "model": "Nike Elite Pro",
  "specification": "7号球，真皮面",
  "unit_price": 150.00,
  "storage_location": "器材室B区3号柜"
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "eq002",
    "name": "高级篮球",
    "model": "Nike Elite Pro",
    "updatedAt": "2024-03-01T15:00:00Z"
  }
}
```

### 2.4 删除器材

删除指定器材（仅当没有借用记录时可删除）。

**请求信息**
- **URL**: `/equipment/items/{id}`
- **方法**: `DELETE`
- **权限**: 需要管理员权限

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 器材ID |

**响应示例**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 2.5 库存调整

对器材库存进行调整，包括增加、减少、损坏报废等操作。

**请求信息**
- **URL**: `/equipment/items/{id}/adjust`
- **方法**: `POST`
- **权限**: 需要教师或管理员权限

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 器材ID |

**请求体**
```json
{
  "type": "increase",
  "quantity": 10,
  "reason": "采购新批次器材"
}
```

**字段说明**
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | string | 是 | 调整类型：increase(增加)/decrease(减少)/damage(损坏) |
| quantity | int | 是 | 调整数量，必须大于0 |
| reason | string | 是 | 调整原因，最大200字符 |

**响应示例**
```json
{
  "code": 200,
  "message": "库存调整成功",
  "data": {
    "equipment_id": "eq001",
    "adjustment_id": "adj001",
    "type": "increase",
    "quantity": 10,
    "before_quantity": 50,
    "after_quantity": 60,
    "reason": "采购新批次器材",
    "operator": "teacher001",
    "createdAt": "2024-03-01T16:00:00Z"
  }
}
```

---

## 3. 借用申请管理

### 3.1 获取借用申请列表

获取器材借用申请列表，支持状态筛选和分页。

**请求信息**
- **URL**: `/equipment/applications`
- **方法**: `GET`
- **权限**: 需要教师或管理员权限

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | 否 | 页码，默认1 |
| limit | int | 否 | 每页数量，默认10 |
| status | string | 否 | 状态筛选：pending/approved/rejected/returned |
| equipment_id | string | 否 | 器材ID筛选 |
| borrower_id | string | 否 | 借用人ID筛选 |
| date_from | string | 否 | 开始日期，格式：YYYY-MM-DD |
| date_to | string | 否 | 结束日期，格式：YYYY-MM-DD |

**请求示例**
```http
GET /equipment/applications?status=pending&page=1&limit=10
```

**响应示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "id": "app001",
        "equipment_id": "eq001",
        "equipment_name": "标准足球",
        "equipment_model": "Nike Premier League",
        "borrower_id": "student001",
        "borrower_name": "张三",
        "borrower_type": "student",
        "borrower_contact": "13800138001",
        "quantity": 5,
        "purpose": "体育课教学使用",
        "borrow_date": "2024-03-02T08:00:00Z",
        "expected_return_date": "2024-03-02T18:00:00Z",
        "actual_return_date": null,
        "status": "pending",
        "remark": "",
        "approved_by": null,
        "approved_at": null,
        "createdAt": "2024-03-01T10:00:00Z",
        "updatedAt": "2024-03-01T10:00:00Z"
      }
    ],
    "pagination": {
      "total": 1,
      "page": 1,
      "limit": 10,
      "pages": 1
    }
  }
}
```

### 3.2 审批借用申请

审批（同意或拒绝）借用申请。

**请求信息**
- **URL**: `/equipment/applications/{id}/approve`
- **方法**: `POST`
- **权限**: 需要教师或管理员权限

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 申请ID |

**请求体**
```json
{
  "status": "approved",
  "remark": "申请已通过，请按时归还"
}
```

**字段说明**
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | string | 是 | 审批结果：approved(同意)/rejected(拒绝) |
| remark | string | 否 | 审批备注，最大200字符 |

**响应示例**
```json
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "application_id": "app001",
    "status": "approved",
    "approved_by": "teacher001",
    "approved_at": "2024-03-01T14:00:00Z",
    "remark": "申请已通过，请按时归还"
  }
}
```

### 3.3 器材归还

处理器材归还，更新申请状态和库存。

**请求信息**
- **URL**: `/equipment/applications/{id}/return`
- **方法**: `POST`
- **权限**: 需要教师或管理员权限

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 申请ID |

**请求体**
```json
{
  "actual_quantity": 5,
  "condition": "good",
  "remark": "器材状态良好，已全部归还"
}
```

**字段说明**
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| actual_quantity | int | 是 | 实际归还数量 |
| condition | string | 是 | 器材状态：good(良好)/damaged(损坏)/lost(丢失) |
| remark | string | 否 | 归还备注，最大200字符 |

**响应示例**
```json
{
  "code": 200,
  "message": "归还成功",
  "data": {
    "application_id": "app001",
    "status": "returned",
    "actual_return_date": "2024-03-02T17:30:00Z",
    "actual_quantity": 5,
    "condition": "good",
    "returned_by": "teacher001"
  }
}
```

---

## 4. 统计信息

### 4.1 获取器材统计

获取器材管理相关的统计信息。

**请求信息**
- **URL**: `/equipment/statistics`
- **方法**: `GET`
- **权限**: 需要教师或管理员权限

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| period | string | 否 | 统计周期：today/week/month/year |

**响应示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "equipment_summary": {
      "total_items": 150,
      "total_categories": 8,
      "available_items": 120,
      "borrowed_items": 25,
      "damaged_items": 5
    },
    "application_summary": {
      "pending_applications": 3,
      "today_applications": 8,
      "week_applications": 45,
      "month_applications": 180
    },
    "popular_equipment": [
      {
        "equipment_id": "eq001",
        "equipment_name": "标准足球",
        "borrow_count": 25,
        "category_name": "球类器材"
      },
      {
        "equipment_id": "eq003",
        "equipment_name": "羽毛球拍",
        "borrow_count": 18,
        "category_name": "球拍器材"
      }
    ]
  }
}
```

---

## 5. 错误处理

### 5.1 错误响应格式

所有错误响应都遵循统一格式：

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "errors": [
    {
      "field": "category_id",
      "message": "器材分类不能为空"
    }
  ]
}
```

### 5.2 常见错误码

| 错误码 | 说明 | 常见原因 |
|--------|------|----------|
| 400 | 请求参数错误 | 参数格式不正确、必填参数缺失 |
| 401 | 未授权 | Token无效或已过期 |
| 403 | 权限不足 | 用户权限不够 |
| 404 | 资源不存在 | 器材或申请不存在 |
| 409 | 资源冲突 | 库存不足、重复操作等 |
| 422 | 业务逻辑错误 | 状态不允许、数据约束等 |
| 500 | 服务器内部错误 | 系统异常 |

### 5.3 业务错误示例

#### 库存不足
```json
{
  "code": 422,
  "message": "库存不足",
  "data": {
    "equipment_id": "eq001",
    "requested_quantity": 10,
    "available_quantity": 5
  }
}
```

#### 申请状态不允许操作
```json
{
  "code": 422,
  "message": "申请状态不允许此操作",
  "data": {
    "application_id": "app001",
    "current_status": "approved",
    "required_status": "pending"
  }
}
```

---

## 6. 数据模型

### 6.1 器材分类模型
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "icon": "string",
  "sort": "integer",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### 6.2 器材模型
```json
{
  "id": "string",
  "category_id": "string",
  "category_name": "string",
  "name": "string",
  "model": "string",
  "specification": "string",
  "total_quantity": "integer",
  "available_quantity": "integer",
  "borrowed_quantity": "integer",
  "damaged_quantity": "integer",
  "unit_price": "float",
  "purchase_date": "date",
  "warranty_period": "integer",
  "storage_location": "string",
  "created_by": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### 6.3 借用申请模型
```json
{
  "id": "string",
  "equipment_id": "string",
  "equipment_name": "string",
  "borrower_id": "string",
  "borrower_name": "string",
  "borrower_type": "enum[student,teacher]",
  "borrower_contact": "string",
  "quantity": "integer",
  "purpose": "string",
  "borrow_date": "datetime",
  "expected_return_date": "datetime",
  "actual_return_date": "datetime",
  "status": "enum[pending,approved,rejected,returned]",
  "remark": "string",
  "approved_by": "string",
  "approved_at": "datetime",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

---

## 7. 开发注意事项

### 7.1 数据库设计建议

1. **器材分类表** (`equipment_categories`)
   - 支持层级分类（parent_id字段）
   - 添加排序字段便于前端展示

2. **器材表** (`equipment_items`)
   - 使用UUID作为主键
   - 添加软删除标记
   - 建立分类外键关联

3. **借用申请表** (`equipment_applications`) 
   - 建立器材和用户的外键关联
   - 添加状态字段索引
   - 记录完整的审批流程

4. **库存调整记录表** (`equipment_adjustments`)
   - 记录所有库存变动
   - 便于审计和追溯

### 7.2 业务逻辑要点

1. **库存管理**
   - 总数量 = 可用数量 + 借出数量 + 损坏数量
   - 每次借用/归还都要更新库存
   - 损坏器材需要单独记录

2. **申请审批**
   - 审批时检查库存是否充足
   - 同一器材可以有多个待审批申请
   - 审批通过后立即扣减库存

3. **归还处理**
   - 支持部分归还
   - 损坏器材自动转入损坏库存
   - 记录实际归还时间

### 7.3 性能优化建议

1. **数据库索引**
   - 器材表的分类ID、状态字段
   - 申请表的状态、日期字段
   - 用户ID相关字段

2. **缓存策略**
   - 器材分类列表（较少变动）
   - 热门器材信息
   - 用户权限信息

3. **分页优化**
   - 使用游标分页处理大数据量
   - 限制单次查询最大条数

---

## 8. 测试用例

### 8.1 器材管理测试

```bash
# 获取器材列表
curl -X GET "http://api.example.com/equipment/items?page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 添加器材
curl -X POST "http://api.example.com/equipment/items" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "category_id": "cat001",
    "name": "标准足球",
    "total_quantity": 20,
    "unit_price": 89.99
  }'

# 库存调整
curl -X POST "http://api.example.com/equipment/items/eq001/adjust" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "increase",
    "quantity": 5,
    "reason": "新采购到货"
  }'
```

### 8.2 申请审批测试

```bash
# 获取待审批申请
curl -X GET "http://api.example.com/equipment/applications?status=pending" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 审批申请
curl -X POST "http://api.example.com/equipment/applications/app001/approve" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "approved",
    "remark": "申请已通过"
  }'
```

---

## 9. 更新日志

### v1.0.0 (2024-03-01)
- 初始版本发布
- 支持基础器材管理功能
- 支持借用申请审批流程
- 支持库存调整操作

---

## 10. 联系信息

如有疑问，请联系：
- **API文档维护**: 开发团队
- **技术支持**: tech-support@example.com
- **项目地址**: https://github.com/your-org/pe-management-system

---

*本文档最后更新时间：2024-03-01*

