# 比赛成绩管理端接口文档

> **服务地址**: `http://38.207.179.218:8888`
>
> **认证方式**: Bearer Token（由登录接口 `/auth/login` 获取）
>
> **Content-Type**: `application/json`
>
> **字符集**: UTF-8
>
> **模块说明**: 本模块完全独立，与其它学生信息（users1）无任何关联，成绩数据存储于独立的 `race_results` 表。

---

## 权限说明

| 角色 | 可查看数据范围 | 删除权限 |
|------|--------------|---------|
| `teacher` | 仅本校成绩 | ✗ |
| `department_admin` | 仅本校成绩 | ✓ |
| `school_admin` | 仅本校成绩 | ✓ |
| `super_admin` | 全部学校成绩（可按 school 参数筛选） | ✓ |

---

## 接口列表

| # | 方法 | 路径 | 描述 | 最低权限 |
|---|------|------|------|---------|
| 1 | GET | `/race/results` | 分页查询成绩列表 | teacher |
| 2 | GET | `/race/results/statistics` | 成绩统计数据 | teacher |
| 3 | GET | `/race/results/{id}` | 查询单条成绩详情 | teacher |
| 4 | DELETE | `/race/results/{id}` | 删除单条成绩 | department_admin |
| 5 | DELETE | `/race/results/batch` | 批量删除成绩 | department_admin |

---

## 1. 分页查询成绩列表

### 接口信息

| 项目 | 内容 |
|------|------|
| **接口路径** | `GET /race/results` |
| **权限** | teacher / department_admin / school_admin / super_admin |

### 请求头

```http
Authorization: Bearer <token>
```

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `page` | integer | 否 | 页码，从 1 开始，默认 `1` |
| `pageSize` | integer | 否 | 每页条数，默认 `20` |
| `school` | string | 否 | 学校名称过滤（**仅 super_admin 生效**，其他角色固定本校） |
| `gender` | string | 否 | 性别过滤：`男` 或 `女` |
| `teacherName` | string | 否 | 教师姓名（模糊匹配） |
| `keyword` | string | 否 | 关键词（模糊匹配学号或姓名） |
| `startDate` | string | 否 | 上传日期起，格式 `yyyy-MM-dd` |
| `endDate` | string | 否 | 上传日期止，格式 `yyyy-MM-dd` |

### 成功响应

**HTTP 状态码**: `200 OK`

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "studentNumber": "2024001001",
        "name": "张三",
        "school": "北京体育大学",
        "gender": "男",
        "totalLaps": 3,
        "finalTime": "03:24.56",
        "finalTimeMs": 204560,
        "finished": true,
        "teacherName": "李老师",
        "uploaderId": "user_001",
        "uploadedAt": "2026-03-19T13:24:10",
        "createdAt": "2026-03-19T21:24:12"
      },
      {
        "id": 2,
        "studentNumber": "2024001002",
        "name": "李四",
        "school": "北京体育大学",
        "gender": "女",
        "totalLaps": 1,
        "finalTime": null,
        "finalTimeMs": null,
        "finished": false,
        "teacherName": "李老师",
        "uploaderId": "user_001",
        "uploadedAt": "2026-03-19T13:24:10",
        "createdAt": "2026-03-19T21:24:12"
      }
    ],
    "total": 128,
    "page": 1,
    "pageSize": 20,
    "totalPages": 7
  },
  "timestamp": "2026-03-19T21:24:15"
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 成绩条目列表 |
| `data.list[].id` | integer | 记录主键 ID |
| `data.list[].studentNumber` | string | 学号 |
| `data.list[].name` | string | 运动员姓名 |
| `data.list[].school` | string | 所属学校 |
| `data.list[].gender` | string | 性别：`男` / `女` |
| `data.list[].totalLaps` | integer | 实际完成圈数 |
| `data.list[].finalTime` | string \| null | 成绩格式化字符串，未完赛为 `null` |
| `data.list[].finalTimeMs` | integer \| null | 成绩毫秒数，未完赛为 `null` |
| `data.list[].finished` | boolean | 是否完赛（`finalTimeMs != null`） |
| `data.list[].teacherName` | string | 上传教师姓名 |
| `data.list[].uploaderId` | string | 上传者用户 ID |
| `data.list[].uploadedAt` | string | 比赛上传时间（ISO 8601，东八区） |
| `data.list[].createdAt` | string | 记录入库时间（ISO 8601，东八区） |
| `data.total` | integer | 符合条件的总记录数 |
| `data.page` | integer | 当前页码 |
| `data.pageSize` | integer | 每页条数 |
| `data.totalPages` | integer | 总页数 |

---

## 2. 成绩统计数据

### 接口信息

| 项目 | 内容 |
|------|------|
| **接口路径** | `GET /race/results/statistics` |
| **权限** | teacher / department_admin / school_admin / super_admin |

### 请求头

```http
Authorization: Bearer <token>
```

### 成功响应

**HTTP 状态码**: `200 OK`

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalCount": 128,
    "finishedCount": 110,
    "unfinishedCount": 18,
    "school": "北京体育大学"
  },
  "timestamp": "2026-03-19T21:24:15"
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.totalCount` | integer | 该学校（或全局）总成绩记录数 |
| `data.finishedCount` | integer | 完赛（有成绩）人数 |
| `data.unfinishedCount` | integer | 未完赛（无成绩）人数 |
| `data.school` | string \| null | 统计范围对应的学校名称；super_admin 且无筛选时为 `null`（表示全校） |

---

## 3. 查询单条成绩详情

### 接口信息

| 项目 | 内容 |
|------|------|
| **接口路径** | `GET /race/results/{id}` |
| **权限** | teacher / department_admin / school_admin / super_admin |

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | integer | 是 | 成绩记录主键 ID |

### 请求头

```http
Authorization: Bearer <token>
```

### 成功响应

**HTTP 状态码**: `200 OK`

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "studentNumber": "2024001001",
    "name": "张三",
    "school": "北京体育大学",
    "gender": "男",
    "totalLaps": 3,
    "finalTime": "03:24.56",
    "finalTimeMs": 204560,
    "finished": true,
    "teacherName": "李老师",
    "uploaderId": "user_001",
    "uploadedAt": "2026-03-19T13:24:10",
    "createdAt": "2026-03-19T21:24:12"
  },
  "timestamp": "2026-03-19T21:24:15"
}
```

---

## 4. 删除单条成绩

### 接口信息

| 项目 | 内容 |
|------|------|
| **接口路径** | `DELETE /race/results/{id}` |
| **权限** | department_admin / school_admin / super_admin |

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | integer | 是 | 成绩记录主键 ID |

### 请求头

```http
Authorization: Bearer <token>
```

### 成功响应

**HTTP 状态码**: `200 OK`

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": "2026-03-19T21:24:15"
}
```

---

## 5. 批量删除成绩

### 接口信息

| 项目 | 内容 |
|------|------|
| **接口路径** | `DELETE /race/results/batch` |
| **权限** | department_admin / school_admin / super_admin |

### 请求头

```http
Authorization: Bearer <token>
Content-Type: application/json
```

### 请求体

```json
{
  "ids": [1, 2, 3, 10, 20]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `ids` | array\<integer\> | 是 | 要删除的成绩记录 ID 列表，至少包含 1 个 |

### 成功响应

**HTTP 状态码**: `200 OK`

```json
{
  "code": 200,
  "message": "批量删除完成",
  "data": {
    "deletedCount": 5
  },
  "timestamp": "2026-03-19T21:24:15"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.deletedCount` | integer | 实际成功删除的条数（无权限或不存在的记录会被跳过） |

---

## 通用错误响应

### 401 未认证

```json
{
  "code": 401,
  "message": "未登录或 Token 已过期"
}
```

### 403 权限不足

```json
{
  "code": 403,
  "message": "无权执行此操作，需要管理员权限"
}
```

### 404 记录不存在

```json
{
  "code": 404,
  "message": "成绩记录不存在"
}
```

### 500 服务器内部错误

```json
{
  "code": 500,
  "message": "服务器内部错误"
}
```

---

## 附录：数据库表结构

```sql
CREATE TABLE race_results (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    student_number  VARCHAR(50)  NOT NULL,          -- 学号
    name            VARCHAR(100) NOT NULL,           -- 姓名
    school          VARCHAR(200) NOT NULL,           -- 学校（来自上传教师归属学校）
    gender          VARCHAR(10)  NOT NULL,           -- 性别：男 / 女
    total_laps      INT          NOT NULL DEFAULT 0, -- 实际完成圈数
    final_time      VARCHAR(20)  NULL,               -- 成绩字符串，未完赛为 NULL
    final_time_ms   BIGINT       NULL,               -- 成绩毫秒，未完赛为 NULL
    teacher_name    VARCHAR(100) NOT NULL,           -- 上传教师姓名
    uploader_id     VARCHAR(50)  NOT NULL,           -- 上传者用户 ID
    uploaded_at     DATETIME     NOT NULL,           -- 比赛时间（客户端传入）
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

> **说明**：
> - `finished` 字段为前端展示字段，由后端根据 `finalTimeMs` 是否为 `null` 计算得出，不存储在数据库中。
> - 本模块不依赖 `users1` 表或其它学生信息表，所有运动员数据均由上位机上传时携带。
> - 时间字段统一使用东八区（Asia/Shanghai）返回给前端。
