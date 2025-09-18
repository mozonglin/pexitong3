# 器材借阅端 API 文档

## 概述

器材借阅端API为学生和教师提供器材浏览和借阅申请功能，包括查看器材库存、提交借阅申请、查看个人借阅记录等。

### 基础信息

- **API版本**: v1.0
- **基础URL**: `http://your-domain/api/borrowing`
- **认证方式**: Bearer Token (JWT)
- **内容类型**: application/json
- **用户来源**: users1 表（PE用户表）

### 认证说明

所有API请求都需要在Header中携带JWT Token：

```http
Authorization: Bearer <your-jwt-token>
```

---

## 1. 器材浏览

### 1.1 获取器材分类列表

获取所有器材分类，用于分类筛选。

**请求信息**
- **URL**: `/categories`
- **方法**: `GET`
- **权限**: 需要登录

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
      "icon": "⚽"
    },
    {
      "id": "cat002",
      "name": "健身器材", 
      "description": "健身和力量训练器材",
      "icon": "💪"
    }
  ]
}
```

### 1.2 获取器材列表

获取可借用的器材列表，支持分页、搜索和分类筛选。

**请求信息**
- **URL**: `/equipment`
- **方法**: `GET`
- **权限**: 需要登录

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | 否 | 页码，默认1 |
| limit | int | 否 | 每页数量，默认10，最大50 |
| category_id | string | 否 | 分类ID筛选 |
| keyword | string | 否 | 关键词搜索（器材名称、型号） |
| available_only | boolean | 否 | 是否只显示有库存的器材，默认true |

**请求示例**
```http
GET /api/borrowing/equipment?page=1&limit=10&category_id=cat001&keyword=足球&available_only=true
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
        "available_quantity": 35,
        "total_quantity": 50,
        "unit_price": 89.99,
        "storage_location": "器材室A区1号柜",
        "can_borrow": true
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

### 1.3 获取器材详情

获取指定器材的详细信息。

**请求信息**
- **URL**: `/equipment/{id}`
- **方法**: `GET`
- **权限**: 需要登录

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 器材ID |

**响应示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "eq001",
    "category_id": "cat001",
    "category_name": "球类器材",
    "name": "标准足球",
    "model": "Nike Premier League", 
    "specification": "5号球，FIFA认证标准",
    "available_quantity": 35,
    "total_quantity": 50,
    "unit_price": 89.99,
    "purchase_date": "2024-01-15",
    "warranty_period": 12,
    "storage_location": "器材室A区1号柜",
    "can_borrow": true,
    "max_borrow_quantity": 10
  }
}
```

---

## 2. 借阅申请

### 2.1 提交借阅申请

提交新的器材借阅申请。

**请求信息**
- **URL**: `/applications`
- **方法**: `POST`
- **权限**: 需要登录

**请求体**
```json
{
  "equipment_id": "eq001",
  "quantity": 5,
  "purpose": "体育课教学使用",
  "borrow_date": "2024-03-15T08:00:00Z",
  "expected_return_date": "2024-03-15T18:00:00Z"
}
```

**字段说明**
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| equipment_id | string | 是 | 器材ID |
| quantity | int | 是 | 借用数量，必须大于0 |
| purpose | string | 是 | 借用目的，最大200字符 |
| borrow_date | string | 是 | 借用开始时间，ISO格式 |
| expected_return_date | string | 是 | 预期归还时间，ISO格式 |

**响应示例**
```json
{
  "code": 200,
  "message": "借阅申请提交成功",
  "data": {
    "application_id": "app001",
    "equipment_id": "eq001",
    "equipment_name": "标准足球",
    "quantity": 5,
    "status": "pending",
    "borrow_date": "2024-03-15T08:00:00Z",
    "expected_return_date": "2024-03-15T18:00:00Z",
    "created_at": "2024-03-14T10:00:00Z"
  }
}
```

### 2.2 获取个人借阅记录

获取当前用户的借阅申请记录。

**请求信息**
- **URL**: `/applications/my`
- **方法**: `GET`
- **权限**: 需要登录

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | 否 | 页码，默认1 |
| limit | int | 否 | 每页数量，默认10 |
| status | string | 否 | 状态筛选：pending/approved/rejected/returned |
| date_from | string | 否 | 开始日期，格式：YYYY-MM-DD |
| date_to | string | 否 | 结束日期，格式：YYYY-MM-DD |

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
        "quantity": 5,
        "purpose": "体育课教学使用",
        "borrow_date": "2024-03-15T08:00:00Z",
        "expected_return_date": "2024-03-15T18:00:00Z",
        "actual_return_date": null,
        "status": "approved",
        "remark": "申请已通过，请按时归还",
        "created_at": "2024-03-14T10:00:00Z",
        "approved_at": "2024-03-14T14:00:00Z",
        "can_cancel": false,
        "is_overdue": false
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

### 2.3 取消借阅申请

取消待审批的借阅申请。

**请求信息**
- **URL**: `/applications/{id}/cancel`
- **方法**: `POST`
- **权限**: 需要登录，且为申请人

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 申请ID |

**响应示例**
```json
{
  "code": 200,
  "message": "申请已取消",
  "data": {
    "application_id": "app001",
    "status": "cancelled",
    "cancelled_at": "2024-03-14T16:00:00Z"
  }
}
```

### 2.4 获取申请详情

获取指定申请的详细信息。

**请求信息**
- **URL**: `/applications/{id}`
- **方法**: `GET`
- **权限**: 需要登录，且为申请人

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 申请ID |

**响应示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "app001",
    "equipment_id": "eq001",
    "equipment_name": "标准足球",
    "equipment_model": "Nike Premier League",
    "quantity": 5,
    "purpose": "体育课教学使用",
    "borrow_date": "2024-03-15T08:00:00Z",
    "expected_return_date": "2024-03-15T18:00:00Z",
    "actual_return_date": null,
    "status": "approved",
    "remark": "申请已通过，请按时归还",
    "created_at": "2024-03-14T10:00:00Z",
    "approved_at": "2024-03-14T14:00:00Z",
    "can_cancel": false,
    "is_overdue": false,
    "borrower_info": {
      "name": "张三",
      "student_id": "202110001",
      "phone": "13800138001"
    }
  }
}
```

---

## 3. 用户信息

### 3.1 获取个人信息

获取当前登录用户的基本信息。

**请求信息**
- **URL**: `/profile`
- **方法**: `GET`
- **权限**: 需要登录

**响应示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "user001",
    "name": "张三",
    "student_id": "202110001",
    "school": "北京体育大学",
    "college": "计算机学院",
    "phone": "13800138001",
    "role": "student",
    "points": 100,
    "borrowing_stats": {
      "total_applications": 15,
      "pending_applications": 1,
      "overdue_count": 0,
      "credit_score": 95
    }
  }
}
```

---

## 4. 数据模型

### 4.1 器材信息模型
```json
{
  "id": "string",
  "category_id": "string",
  "category_name": "string",
  "name": "string",
  "model": "string",
  "specification": "string",
  "available_quantity": "integer",
  "total_quantity": "integer",
  "unit_price": "float",
  "purchase_date": "date",
  "warranty_period": "integer",
  "storage_location": "string",
  "can_borrow": "boolean",
  "max_borrow_quantity": "integer"
}
```

### 4.2 借阅申请模型
```json
{
  "id": "string",
  "equipment_id": "string",
  "equipment_name": "string",
  "equipment_model": "string",
  "quantity": "integer",
  "purpose": "string",
  "borrow_date": "datetime",
  "expected_return_date": "datetime",
  "actual_return_date": "datetime",
  "status": "enum[pending,approved,rejected,returned,cancelled]",
  "remark": "string",
  "created_at": "datetime",
  "approved_at": "datetime",
  "can_cancel": "boolean",
  "is_overdue": "boolean"
}
```

---

## 5. 错误处理

### 5.1 错误响应格式

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "errors": [
    {
      "field": "quantity",
      "message": "借用数量不能超过库存"
    }
  ]
}
```

### 5.2 常见错误码

| 错误码 | 说明 | 常见原因 |
|--------|------|----------|
| 400 | 请求参数错误 | 参数格式不正确、必填参数缺失 |
| 401 | 未授权 | Token无效或已过期 |
| 403 | 权限不足 | 用户无权限访问 |
| 404 | 资源不存在 | 器材或申请不存在 |
| 409 | 资源冲突 | 库存不足、重复申请等 |
| 422 | 业务逻辑错误 | 借用时间冲突、超出限额等 |
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

#### 借用数量超限
```json
{
  "code": 422,
  "message": "单次借用数量超出限制",
  "data": {
    "equipment_id": "eq001",
    "requested_quantity": 15,
    "max_quantity": 10
  }
}
```

---

## 6. 业务规则

### 6.1 借阅限制

1. **数量限制**: 单次借用数量不能超过库存的50%，最多10件
2. **时间限制**: 借用时间不能超过7天
3. **重复申请**: 同一器材同一时间段只能有一个待审批申请
4. **信用限制**: 信用分低于60分的用户限制借阅

### 6.2 申请状态说明

- **pending**: 待审批
- **approved**: 已批准
- **rejected**: 已拒绝
- **returned**: 已归还
- **cancelled**: 已取消（用户主动取消）

### 6.3 时间规则

- 借用开始时间不能早于当前时间
- 归还时间必须晚于借用时间
- 借用时长不能超过7天
- 超期未归还会影响信用分

---

## 7. 测试示例

### 7.1 获取器材列表
```bash
curl -X GET "http://localhost:8080/api/borrowing/equipment?category_id=cat001" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 7.2 提交借阅申请
```bash
curl -X POST "http://localhost:8080/api/borrowing/applications" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "equipment_id": "eq001",
    "quantity": 5,
    "purpose": "体育课使用",
    "borrow_date": "2024-03-15T08:00:00Z",
    "expected_return_date": "2024-03-15T18:00:00Z"
  }'
```

### 7.3 查看个人借阅记录
```bash
curl -X GET "http://localhost:8080/api/borrowing/applications/my?status=approved" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 8. 前端开发建议

### 8.1 状态管理

建议使用状态管理库管理以下状态：
- 用户登录状态和基本信息
- 器材列表和分类数据
- 借阅申请记录
- 购物车（批量借阅）

### 8.2 缓存策略

- 器材分类数据可缓存较长时间
- 器材列表数据缓存5分钟
- 个人申请记录实时获取

### 8.3 用户体验

1. **实时库存**: 显示实时可用数量
2. **快速筛选**: 提供分类、搜索、排序功能
3. **批量操作**: 支持批量添加到借阅清单
4. **状态提醒**: 申请状态变化及时通知
5. **离线支持**: 关键数据支持离线浏览

---

## 9. 安全说明

1. **权限验证**: 所有接口都需要验证用户身份
2. **数据隔离**: 用户只能查看和操作自己的申请
3. **参数验证**: 严格验证所有输入参数
4. **频率限制**: 对申请提交接口进行频率限制
5. **敏感信息**: 不返回其他用户的敏感信息

---

*本文档最后更新时间：2024-08-15*





