# 成绩上传接口文档

> **服务地址**: `http://38.207.179.218:8888`
>
> **认证方式**: Bearer Token（由登录接口 `/auth/login` 获取）
>
> **Content-Type**: `application/json`
>
> **字符集**: UTF-8

---

## 1. 上传比赛成绩

### 接口信息

| 项目 | 内容 |
|------|------|
| **接口路径** | `POST /race/results` |
| **权限** | 需要登录（teacher / department_admin / school_admin / super_admin） |
| **调用时机** | 上位机点击"结束比赛"并完成最终判圈算法后自动调用 |

---

### 请求头

```http
Authorization: Bearer <token>
Content-Type: application/json
```

---

### 请求体

```json
{
  "uploadedAt": "2026-03-19T13:24:10.000Z",
  "results": [
    {
      "studentNumber": "2024001001",
      "name": "张三",
      "school": "北京体育大学",
      "gender": "男",
      "totalLaps": 3,
      "finalTime": "03:24.56",
      "finalTimeMs": 204560,
      "teacherName": "李老师"
    },
    {
      "studentNumber": "2024001002",
      "name": "李四",
      "school": "北京体育大学",
      "gender": "女",
      "totalLaps": 1,
      "finalTime": null,
      "finalTimeMs": null,
      "teacherName": "李老师"
    }
  ]
}
```

### 请求体字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `uploadedAt` | string (ISO 8601) | 是 | 上传时间（UTC） |
| `results` | array | 是 | 成绩列表，至少包含 1 条 |
| `results[].studentNumber` | string | 是 | 学号，来自运动员二维码 `sn` 字段 |
| `results[].name` | string | 是 | 运动员姓名 |
| `results[].school` | string | 是 | 学校名称，**取自当前登录教师的所属学校**，非运动员字段 |
| `results[].gender` | string | 是 | 性别，固定值：`"男"` 或 `"女"` |
| `results[].totalLaps` | integer | 是 | 实际完成圈数 |
| `results[].finalTime` | string \| null | 是 | 最终成绩格式化字符串，格式 `"mm:ss.ff"`（如 `"03:24.56"`）；**未完赛时为 `null`** |
| `results[].finalTimeMs` | integer \| null | 是 | 最终成绩毫秒数（如 `204560`）；**未完赛时为 `null`** |
| `results[].teacherName` | string | 是 | 上传教师（或管理员）的真实姓名，**取自当前登录用户的 `realName` 字段** |

> **说明**：
> - `finalTime` 和 `finalTimeMs` 同时为 `null` 表示该运动员**未完成规定圈数**（中途退出或未识别到信号），仍需上传记录。
> - `school` 字段由教师端自动填充，不从运动员二维码中读取，确保学校信息以登录教师归属学校为准。

---

### 成功响应

**HTTP 状态码**: `200 OK`

```json
{
  "code": 200,
  "message": "成绩上传成功",
  "data": {
    "uploadedCount": 28,
    "failedCount": 2,
    "failedItems": [
      {
        "studentNumber": "2024001099",
        "reason": "学号不存在"
      },
      {
        "studentNumber": "",
        "reason": "学号不能为空"
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | integer | 状态码，200 表示成功 |
| `message` | string | 提示信息 |
| `data.uploadedCount` | integer | 成功入库的条数 |
| `data.failedCount` | integer | 入库失败的条数（如学号不存在、数据格式错误等） |
| `data.failedItems` | array | 失败条目详情，可为空数组 |
| `data.failedItems[].studentNumber` | string | 失败的学号 |
| `data.failedItems[].reason` | string | 失败原因 |

---

### 错误响应

#### 401 未认证

```json
{
  "code": 401,
  "message": "未登录或 Token 已过期",
  "error": "UNAUTHORIZED"
}
```

#### 403 权限不足

```json
{
  "code": 403,
  "message": "无权上传成绩，请使用教师或管理员账号",
  "error": "FORBIDDEN"
}
```

#### 400 请求参数错误

```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": "VALIDATION_ERROR",
  "details": [
    {
      "field": "results",
      "message": "results 不能为空"
    }
  ]
}
```

#### 500 服务器内部错误

```json
{
  "code": 500,
  "message": "服务器内部错误",
  "error": "INTERNAL_SERVER_ERROR"
}
```

---

## 2. 业务规则说明

### 2.1 上传时机

1. 教师点击上位机软件中的**"结束比赛"**按钮
2. 系统弹出确认弹窗，教师确认后触发最终判圈算法
3. 判圈算法执行完毕后，**自动调用本接口**上传全部运动员成绩
4. 上传失败时会弹窗提示，教师可通过界面中的**"手动上传成绩"**按钮重试

### 2.2 学校归属

- 学校信息**不从运动员二维码**中读取
- 学校以**当前登录教师的所属学校**为准（来自 `POST /auth/login` 响应中的 `user.school` 字段）
- 目的是防止跨校数据污染，确保数据归属正确

### 2.3 未完赛运动员

- 若运动员完成圈数 < 目标圈数，`finalTime` 和 `finalTimeMs` 均为 `null`
- 后端应正常接收并入库，标记为未完赛状态

### 2.4 重复上传

- 同一场比赛（同一批运动员、同一教师、同一时间段）**可能重复上传**（用户重试时）
- 建议后端以 `studentNumber` + `uploadedAt` 做幂等处理，或允许覆盖写入

---

## 3. 前端发送示例（C# HttpClient）

```csharp
// POST /race/results
using var request = new HttpRequestMessage(HttpMethod.Post, "race/results");
request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
request.Content = JsonContent.Create(new {
    uploadedAt = DateTime.UtcNow.ToString("O"),
    results = results   // List<RaceResultItem>
});

var response = await _http.SendAsync(request);
var json = await response.Content.ReadAsStringAsync();
```

---

## 4. 附录：成绩字段来源映射

| 请求字段 | 上位机来源 |
|----------|-----------|
| `studentNumber` | 运动员二维码 `sn` 字段 |
| `name` | 运动员姓名（手动录入或二维码解析） |
| `school` | 当前登录教师的 `user.school` |
| `gender` | 运动员性别（`1`=男，`2`=女，转为中文字符串） |
| `totalLaps` | `AthleteViewModel.TotalLapsDone`（实际完成圈数） |
| `finalTime` | `AthleteViewModel.FormattedFinalTime`（如 `"03:24.56"`） |
| `finalTimeMs` | `AthleteViewModel.LastLapRaceTime.TotalMilliseconds` |
| `teacherName` | 当前登录用户的 `user.realName` |
| `uploadedAt` | 调用上传时的 `DateTime.UtcNow` |
