# 用户认证系统 API 文档

## 概述

本文档描述了校园体育管理系统用户认证模块的API接口，包含用户注册、登录、权限管理、会话管理等功能。

## 基础信息

- **基础URL**: `https://api.example.com/v1`
- **认证方式**: Bearer Token
- **数据格式**: JSON
- **编码**: UTF-8

## 用户角色体系

### 角色定义

| 角色 | 代码 | 描述 | 权限层级 |
|------|------|------|----------|
| 学生 | `student` | 普通学生用户 | 基础用户权限 |
| 教师 | `teacher` | 任课教师 | 内容创建权限 |
| 院级管理员 | `department_admin` | 院系管理员 | 院系管理权限 |
| 校级管理员 | `school_admin` | 学校管理员 | 全校管理权限 |
| 超级管理员 | `super_admin` | 系统管理员 | 系统级权限 |

### 系统权限矩阵

| 权限类别 | 学生 | 教师 | 院级管理员 | 校级管理员 | 超级管理员 |
|----------|------|------|------------|------------|------------|
| **用户认证** | | | | | |
| 用户注册 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 用户登录 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 修改个人信息 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 修改密码 | ✅ | ✅ | ✅ | ✅ | ✅ |
| **用户管理** | | | | | |
| 查看用户列表 | ❌ | ❌ | ❌ | ✅ | ✅ |
| 创建用户 | ❌ | ❌ | ❌ | ✅ | ✅ |
| 修改用户状态 | ❌ | ❌ | ❌ | ✅ | ✅ |
| 重置用户密码 | ❌ | ❌ | ❌ | ✅ | ✅ |
| **角色管理** | | | | | |
| 设置院级管理员 | ❌ | ❌ | ❌ | ✅ | ✅ |
| 设置校级管理员 | ❌ | ❌ | ❌ | ❌ | ✅ |
| 降级管理员 | ❌ | ❌ | ❌ | ✅ | ✅ |
| **会话管理** | | | | | |
| 查看在线统计 | ❌ | ❌ | ❌ | ✅ | ✅ |
| 查看用户会话 | ❌ | ❌ | ❌ | ✅ | ✅ |
| 强制下线用户 | ❌ | ❌ | ❌ | ✅ | ✅ |
| **系统管理** | | | | | |
| 查看登录日志 | ❌ | ❌ | ❌ | ❌ | ✅ |
| 系统配置管理 | ❌ | ❌ | ❌ | ❌ | ✅ |
| 安全审计 | ❌ | ❌ | ❌ | ❌ | ✅ |

### 权限说明

**权限层级**：
- **基础用户权限**：个人信息管理、基础功能访问
- **内容创建权限**：基础权限 + 内容创建和编辑
- **院系管理权限**：内容权限 + 本院系数据管理
- **全校管理权限**：院系权限 + 全校数据管理 + 用户管理
- **系统级权限**：全校权限 + 系统配置 + 安全管理

**注意事项**：
- 前端基于用户角色直接控制UI显示
- 后端负责API访问权限验证
- 具体功能权限请参考对应功能的API文档

### 管理员层级设置规则

- **超级管理员**：可以设置任何用户为校级管理员
- **校级管理员**：可以设置教师为院级管理员，也可以将院级管理员降级为教师
- **院级管理员**：无法设置其他管理员
- **教师和学生**：无法设置任何管理员

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 错误响应

```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": "详细错误信息",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 1. 用户认证接口

### 1.1 发送验证码

**接口**: `POST /auth/send-verification-code`

**权限**: 公开接口

**说明**: 发送短信验证码到用户手机，用于注册验证、密码重置等场景

**请求体**:
```json
{
  "phone": "13812345678",
  "type": "register"
}
```

**参数说明**:
- `phone`: 手机号码，11位中国大陆手机号
- `type`: 验证码类型，可选值：
  - `register`: 注册验证码
  - `reset_password`: 密码重置验证码
  - `change_phone`: 更换手机号验证码

**响应**:
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": {
    "phone": "138****5678",
    "expiresAt": "2024-01-15T10:35:00Z",
    "canResendAfter": "2024-01-15T10:31:00Z"
  }
}
```

**错误示例**:
```json
{
  "code": 400,
  "message": "手机号格式错误",
  "error": "INVALID_PHONE_FORMAT"
}
```

**限制规则**:
- 同一手机号60秒内只能发送一次
- 同一手机号一天内最多发送5次
- 验证码有效期5分钟

### 1.2 用户注册

**接口**: `POST /auth/register`

**权限**: 公开接口

**说明**: 用户提供基本信息和手机验证码进行身份认证，系统根据用户类型分别在学生预导入表或教师预导入表中验证信息，成功后返回用户名、角色和初始密码。

**后端实现说明**:
- 当 `userType` 为 `student` 时，后端从学生预导入表中查找匹配的学号
- 当 `userType` 为 `teacher` 时，后端从教师预导入表中查找匹配的工号
- 这样可以避免学号和工号冲突，提高查找效率

**请求体**:
```json
{
  "realName": "张三",
  "userType": "student",
  "school": "北京体育大学",
  "studentId": "2024001001",
  "phone": "13812345678",
  "verificationCode": "123456"
}
```

**参数说明**:
- `realName`: 真实姓名，2-20位中文或英文
- `userType`: 用户类型，可选值：`student`(学生) 或 `teacher`(教师)
- `school`: 所属学校，必须从预设学校列表中选择
- `studentId`: 学号或工号，用于在数据库中查找用户信息
- `phone`: 手机号码，11位中国大陆手机号
- `verificationCode`: 6位数字验证码，通过`/auth/send-verification-code`接口获取

**学校列表**:
- 北京体育大学
- 上海体育学院
- 成都体育学院
- 武汉体育学院
- 西安体育学院
- 南京体育学院
- 首都体育学院
- 天津体育学院
- 沈阳体育学院
- 吉林体育学院
- 哈尔滨体育学院
- 山东体育学院
- 河北体育学院
- 广州体育学院

**响应**:
```json
{
  "code": 200,
  "message": "身份认证成功",
  "data": {
    "userId": "stu_xyz123",
    "username": "zhang2024001",
    "userType": "student",
    "realName": "张三",
    "school": "北京体育大学",
    "department": "体育教育学院",
    "initialPassword": "TempPass123",
    "passwordSent": true,
    "smsMessage": "您的用户名: zhang2024001, 初始密码: TempPass123, 请及时登录修改密码"
  }
}
```

**错误示例**:
```json
{
  "code": 400,
  "message": "验证码错误或已过期",
  "error": "INVALID_VERIFICATION_CODE"
}
```

**注册流程**:
1. 用户选择用户类型（学生或教师）
2. 填写基本信息（姓名、学校、学工号、手机号）
3. 点击"获取验证码"发送短信验证码
4. 输入收到的6位验证码
5. 提交注册信息
6. 系统根据用户类型分别在学生表或教师表中验证学工号
7. 验证成功后生成用户名和初始密码，通过短信发送
8. 用户使用返回的用户名和密码登录

### 1.3 用户登录

**接口**: `POST /auth/login`

**权限**: 公开接口

**请求体**:
```json
{
  "username": "zhang2024001",
  "password": "TempPass123",
  "userType": "student",
  "rememberMe": false,
  "captcha": "abc123",
  "captchaToken": "token_xyz"
}
```

**参数说明**:
- `username`: 系统生成的用户名
- `password`: 密码（初始密码或用户修改后的密码）
- `userType`: 用户类型，必须匹配系统分配的类型
- `rememberMe`: 是否记住登录，默认false（24小时），true（30天）
- `captcha`: 验证码（登录失败3次后需要）
- `captchaToken`: 验证码令牌

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "user": {
      "id": "stu_xyz123",
      "username": "zhang2024001",
      "realName": "张三",
      "userType": "student",
      "school": "北京体育大学",
      "departmentId": 1,
      "departmentName": "体育教育学院",
      "studentId": "2024001001",
      "phone": "13812345678",
      "avatar": null,
      "status": "active",
      "lastLoginAt": "2024-01-15T10:30:00Z",
      "loginCount": 15,
      "isFirstLogin": false
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2024-01-16T10:30:00Z"
  }
}
```

**说明**:
- 登录响应不再包含 `permissions` 和 `roles` 数组
- 前端基于用户的 `userType` 字段直接进行角色判断
- 系统采用基于角色的简单权限控制，UI根据角色显示不同功能

**错误示例**:
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "error": "INVALID_CREDENTIALS",
  "remainingAttempts": 2
}
```

### 1.4 用户登出

**接口**: `POST /auth/logout`

**权限**: 需要登录

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

### 1.5 刷新Token

**接口**: `POST /auth/refresh`

**权限**: 需要有效Token

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2024-01-16T10:30:00Z"
  }
}
```

## 2. 用户信息管理

### 2.1 获取当前用户信息

**接口**: `GET /user/profile`

**权限**: 需要登录

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "stu_xyz123",
    "username": "zhang2024001",
    "realName": "张三",
    "studentId": "2024001001",
    "userType": "student",
    "school": "北京体育大学",
    "departmentId": 1,
    "departmentName": "体育教育学院",
    "phone": "13812345678",
    "avatar": "https://oss.example.com/avatars/avatar.jpg",
    "status": "active",
    "createdAt": "2024-01-01T00:00:00Z",
    "lastLoginAt": "2024-01-15T10:30:00Z",
    "loginCount": 15,
    "isFirstLogin": false,
    "statistics": {
      "listeningCount": 25,
      "uploadCount": 8,
      "feedbackCount": 12,
      "averageScore": 8.7
    }
  }
}
```

### 2.2 更新用户信息

**接口**: `PUT /user/profile`

**权限**: 需要登录

**说明**: 用户只能修改部分非关键信息

**请求体**:
```json
{
  "phone": "13812345679",
  "avatar": "base64_encoded_image_data"
}
```

**可修改字段**:
- `phone`: 手机号码
- `avatar`: 头像（base64编码或文件上传）

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "stu_xyz123",
    "phone": "13812345679",
    "avatar": "https://oss.example.com/avatars/updated_avatar.jpg"
  }
}
```

### 2.3 修改密码

**接口**: `POST /user/change-password`

**权限**: 需要登录

**请求体**:
```json
{
  "oldPassword": "TempPass123",
  "newPassword": "NewSecurePass456",
  "confirmPassword": "NewSecurePass456"
}
```

**密码要求**:
- 至少8位字符
- 包含大写字母
- 包含小写字母
- 包含数字
- 可选：包含特殊字符

**响应**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": {
    "isFirstLogin": false,
    "passwordChangedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 2.4 上传头像

**接口**: `POST /user/avatar`

**权限**: 需要登录

**请求**: `multipart/form-data`
- `avatar`: 头像文件，支持jpg、png格式，大小不超过2MB

**响应**:
```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "avatarUrl": "https://oss.example.com/avatars/stu_xyz123.jpg",
    "fileSize": 512000,
    "uploadTime": "2024-01-15T10:30:00Z"
  }
}
```

## 3. 管理员功能接口

### 3.1 获取用户列表

**接口**: `GET /admin/users`

**权限**: `school_admin` 或 `super_admin`

**请求参数**:
```json
{
  "userType": "teacher",
  "departmentId": 1,
  "keyword": "张",
  "status": "active",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "page": 1,
  "limit": 10,
  "sortBy": "createdAt",
  "sortOrder": "desc"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "id": "tea_xyz123",
        "username": "li2024002",
        "realName": "李老师",
        "studentId": "T2024002",
        "userType": "teacher",
        "school": "北京体育大学",
        "departmentName": "体育教育学院",
        "phone": "13812345679",
        "status": "active",
        "lastLoginAt": "2024-01-15T10:30:00Z",
        "loginCount": 25,
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 156,
    "page": 1,
    "limit": 10,
    "totalPages": 16
  }
}
```

### 3.2 设置用户角色

**接口**: `PUT /admin/users/{userId}/role`

**权限**: 
- `super_admin`: 可设置任何角色
- `school_admin`: 只能设置教师为院级管理员，或将院级管理员降级为教师

**请求体**:
```json
{
  "userType": "department_admin",
  "reason": "根据工作需要提升为院级管理员"
}
```

**提升规则**:
- 超级管理员 → 可提升教师为院级管理员，院级管理员为校级管理员
- 校级管理员 → 只能提升教师为院级管理员
- 降级同理，但方向相反

**响应**:
```json
{
  "code": 200,
  "message": "用户角色设置成功",
  "data": {
    "userId": "tea_xyz123",
    "oldRole": "teacher",
    "newRole": "department_admin",
    "changedBy": "admin",
    "changedAt": "2024-01-15T10:30:00Z",
    "reason": "根据工作需要提升为院级管理员"
  }
}
```

**错误示例**:
```json
{
  "code": 403,
  "message": "权限不足，无法设置该角色",
  "error": "INSUFFICIENT_PERMISSION",
  "details": "校级管理员只能设置院级管理员"
}
```

### 3.3 更新用户状态

**接口**: `PUT /admin/users/{userId}/status`

**权限**: `school_admin` 或 `super_admin`

**请求体**:
```json
{
  "status": "suspended",
  "reason": "违反使用规定",
  "duration": 30
}
```

**状态说明**:
- `active`: 正常状态
- `suspended`: 暂停使用
- `banned`: 永久封禁

**响应**:
```json
{
  "code": 200,
  "message": "用户状态更新成功",
  "data": {
    "userId": "tea_xyz123",
    "oldStatus": "active",
    "newStatus": "suspended",
    "reason": "违反使用规定",
    "effectiveUntil": "2024-02-15T10:30:00Z",
    "changedBy": "admin"
  }
}
```

### 3.4 重置用户密码

**接口**: `POST /admin/users/{userId}/reset-password`

**权限**: `school_admin` 或 `super_admin`

**请求体**:
```json
{
  "temporaryPassword": "TempPass123",
  "forceChange": true,
  "notifyUser": true,
  "notifyMethod": "sms"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": {
    "userId": "tea_xyz123",
    "temporaryPassword": "TempPass123",
    "mustChangeOnNextLogin": true,
    "notificationSent": true,
    "notifyMethod": "sms"
  }
}
```

### 3.5 获取管理员统计

**接口**: `GET /admin/stats`

**权限**: `school_admin` 或 `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "userCounts": {
      "student": 1250,
      "teacher": 85,
      "department_admin": 12,
      "school_admin": 3,
      "super_admin": 1
    },
    "onlineStats": {
      "totalOnline": 156,
      "userTypeStats": {
        "student": 120,
        "teacher": 25,
        "department_admin": 8,
        "school_admin": 2,
        "super_admin": 1
      }
    },
    "recentActivity": {
      "newUsers": 15,
      "activeUsers": 340,
      "loginToday": 280
    }
  }
}
```

## 4. 会话管理接口

### 4.1 获取在线用户统计

**接口**: `GET /admin/sessions/stats`

**权限**: `school_admin` 或 `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalOnline": 156,
    "userTypeStats": {
      "student": 120,
      "teacher": 25,
      "department_admin": 8,
      "school_admin": 2,
      "super_admin": 1
    },
    "recentLogins": 45,
    "totalUsers": 2560,
    "activeRate": 0.061,
    "peakHours": {
      "morning": 145,
      "afternoon": 89,
      "evening": 67
    }
  }
}
```

### 4.2 获取用户会话列表

**接口**: `GET /admin/users/{userId}/sessions`

**权限**: `school_admin` 或 `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "sessions": [
      {
        "sessionId": "sess_abc123",
        "loginTime": "2024-01-15T10:30:00Z",
        "lastActivity": "2024-01-15T11:45:00Z",
        "expiresAt": "2024-01-16T10:30:00Z",
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0 Chrome/120.0",
        "location": "北京市",
        "device": "Desktop",
        "status": "active"
      }
    ],
    "total": 3,
    "activeCount": 1
  }
}
```

### 4.3 强制下线用户

**接口**: `DELETE /admin/users/{userId}/sessions/{sessionId}`

**权限**: `school_admin` 或 `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "用户已强制下线",
  "data": {
    "userId": "tea_xyz123",
    "sessionId": "sess_abc123",
    "terminatedAt": "2024-01-15T12:00:00Z",
    "terminatedBy": "admin"
  }
}
```

## 5. 安全接口

### 5.1 获取验证码

**接口**: `GET /security/captcha`

**权限**: 公开接口

**响应**:
```json
{
  "code": 200,
  "message": "验证码生成成功",
  "data": {
    "captchaToken": "token_xyz123",
    "captchaImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "expiresAt": "2024-01-15T10:35:00Z"
  }
}
```

### 5.2 登录日志查询

**接口**: `GET /admin/security/login-logs`

**权限**: `super_admin`

**请求参数**:
```json
{
  "userId": "tea_xyz123",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "status": "success",
  "ipAddress": "192.168.1.100",
  "page": 1,
  "limit": 10
}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "logs": [
      {
        "id": "log_123",
        "userId": "tea_xyz123",
        "username": "li2024002",
        "userType": "teacher",
        "status": "success",
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0 Chrome/120.0",
        "location": "北京市",
        "timestamp": "2024-01-15T10:30:00Z",
        "failureReason": null
      }
    ],
    "total": 156,
    "page": 1,
    "limit": 10
  }
}
```

## 6. 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和内容 |
| 401 | 未授权 | 检查Token是否有效 |
| 403 | 权限不足 | 检查用户权限 |
| 404 | 资源不存在 | 检查请求URL和资源ID |
| 409 | 资源冲突 | 用户名或邮箱已存在 |
| 422 | 参数验证失败 | 检查参数格式和约束 |
| 429 | 请求频率限制 | 减少请求频率 |
| 500 | 服务器内部错误 | 联系技术支持 |

### 具体错误代码

| 业务错误码 | 说明 |
|------------|------|
| IDENTITY_VERIFICATION_FAILED | 身份信息验证失败 |
| STUDENT_ID_NOT_FOUND | 学工号不存在 |
| INVALID_CREDENTIALS | 用户名或密码错误 |
| ACCOUNT_LOCKED | 账户被锁定 |
| ACCOUNT_SUSPENDED | 账户被暂停 |
| WEAK_PASSWORD | 密码强度不足 |
| TOKEN_EXPIRED | Token已过期 |
| TOKEN_INVALID | Token无效 |
| PERMISSION_DENIED | 权限不足 |
| ROLE_MISMATCH | 角色不匹配 |
| INSUFFICIENT_PERMISSION | 权限不足，无法执行操作 |
| CAPTCHA_REQUIRED | 需要验证码 |
| CAPTCHA_INVALID | 验证码错误 |
| INVALID_PHONE_FORMAT | 手机号格式错误 |
| VERIFICATION_CODE_REQUIRED | 需要验证码 |
| INVALID_VERIFICATION_CODE | 验证码错误或已过期 |
| VERIFICATION_CODE_EXPIRED | 验证码已过期 |
| SMS_SEND_FAILED | 短信发送失败 |
| SMS_FREQUENCY_LIMIT | 短信发送频率限制 |
| SMS_DAILY_LIMIT | 短信发送日限制 |

## 7. 数据库设计

### 7.1 用户表 (users)

```sql
CREATE TABLE users (
  id VARCHAR(50) PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  student_id VARCHAR(30) UNIQUE NOT NULL,
  user_type ENUM('student', 'teacher', 'department_admin', 'school_admin', 'super_admin') NOT NULL,
  school VARCHAR(100) NOT NULL,
  department_id INT,
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
  INDEX idx_department (department_id),
  INDEX idx_status (status)
);
```

### 7.2 用户预导入表 (user_imports)

**注意**: 根据新的注册流程，建议将用户预导入表分为学生表和教师表，以提高查询效率：

#### 学生预导入表 (student_imports)

```sql
CREATE TABLE student_imports (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  real_name VARCHAR(50) NOT NULL,
  student_id VARCHAR(30) UNIQUE NOT NULL,
  school VARCHAR(100) NOT NULL,
  department_id INT,
  department_name VARCHAR(100),
  class_name VARCHAR(50),
  grade VARCHAR(10),
  email VARCHAR(100),
  phone VARCHAR(20),
  is_used BOOLEAN DEFAULT false,
  imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  used_at TIMESTAMP NULL,
  INDEX idx_student_id (student_id),
  INDEX idx_school (school),
  INDEX idx_department (department_id),
  INDEX idx_is_used (is_used)
);
```

#### 教师预导入表 (teacher_imports)

```sql
CREATE TABLE teacher_imports (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  real_name VARCHAR(50) NOT NULL,
  teacher_id VARCHAR(30) UNIQUE NOT NULL,
  school VARCHAR(100) NOT NULL,
  department_id INT,
  department_name VARCHAR(100),
  job_title VARCHAR(50),
  employment_type ENUM('full_time', 'part_time', 'visiting') DEFAULT 'full_time',
  email VARCHAR(100),
  phone VARCHAR(20),
  is_used BOOLEAN DEFAULT false,
  imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  used_at TIMESTAMP NULL,
  INDEX idx_teacher_id (teacher_id),
  INDEX idx_school (school),
  INDEX idx_department (department_id),
  INDEX idx_is_used (is_used)
);
```

### 7.3 验证码表 (verification_codes)

```sql
CREATE TABLE verification_codes (
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
```

### 7.4 短信发送记录表 (sms_logs)

```sql
CREATE TABLE sms_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  phone VARCHAR(20) NOT NULL,
  type ENUM('register', 'reset_password', 'change_phone', 'login_notification') NOT NULL,
  content TEXT NOT NULL,
  status ENUM('pending', 'sent', 'failed', 'delivered') DEFAULT 'pending',
  provider VARCHAR(50) NOT NULL,
  provider_message_id VARCHAR(100),
  error_message TEXT,
  sent_at TIMESTAMP NULL,
  delivered_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_phone (phone),
  INDEX idx_type (type),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
);
```

### 7.5 角色变更记录表 (role_change_logs)

```sql
CREATE TABLE role_change_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR(50) NOT NULL,
  old_role ENUM('student', 'teacher', 'department_admin', 'school_admin', 'super_admin'),
  new_role ENUM('student', 'teacher', 'department_admin', 'school_admin', 'super_admin') NOT NULL,
  changed_by VARCHAR(50) NOT NULL,
  reason TEXT,
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (changed_by) REFERENCES users(id),
  INDEX idx_user_id (user_id),
  INDEX idx_changed_by (changed_by),
  INDEX idx_changed_at (changed_at)
);
```

## 8. 安全机制

### 8.1 身份认证安全

- **预导入验证**: 用户信息必须在系统预导入数据中存在
- **多重验证**: 姓名、学工号、学校、手机号四重验证
- **短信验证码**: 注册时必须通过手机验证码验证
- **验证码安全**: 6位随机数字，5分钟有效期，最多5次验证尝试
- **频率限制**: 60秒内只能发送一次，一天内最多5次
- **初始密码**: 系统生成强密码并通过短信发送
- **首次登录**: 强制修改初始密码

### 8.2 权限安全

- **层级权限**: 严格的管理员层级权限控制
- **操作审计**: 所有权限变更都有详细记录
- **最小权限**: 用户只拥有执行任务所需的最小权限
- **权限验证**: 每次API调用都进行权限验证

### 8.3 会话安全

- **Token机制**: JWT Token with 签名验证
- **会话超时**: 自动会话超时机制
- **设备管理**: 多设备登录管理和控制
- **强制下线**: 管理员可强制下线异常会话

### 8.4 数据安全

- **密码加密**: bcrypt 哈希加密存储
- **敏感信息**: 不在日志中记录敏感信息
- **数据脱敏**: 日志和响应中敏感信息脱敏
- **传输加密**: HTTPS 加密传输

## 9. 部署配置

### 9.1 环境变量

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=campus_sports
DB_USER=username
DB_PASS=password

# JWT配置
JWT_SECRET=your-super-secret-key-256-bits
JWT_EXPIRES_IN=24h
JWT_REFRESH_EXPIRES_IN=7d

# 短信服务配置
SMS_PROVIDER=aliyun
SMS_ACCESS_KEY=your-access-key
SMS_SECRET_KEY=your-secret-key
SMS_SIGN_NAME=校园体育系统
SMS_TEMPLATE_CODE=SMS_123456789

# 验证码配置
VERIFICATION_CODE_LENGTH=6
VERIFICATION_CODE_EXPIRES=300
VERIFICATION_CODE_MAX_ATTEMPTS=5
SMS_SEND_INTERVAL=60
SMS_DAILY_LIMIT=5

# 文件上传配置
UPLOAD_MAX_SIZE=10485760
AVATAR_MAX_SIZE=2097152
ALLOWED_MIME_TYPES=image/jpeg,image/png,image/gif

# 安全配置
BCRYPT_ROUNDS=12
MAX_LOGIN_ATTEMPTS=5
LOCKOUT_DURATION=900
CAPTCHA_THRESHOLD=3
```

### 9.2 数据预导入

系统部署前需要分别导入学生和教师基础数据：

```sql
-- 示例：导入学生数据
INSERT INTO student_imports (real_name, student_id, school, department_id, department_name, class_name, grade, email, phone) VALUES
('张三', '2024001001', '北京体育大学', 1, '体育教育学院', '体教24-1班', '2024', 'zhang@bsu.edu.cn', '13812345678'),
('李四', '2024001002', '北京体育大学', 1, '体育教育学院', '体教24-1班', '2024', 'lisi@bsu.edu.cn', '13812345679'),
('王五', '2024002001', '北京体育大学', 2, '运动训练学院', '训练24-1班', '2024', 'wangwu@bsu.edu.cn', '13812345680');

-- 示例：导入教师数据
INSERT INTO teacher_imports (real_name, teacher_id, school, department_id, department_name, job_title, employment_type, email, phone) VALUES
('李老师', 'T2024001', '北京体育大学', 1, '体育教育学院', '副教授', 'full_time', 'li@bsu.edu.cn', '13912345678'),
('王教授', 'T2024002', '北京体育大学', 1, '体育教育学院', '教授', 'full_time', 'wang@bsu.edu.cn', '13912345679'),
('赵讲师', 'T2024003', '北京体育大学', 2, '运动训练学院', '讲师', 'full_time', 'zhao@bsu.edu.cn', '13912345680');
```

### 9.3 初始化脚本

```sql
-- 创建默认超级管理员
INSERT INTO users (id, username, password_hash, real_name, student_id, user_type, school, status, is_first_login) VALUES 
('super_admin_001', 'superadmin', '$2b$12$...', '系统管理员', 'SUPER001', 'super_admin', '系统', 'active', false);

-- 创建默认院系
INSERT INTO departments (id, name, school) VALUES 
(1, '体育教育学院', '北京体育大学'),
(2, '运动训练学院', '北京体育大学'),
(3, '体育商学院', '北京体育大学');
```

## 11. API使用示例

### 11.1 前端集成

```javascript
import axios from 'axios'

// 创建API客户端
const apiClient = axios.create({
  baseURL: 'https://api.example.com/v1',
  timeout: 10000
})

// 用户认证流程
class AuthAPI {
  // 发送验证码
  static async sendVerificationCode(phone, type) {
    try {
      const response = await apiClient.post('/auth/send-verification-code', {
        phone,
        type
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '发送验证码失败')
    }
  }

  // 用户注册（集成学工号验证）
  static async register(userData) {
    try {
      const response = await apiClient.post('/auth/register', userData)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '认证失败')
    }
  }

  // 用户登录
  static async login(credentials) {
    try {
      const response = await apiClient.post('/auth/login', credentials)
      
      if (response.data.code === 200) {
        // 保存Token
        localStorage.setItem('token', response.data.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.data.user))
      }
      
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '登录失败')
    }
  }

  // 设置用户角色（管理员功能）
  static async setUserRole(userId, newRole, reason) {
    try {
      const response = await apiClient.put(`/admin/users/${userId}/role`, {
        userType: newRole,
        reason
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '角色设置失败')
    }
  }
}

export default AuthAPI
```

### 11.2 Vue组件示例

```vue
<template>
  <div class="register-form">
    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label>真实姓名</label>
        <input v-model="form.realName" required />
      </div>
      
      <div class="form-group">
        <label>用户类型</label>
        <select v-model="form.userType" required>
          <option value="">请选择用户类型</option>
          <option value="student">学生</option>
          <option value="teacher">教师</option>
        </select>
      </div>
      
      <div class="form-group">
        <label>所属学校</label>
        <select v-model="form.school" required>
          <option value="">请选择学校</option>
          <option value="北京体育大学">北京体育大学</option>
          <!-- 其他学校选项 -->
        </select>
      </div>
      
      <div class="form-group">
        <label>{{ form.userType === 'student' ? '学号' : form.userType === 'teacher' ? '工号' : '学号/工号' }}</label>
        <input v-model="form.studentId" required />
      </div>
      
      <div class="form-group">
        <label>手机号</label>
        <input v-model="form.phone" type="tel" required />
      </div>
      
      <button type="submit" :disabled="loading">
        {{ loading ? '认证中...' : '身份认证' }}
      </button>
    </form>
  </div>
</template>

<script>
import AuthAPI from '@/api/auth'

export default {
  data() {
    return {
      form: {
        realName: '',
        userType: '',
        school: '',
        studentId: '',
        phone: ''
      },
      loading: false
    }
  },
  
  methods: {
    async handleRegister() {
      this.loading = true
      
      try {
        const result = await AuthAPI.register(this.form)
        
        if (result.code === 200) {
          alert(`认证成功！用户名：${result.data.username}，角色：${result.data.userType}，初始密码已发送到手机`)
          this.$router.push('/login')
        } else {
          alert(result.message)
        }
      } catch (error) {
        alert(error.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
```

---

**文档版本**: v5.1.0  
**更新时间**: 2024-01-25  
**维护团队**: 后端开发组

**更新内容**:
- 🆕 **注册流程优化**：新增用户类型选择，支持学生和教师分别注册
- 📊 **数据库设计优化**：分离学生和教师预导入表，提高查询效率
- 🔍 **身份验证改进**：根据用户类型分别在对应表中验证学号/工号
- 📝 **API接口更新**：注册接口增加userType参数
- 🎯 **权限系统简化**：移除复杂的权限检查API，采用基于角色的简单判断
- 🚀 **开发效率提升**：前端直接根据角色控制UI显示，无需复杂权限计算
- 📊 **架构简化**：统一采用角色判断，提高系统一致性和可维护性
- ⚡ **性能优化**：移除权限API调用，减少网络请求，提升响应速度

**联系方式**:
- 技术支持: tech-support@school.edu.cn
- API问题: api-support@school.edu.cn 