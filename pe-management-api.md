# PE校园管理端API文档

## 概述

PE校园管理端API为管理员提供活动审核、用户管理、早操管理和签到记录查看等功能。所有接口都需要管理员权限，并根据管理员级别（校级/院级）进行数据过滤。

## 基础信息

- **基础URL**: `/api/pe`
- **认证方式**: Bearer Token
- **数据格式**: JSON
- **字符编码**: UTF-8

## 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体数据
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 权限说明

- 校级管理员：只能查看和管理本学校的数据
- 院级管理员：只能查看和管理本学院的数据
- 后端需要根据登录用户的学校/学院信息进行数据过滤

## 1. 活动管理

### 1.1 获取活动列表

**接口地址**: `GET /pe/activities`

**请求参数**:
```json
{
  "page": 1,                    // 页码，默认1
  "pageSize": 20,               // 每页大小，默认20
  "approvalStatus": "PENDING",  // 审核状态：PENDING/APPROVED/REJECTED/DRAFT
  "category": "球类运动",        // 活动类别（可选）
  "organizerId": "user_001",    // 组织者ID（可选）
  "startDate": "2024-01-01",    // 开始日期（可选）
  "endDate": "2024-01-31"       // 结束日期（可选）
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "activity_001",
        "title": "篮球比赛",
        "description": "校内篮球比赛，欢迎参加",
        "location": "体育馆",
        "max_participants": 20,
        "current_participants": 15,
        "registration_start_time": "2024-01-01T09:00:00Z",
        "registration_end_time": "2024-01-05T18:00:00Z",
        "activity_start_time": "2024-01-06T14:00:00Z",
        "activity_end_time": "2024-01-06T16:00:00Z",
        "organizer": "体育部",
        "organizer_id": "user_002",
        "category": "球类运动",
        "points": 10,
        "image_url": "https://example.com/image.jpg",
        "approval_status": "PENDING",
        "reviewed_by": null,
        "reviewed_at": null,
        "review_comment": null,
        "created_at": "2024-01-01T08:00:00Z",
        "updated_at": "2024-01-01T08:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 1.2 获取单个活动详情

**接口地址**: `GET /pe/activities/{id}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "activity_001",
    "title": "篮球比赛",
    // ... 完整活动信息，同上
  }
}
```

### 1.3 审核活动

**接口地址**: `PUT /pe/activities/{id}/review`

**请求数据**:
```json
{
  "status": "APPROVED",     // APPROVED/REJECTED
  "comment": "活动内容符合要求，通过审核"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "审核成功",
  "data": null
}
```

### 1.4 获取活动签到记录

**接口地址**: `GET /pe/activities/{id}/attendance`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 50,
  "studentId": "2021001",   // 可选，按学号筛选
  "isCheckedOut": true      // 可选，筛选签退状态
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "attendance_001",
        "activity_id": "activity_001",
        "user_id": "user_001",
        "user_name": "张三",
        "student_id": "2021001",
        "check_in_time": "2024-01-06T14:00:00Z",
        "check_in_location": "体育馆",
        "checked_in_by": "checker_001",
        "check_out_time": "2024-01-06T16:00:00Z",
        "check_out_location": "体育馆",
        "checked_out_by": "checker_001",
        "is_checked_out": true,
        "duration": 120,
        "points_earned": 2,
        "qr_code_data": "QR_CODE_DATA",
        "created_at": "2024-01-06T14:00:00Z",
        "updated_at": "2024-01-06T16:00:00Z"
      }
    ],
    "total": 15
  }
}
```

## 2. 用户管理

### 2.1 获取用户列表

**接口地址**: `GET /pe/users`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "search": "张三",          // 可选，按姓名或学号搜索
  "role": "STUDENT",        // 可选，按角色筛选：STUDENT/CHECKER/SUB_CHECKER/ADMIN
  "school": "某大学",       // 可选，按学校筛选
  "college": "计算机学院"   // 可选，按学院筛选
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "user_001",
        "name": "张三",
        "student_id": "2021001",
        "school": "某大学",
        "college": "计算机学院",
        "phone_number": "13800138000",
        "avatar": "https://example.com/avatar.jpg",
        "points": 100,
        "pe_activity_points": 80,
        "morning_exercise_points": 20,
        "study_hours": 5,
        "integrity_score": 95,
        "role": "STUDENT",
        "is_logged_in": false,
        "points_last_updated": "2024-01-01T12:00:00Z",
        "created_at": "2024-01-01T00:00:00Z",
        "updated_at": "2024-01-01T12:00:00Z"
      }
    ],
    "total": 500,
    "page": 1,
    "pageSize": 20
  }
}
```

### 2.2 获取单个用户详情

**接口地址**: `GET /pe/users/{id}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "user_001",
    "name": "张三",
    // ... 完整用户信息，同上
  }
}
```

### 2.3 设置用户角色

**接口地址**: `PUT /pe/users/{id}/role`

**请求数据**:
```json
{
  "role": "CHECKER"  // STUDENT转为CHECKER
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "角色设置成功",
  "data": null
}
```

### 2.4 获取用户积分历史

**接口地址**: `GET /pe/users/{id}/points-history`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 50,
  "startDate": "2024-01-01",  // 可选
  "endDate": "2024-01-31",    // 可选
  "activityType": "PE_ACTIVITY" // 可选：PE_ACTIVITY/MORNING_EXERCISE
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "points_001",
        "user_id": "user_001",
        "activity_id": "activity_001",
        "activity_name": "篮球比赛",
        "activity_type": "PE_ACTIVITY",
        "points_earned": 2,
        "earned_reason": "参与活动",
        "calculation_rule": "参与时长120分钟，积分=floor(120/60)=2",
        "participation_duration": 120,
        "earned_at": "2024-01-06T16:00:00Z"
      }
    ],
    "total": 20
  }
}
```

## 3. 早操管理

### 3.1 获取早操活动列表

**接口地址**: `GET /pe/morning-exercises`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "date": "2024-01-01",      // 可选，按日期筛选
  "isActive": true,          // 可选，筛选活跃状态
  "startDate": "2024-01-01", // 可选，日期范围开始
  "endDate": "2024-01-31"    // 可选，日期范围结束
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "morning_001",
        "title": "早操考勤",
        "description": "2024年1月1日早操考勤",
        "location": "操场",
        "date": "2024-01-01",
        "start_time": "2024-01-01T06:30:00Z",
        "end_time": "2024-01-01T07:30:00Z",
        "type": "MORNING_EXERCISE",
        "is_active": true,
        "total_participants": 100,
        "checked_in_count": 85,
        "checked_out_count": 80,
        "created_by": "admin_001",
        "created_at": "2023-12-31T20:00:00Z",
        "updated_at": "2024-01-01T07:30:00Z"
      }
    ],
    "total": 30
  }
}
```

### 3.2 获取单个早操活动详情

**接口地址**: `GET /pe/morning-exercises/{id}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "morning_001",
    // ... 完整早操信息，同上
  }
}
```

### 3.3 创建早操活动

**接口地址**: `POST /pe/morning-exercises`

**请求数据**:
```json
{
  "title": "早操考勤",
  "description": "2024年1月2日早操考勤",
  "location": "操场",
  "date": "2024-01-02",
  "start_time": "2024-01-02T06:30:00Z",
  "end_time": "2024-01-02T07:30:00Z"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "早操活动创建成功",
  "data": {
    "id": "morning_002",
    "title": "早操考勤",
    // ... 创建的早操信息
  }
}
```

### 3.4 更新早操活动

**接口地址**: `PUT /pe/morning-exercises/{id}`

**请求数据**:
```json
{
  "title": "早操考勤（更新）",
  "description": "更新后的描述",
  "location": "操场",
  "start_time": "2024-01-02T06:30:00Z",
  "end_time": "2024-01-02T07:30:00Z"
}
```

### 3.5 删除早操活动

**接口地址**: `DELETE /pe/morning-exercises/{id}`

**响应数据**:
```json
{
  "code": 200,
  "message": "早操活动删除成功",
  "data": null
}
```

### 3.6 获取早操考勤记录

**接口地址**: `GET /pe/morning-exercises/{id}/attendance`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 100,
  "studentId": "2021001",     // 可选，按学号筛选
  "isCheckedOut": true,       // 可选，筛选签退状态
  "checkedBy": "checker_001"  // 可选，按签到员筛选
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "morning_attendance_001",
        "exercise_id": "morning_001",
        "student_id": "2021001",
        "student_name": "张三",
        "check_in_time": "2024-01-01T06:30:00Z",
        "check_in_location": "操场",
        "checked_by": "checker_001",
        "checked_by_name": "李四",
        "check_out_time": "2024-01-01T07:25:00Z",
        "check_out_location": "操场",
        "checked_out_by": "checker_001",
        "checked_out_by_name": "李四",
        "is_checked_out": true,
        "points_earned": 1,
        "qr_code_data": "QR_CODE_DATA",
        "is_valid": true,
        "created_at": "2024-01-01T06:30:00Z",
        "updated_at": "2024-01-01T07:25:00Z"
      }
    ],
    "total": 85
  }
}
```

## 4. 签到记录管理

### 4.1 获取签到记录列表

**接口地址**: `GET /pe/attendance-records`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 50,
  "activityId": "activity_001", // 可选，按活动筛选
  "studentId": "2021001",       // 可选，按学号筛选
  "studentName": "张三",        // 可选，按姓名筛选
  "startDate": "2024-01-01",    // 可选，签到开始日期
  "endDate": "2024-01-31",      // 可选，签到结束日期
  "isCheckedOut": true,         // 可选，筛选签退状态
  "minDuration": 60,            // 可选，最小参与时长（分钟）
  "maxDuration": 300            // 可选，最大参与时长（分钟）
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "attendance_001",
        "activity_id": "activity_001",
        "user_id": "user_001",
        "user_name": "张三",
        "student_id": "2021001",
        "check_in_time": "2024-01-06T14:00:00Z",
        "check_in_location": "体育馆",
        "checked_in_by": "checker_001",
        "check_out_time": "2024-01-06T16:00:00Z",
        "check_out_location": "体育馆",
        "checked_out_by": "checker_001",
        "is_checked_out": true,
        "duration": 120,
        "points_earned": 2,
        "qr_code_data": "QR_CODE_DATA",
        "created_at": "2024-01-06T14:00:00Z",
        "updated_at": "2024-01-06T16:00:00Z"
      }
    ],
    "total": 200
  }
}
```

### 4.2 获取单个签到记录详情

**接口地址**: `GET /pe/attendance-records/{id}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "attendance_001",
    // ... 完整签到记录信息，同上
    "activity": {
      "id": "activity_001",
      "title": "篮球比赛",
      "category": "球类运动"
    },
    "user": {
      "id": "user_001",
      "name": "张三",
      "student_id": "2021001",
      "school": "某大学",
      "college": "计算机学院"
    }
  }
}
```

### 4.3 导出签到记录

**接口地址**: `GET /pe/attendance-records/export`

**请求参数**: 同获取签到记录列表

**响应**: Excel文件下载

## 5. 统计数据

### 5.1 获取总体统计

**接口地址**: `GET /pe/statistics`

**请求参数**:
```json
{
  "startDate": "2024-01-01",  // 可选，统计开始日期
  "endDate": "2024-01-31",    // 可选，统计结束日期
  "school": "某大学",         // 可选，按学校筛选
  "college": "计算机学院"     // 可选，按学院筛选
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "activities": {
      "total": 100,
      "pending": 10,
      "approved": 80,
      "rejected": 10
    },
    "users": {
      "total": 500,
      "students": 480,
      "checkers": 15,
      "sub_checkers": 4,
      "admins": 1
    },
    "morning_exercises": {
      "total": 30,
      "active": 1,
      "completed": 29
    },
    "attendance": {
      "total_checkins": 1500,
      "total_checkouts": 1200,
      "average_duration": 95,
      "total_points": 1800
    }
  }
}
```

### 5.2 获取活动统计

**接口地址**: `GET /pe/statistics/activities`

**请求参数**:
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "groupBy": "category"  // category/month/week
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "chart_data": [
      {
        "name": "球类运动",
        "count": 30,
        "participants": 450
      },
      {
        "name": "跑步",
        "count": 25,
        "participants": 380
      }
    ],
    "summary": {
      "total_activities": 100,
      "total_participants": 1500,
      "average_participants": 15
    }
  }
}
```

### 5.3 获取用户统计

**接口地址**: `GET /pe/statistics/users`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "points_distribution": [
      {
        "range": "0-50",
        "count": 100
      },
      {
        "range": "51-100",
        "count": 200
      }
    ],
    "top_users": [
      {
        "user_id": "user_001",
        "name": "张三",
        "student_id": "2021001",
        "total_points": 150,
        "activity_count": 10
      }
    ]
  }
}
```

### 5.4 获取早操统计

**接口地址**: `GET /pe/statistics/morning-exercises`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "monthly_data": [
      {
        "month": "2024-01",
        "exercise_count": 31,
        "total_checkins": 2500,
        "total_checkouts": 2300,
        "attendance_rate": 0.92
      }
    ],
    "summary": {
      "total_exercises": 31,
      "average_attendance": 85,
      "total_points": 2300
    }
  }
}
```

## 错误响应

### 4xx 客户端错误

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "errors": [
    {
      "field": "approvalStatus",
      "message": "审核状态值无效"
    }
  ]
}
```

### 5xx 服务器错误

```json
{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}
```

## 权限控制说明

1. **校级管理员权限**:
   - 只能查看和管理本校的活动、用户、早操等数据
   - 后端需要根据管理员的school字段过滤数据

2. **院级管理员权限**:
   - 只能查看和管理本院的活动、用户、早操等数据
   - 后端需要根据管理员的school和college字段过滤数据

3. **数据过滤实现**:
   - 在SQL查询中添加WHERE条件
   - 活动数据：通过organizer所属学校/学院过滤
   - 用户数据：直接按school/college字段过滤
   - 早操数据：通过创建者所属学校/学院过滤
   - 签到记录：通过用户所属学校/学院过滤

## 注意事项

1. 所有接口都需要验证管理员权限
2. 分页参数page从1开始计数
3. 日期格式统一使用ISO 8601格式
4. 文件导出接口返回二进制流，需要设置正确的Content-Type
5. 所有敏感操作需要记录操作日志
6. 积分计算规则：
   - PE活动积分 = floor(参与时长(分钟) / 60)
   - 早操积分 = 完成签到和签退得1积分