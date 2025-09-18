# PE校园管理端API使用说明

## 概述

PE校园管理端已成功集成到现有的听课系统项目中，提供完整的PE活动、用户、早操管理功能。

## 权限说明

### 管理员权限
PE管理端使用原有登录系统的权限控制：
- **超级管理员** (`super_admin`): 可以管理所有学校的PE数据
- **校级管理员** (`school_admin`): 只能管理本校的PE数据
- **其他角色**: 无PE管理权限

### 数据隔离
- 校级管理员只能查看和管理本校学生的PE数据
- 超级管理员可以跨学校管理PE数据

## API基础路径

```
基础URL: http://localhost:9999/api/pe
```

## 认证方式

所有API都需要在请求头中包含有效的JWT Token：

```
Authorization: Bearer <your_token_here>
```

## 主要API模块

### 1. 活动管理 (`/api/pe/activities`)

- `GET /activities` - 获取活动列表
- `GET /activities/{id}` - 获取活动详情
- `PUT /activities/{id}/review` - 审核活动
- `GET /activities/{id}/attendance` - 获取活动签到记录

### 2. 用户管理 (`/api/pe/users`)

- `GET /users` - 获取用户列表
- `GET /users/{id}` - 获取用户详情
- `PUT /users/{id}/role` - 设置用户角色
- `GET /users/{id}/points-history` - 获取用户积分历史

### 3. 早操管理 (`/api/pe/morning-exercises`)

- `GET /morning-exercises` - 获取早操活动列表
- `GET /morning-exercises/{id}` - 获取早操活动详情
- `POST /morning-exercises` - 创建早操活动
- `PUT /morning-exercises/{id}` - 更新早操活动
- `DELETE /morning-exercises/{id}` - 删除早操活动
- `GET /morning-exercises/{id}/attendance` - 获取早操考勤记录

### 4. 签到记录管理 (`/api/pe/attendance-records`)

- `GET /attendance-records` - 获取签到记录列表
- `GET /attendance-records/{id}` - 获取签到记录详情
- `GET /attendance-records/export` - 导出签到记录

### 5. 统计数据 (`/api/pe/statistics`)

- `GET /statistics` - 获取总体统计
- `GET /statistics/activities` - 获取活动统计
- `GET /statistics/users` - 获取用户统计
- `GET /statistics/morning-exercises` - 获取早操统计
- `GET /statistics/attendance` - 获取签到统计

## 使用示例

### 获取活动列表

```bash
curl -X GET "http://localhost:9999/api/pe/activities?page=1&pageSize=20&approvalStatus=PENDING" \
     -H "Authorization: Bearer your_token_here" \
     -H "Content-Type: application/json"
```

### 审核活动

```bash
curl -X PUT "http://localhost:9999/api/pe/activities/{activity_id}/review" \
     -H "Authorization: Bearer your_token_here" \
     -H "Content-Type: application/json" \
     -d '{
       "status": "APPROVED",
       "comment": "活动内容符合要求，通过审核"
     }'
```

### 创建早操活动

```bash
curl -X POST "http://localhost:9999/api/pe/morning-exercises" \
     -H "Authorization: Bearer your_token_here" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "早操考勤",
       "description": "2024年1月15日早操考勤",
       "location": "操场",
       "date": "2024-01-15",
       "startTime": "2024-01-15T06:30:00Z",
       "endTime": "2024-01-15T07:30:00Z"
     }'
```

## 数据库表映射

PE管理端使用以下数据库表：
- `users1` - PE用户信息
- `activities` - PE活动信息
- `attendance_records` - 签到记录
- `morning_exercises` - 早操活动
- `morning_exercise_attendance` - 早操考勤记录
- `points_records` - 积分记录

## 错误处理

API使用统一的错误响应格式：

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null,
  "timestamp": "2024-01-01T12:00:00Z"
}
```

常见错误：
- `400` - 请求参数错误
- `401` - 未认证或Token无效
- `403` - 权限不足
- `404` - 资源不存在
- `500` - 服务器内部错误

## 测试工具

项目根目录提供了API测试脚本：
- `test-pe-api.bat` (Windows)
- `test-pe-api.sh` (Linux/Mac)

使用前需要：
1. 启动项目 (`mvn spring-boot:run` 或在IDE中运行)
2. 获取有效的管理员Token
3. 替换脚本中的Token值
4. 运行测试脚本

## 注意事项

1. **权限控制**: 所有API都会根据当前用户的权限级别自动过滤数据
2. **分页参数**: 列表接口支持分页，默认页码从1开始
3. **日期格式**: 统一使用ISO 8601格式 (`yyyy-MM-dd'T'HH:mm:ss'Z'`)
4. **数据源**: PE管理端使用独立的数据表，与原听课系统数据隔离
5. **跨域**: 所有Controller都配置了 `@CrossOrigin(origins = "*")`

## 后续扩展

PE管理端的架构支持以下扩展：
1. 添加院级管理员权限（需修改权限服务）
2. 实现Excel导出功能（目前为占位符实现）
3. 添加更多统计维度
4. 支持更复杂的活动审核流程




