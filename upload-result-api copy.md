# 成绩上传接口文档（Excel 文件版）

> **服务地址**: `http://38.207.179.218:8888`
>
> **认证方式**: Bearer Token（由登录接口 `POST /auth/login` 获取）
>
> **Content-Type**: `multipart/form-data`

---

## 1. 登录接口（已有，供参考）

### `POST /auth/login`

**权限**: 公开

**请求体** (`application/json`):

```json
{
  "username": "zhang2024001",
  "password": "TempPass123",
  "userType": "teacher",
  "rememberMe": false
}
```

**响应**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "user": {
      "id": "usr_abc123",
      "username": "zhang2024001",
      "realName": "张三",
      "userType": "teacher",
      "school": "山东科技大学",
      "departmentName": "体育学院"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2026-03-20T10:30:00Z"
  }
}
```

---

## 2. 成绩上传接口

### `POST /race/results/upload`

**权限**: 需要登录（teacher / department_admin / school_admin / super_admin）

**Content-Type**: `multipart/form-data`

### 请求头

```http
Authorization: Bearer <token>
Content-Type: multipart/form-data; boundary=----FormBoundary...
```

### 表单字段

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `file` | 文件 | 是 | `.xlsx` 格式的成绩文件，文件名格式：`{学校名}_{yyyyMMdd_HHmmss}.xlsx`，例如 `山东科技大学_20260322_180007.xlsx` |
| `school` | string | 是 | 学校名称，取自**当前登录用户**的 `school` 字段 |
| `teacherName` | string | 是 | 上传教师姓名，取自当前登录用户的 `realName` |
| `uploadedAt` | string (ISO 8601) | 是 | 上传时间（UTC），如 `2026-03-22T10:00:07.839Z` |

### Excel 文件结构

上传的 `.xlsx` 文件固定格式如下：

**元信息区（第 1–3 行）**：

| A 列 | B 列 |
|------|------|
| 学校 | 山东科技大学 |
| 上传教师 | 吴文曦 |
| 上传时间 | 2026-03-22 18:00:07 |

**表头行（第 5 行，加粗紫色底色）**：

| 姓名 | 学号 | 性别 | 完成圈数 | 最终成绩 |
|------|------|------|----------|----------|

**数据行（第 6 行起）**，按完成圈数降序、成绩升序排列：

| 张三 | 2024001001 | 男 | 4 | 03:24.56 |
|------|------------|----|----|----------|
| 李四 | 2024001002 | 女 | 2 | -- |
| tyband_95FC | -- | 男 | 3 | -- |

> **说明**：
> - `最终成绩` 为 `--` 表示该运动员**未完成规定圈数**（未完赛）
> - `学号` 为 `--` 表示该运动员未录入学号（如仅绑定了手环但未扫码）
> - 文件由上位机在教师点击「上传成绩」并确认后自动生成，**后端无需解析文件名中的学校，以 `school` 表单字段为准**

---

### 成功响应

**HTTP 200**

```json
{
  "code": 200,
  "message": "成绩上传成功",
  "data": {
    "uploadedCount": 26,
    "failedCount": 2,
    "failedItems": [
      {
        "studentNumber": "2024001099",
        "reason": "学号不存在"
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.uploadedCount` | integer | 成功入库条数 |
| `data.failedCount` | integer | 失败条数（学号无效、格式错误等） |
| `data.failedItems` | array | 失败明细，可为空数组 |

---

### 错误响应

| HTTP 状态码 | code | 场景 |
|-------------|------|------|
| 401 | 401 | Token 无效或已过期 |
| 403 | 403 | 用户角色无权上传 |
| 400 | 400 | 缺少必填字段、文件格式错误等 |
| 500 | 500 | 服务器内部错误 |

**示例（400）**：

```json
{
  "code": 400,
  "message": "缺少必填字段 school",
  "error": "VALIDATION_ERROR"
}
```

---

## 3. 业务规则

### 3.1 文件命名

文件名格式：`{学校名}_{yyyyMMdd_HHmmss}.xlsx`，例如：

```
山东科技大学_20260322_180007.xlsx
```

后端可用文件名做日志记录，但学校归属以 `school` 表单字段为准。

### 3.2 学校归属

- 学校取自**当前登录用户**的 `school`，不从运动员信息读取
- 确保跨校数据不被混淆

### 3.3 未完赛运动员

- `最终成绩` 列为 `--` 表示未完赛，后端应入库并标记未完赛状态
- 仍需正常接收，不应返回错误

### 3.4 上传触发时机

1. 教师完成比赛 → 点击「结束比赛」→ 最终判圈算法执行完毕
2. 点击「**上传成绩**」按钮
3. 确认弹窗（含文件名、教师、学校预览）→ 点「是」
4. 上位机在内存中生成 Excel → 以 `multipart/form-data` 上传

### 3.5 重复上传处理

同一学校可能多次上传（教师重试时），建议后端以 `uploadedAt` + `school` 做幂等处理或按时间戳覆盖写入。

---

## 4. 前端发送示例（C# HttpClient）

```csharp
using var content = new MultipartFormDataContent();

// Excel 文件
var fileContent = new StreamContent(excelStream);
fileContent.Headers.ContentType = new MediaTypeHeaderValue(
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
content.Add(fileContent, "file", fileName);    // fileName = "山东科技大学_20260322_180007.xlsx"

// 额外字段
content.Add(new StringContent(school,       Encoding.UTF8), "school");
content.Add(new StringContent(teacherName,  Encoding.UTF8), "teacherName");
content.Add(new StringContent(uploadedAt,   Encoding.UTF8), "uploadedAt");  // ISO 8601 UTC

using var request = new HttpRequestMessage(HttpMethod.Post, "race/results/upload");
request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
request.Content = content;

var response = await httpClient.SendAsync(request);
```

---

## 5. Excel 列与数据源映射

| Excel 列 | 数据来源（上位机） |
|----------|-------------------|
| 姓名 | 运动员姓名（手动录入或二维码解析），无名时显示设备 ID |
| 学号 | 二维码 `sn` 字段，未扫码时为 `--` |
| 性别 | 运动员性别（`男`/`女`） |
| 完成圈数 | 实际完成圈数（`TotalLapsDone`） |
| 最终成绩 | 格式 `mm:ss.ff`（如 `03:24.56`），未完赛为 `--` |
| school 字段 | 登录用户的 `user.school` |
| teacherName 字段 | 登录用户的 `user.realName` |
