# 简化听课系统 API 文档 v2.0

## 概述

本文档描述了重构后的简化听课系统API接口。新系统基于模板文件提交方式，去除了复杂的评价参数设置，提供更简洁的听课管理功能。

## 基础信息

- **基础URL**: `https://api.example.com/v1`
- **认证方式**: Bearer Token
- **数据格式**: JSON
- **编码**: UTF-8

## 用户角色说明

- `teacher`: 教师 - 可创建听课记录、下载模板、上传评价文件和视频
- `department_admin`: 院级管理员 - 具有教师权限，可查看本院系所有听课记录
- `school_admin`: 校级管理员 - 具有教师权限，可管理模板，查看全校听课记录
- `super_admin`: 超级管理员 - 拥有所有权限

## 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 1. 听课记录管理

### 1.1 获取听课记录列表

**接口**: `GET /listening/observations`

**权限**: 所有角色

**权限说明**:
- `teacher`: 只能查看自己作为听课者（observer）的记录，无法查看别人听自己课的记录
- `department_admin`, `school_admin`, `super_admin`: 可以查看相应权限范围内的所有听课记录

**请求参数**:
```json
{
  "keyword": "张老师",
  "department_id": 1,
  "start_date": "2024-01-01",
  "end_date": "2024-01-31",
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
    "items": [
      {
        "id": 1,
        "observer_name": "王主任",
        "observer_type": "department_admin",
        "observer_id": 3,
        "course_name": "体育与健康",
        "teacher_name": "李老师",
        "teacher_id": 5,
        "class_name": "高一(1)班",
        "class_date": "2024-01-15",
        "classroom": "操场",
        "evaluation_file": {
          "name": "听课评价表_20240115.xlsx",
          "path": "evaluations/eval_123456.xlsx",
          "type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "size": 51200,
          "uploaded_at": "2024-01-15T14:30:00Z"
        },
        "video_file": {
          "name": "听课视频_20240115.mp4",
          "path": "videos/video_123456.mp4",
          "url": "/api/files/download/videos/video_123456.mp4",
          "type": "video/mp4",
          "size": 104857600,
          "uploaded_at": "2024-01-15T15:00:00Z"
        },
        "created_at": "2024-01-15T10:00:00Z"
      }
    ],
    "total": 50,
    "page": 1,
    "totalPages": 5
  }
}
```

### 1.2 创建听课记录

**接口**: `POST /listening/observations`

**权限**: `teacher`, `department_admin`, `school_admin`, `super_admin`

**请求体**:
```json
{
  "class_date": "2024-01-15",
  "course_id": 1
}
```

**说明**: 听课者信息由后端根据JWT Token自动获取，无需前端传递。

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "created_at": "2024-01-15T10:00:00Z"
  }
}
```

### 1.3 获取听课记录详情

**接口**: `GET /listening/observations/{id}`

**权限**: 所有角色

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "observer_name": "王主任",
    "observer_type": "department_admin",
    "course_name": "体育与健康",
    "teacher_name": "李老师",
    "class_name": "高一(1)班",
    "class_date": "2024-01-15",
    "classroom": "操场",
    "evaluation_file": {
      "name": "听课评价表_20240115.xlsx",
      "path": "evaluations/eval_123456.xlsx",
      "type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "size": 51200,
      "uploaded_at": "2024-01-15T14:30:00Z"
    },
    "video_file": {
      "name": "听课视频_20240115.mp4",
      "path": "videos/video_123456.mp4",
      "url": "/api/files/download/videos/video_123456.mp4",
      "type": "video/mp4",
      "size": 104857600,
      "uploaded_at": "2024-01-15T15:00:00Z"
    },
    "created_at": "2024-01-15T10:00:00Z"
  }
}
```

### 1.4 删除听课记录

**接口**: `DELETE /listening/observations/{id}`

**权限**: `department_admin`, `school_admin`, `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

## 2. 课程搜索

### 2.1 搜索课程

**接口**: `GET /listening/courses/search`

**权限**: 所有角色

**请求参数**:
```json
{
  "keyword": "张老师",
  "date": "2024-01-15"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": [
    {
      "id": 1,
      "course_name": "体育与健康",
      "teacher_name": "张老师",
      "teacher_id": 5,
      "class_name": "高一(1)班",
      "classroom": "操场",
      "class_time": "08:00-09:40",
      "class_date": "2024-01-15"
    }
  ]
}
```

## 3. 文件管理

### 3.1 上传评价文件

**接口**: `POST /listening/observations/{id}/evaluation`

**权限**: `teacher`, `department_admin`, `school_admin`, `super_admin`

**请求类型**: `multipart/form-data`

**请求参数**:
```
evaluation_file: 评价文件 (必需)
```

**文件限制**:
- 支持格式: Excel (.xlsx, .xls), PDF (.pdf), Word (.doc, .docx)
- 最大大小: 50MB

**响应**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "file": {
      "name": "听课评价表_20240115.xlsx",
      "path": "evaluations/eval_123456.xlsx",
      "type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "size": 51200,
      "uploaded_at": "2024-01-15T14:30:00Z"
    }
  }
}
```

### 3.2 上传听课视频

**接口**: `POST /listening/observations/{id}/video`

**权限**: `teacher`, `department_admin`, `school_admin`, `super_admin`

**请求类型**: `multipart/form-data`

**请求参数**:
```
video_file: 视频文件 (必需)
```

**文件限制**:
- 支持格式: MP4 (.mp4), AVI (.avi), MOV (.mov)
- 最大大小: 500MB

**响应**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "file": {
      "name": "听课视频_20240115.mp4",
      "path": "videos/video_123456.mp4",
      "url": "/api/files/download/videos/video_123456.mp4",
      "type": "video/mp4",
      "size": 104857600,
      "uploaded_at": "2024-01-15T15:00:00Z"
    }
  }
}
```

## 4. 模板管理

### 4.1 获取模板列表

**接口**: `GET /listening/templates`

**权限**: `school_admin`, `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "听课评价表模板_v2.xlsx",
      "file_path": "templates/template_123456.xlsx",
      "file_type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "file_size": 30720,
      "is_default": true,
      "uploaded_by": "校级管理员",
      "uploaded_at": "2024-01-10T10:00:00Z"
    }
  ]
}
```

### 4.2 上传模板

**接口**: `POST /listening/templates`

**权限**: `school_admin`, `super_admin`

**请求类型**: `multipart/form-data`

**请求参数**:
```
template_file: 模板文件 (必需)
name: 模板名称 (必需)
```

**文件限制**:
- 支持格式: Excel (.xlsx, .xls)
- 最大大小: 10MB

**响应**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "name": "听课评价表模板_v2.xlsx",
    "file_path": "templates/template_123456.xlsx",
    "uploaded_at": "2024-01-10T10:00:00Z"
  }
}
```

### 4.3 下载模板

**接口**: `GET /listening/templates/{id}/download`

**权限**: 所有角色

**响应**: 直接返回文件流

### 4.4 下载默认模板

**接口**: `GET /listening/templates/default/download`

**权限**: 所有角色

**响应**: 直接返回文件流

### 4.5 设置默认模板

**接口**: `PUT /listening/templates/{id}/default`

**权限**: `school_admin`, `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "设置成功"
}
```

### 4.6 删除模板

**接口**: `DELETE /listening/templates/{id}`

**权限**: `school_admin`, `super_admin`

**响应**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

## 5. 管理员功能

### 5.1 获取管理员听课记录

**接口**: `GET /listening/admin/observations`

**权限**: `department_admin`, `school_admin`, `super_admin`

**请求参数**: 同 1.1

**数据权限**:
- `department_admin`: 可以查看本院系所有听课记录（包括作为听课者和被听课者的记录）
- `school_admin`: 可以查看全校所有听课记录
- `super_admin`: 可以查看所有数据

**与普通教师的区别**:
- 管理员可以查看完整的听课记录，包括别人听本院系/学校教师课的记录
- 普通教师只能查看自己作为听课者的记录

**响应**: 同 1.1

### 5.2 获取统计数据

**接口**: `GET /listening/admin/stats`

**权限**: `department_admin`, `school_admin`, `super_admin`

**请求参数**:
```json
{
  "start_date": "2024-01-01",
  "end_date": "2024-01-31",
  "department_id": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total_observations": 156,
    "this_month_count": 23,
    "teacher_count": 45,
    "evaluation_files": 134,
    "video_files": 89,
    "department_stats": [
      {
        "department_id": 1,
        "department_name": "体育学院",
        "observation_count": 78,
        "teacher_count": 25,
        "evaluation_files": 67,
        "video_files": 45
      }
    ]
  }
}
```

### 5.3 获取院系列表

**接口**: `GET /listening/departments`

**权限**: 所有角色

**响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "体育学院",
      "code": "PE",
      "description": "体育教育专业院系"
    }
  ]
}
```

## 6. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 413 | 文件过大 |
| 415 | 不支持的文件类型 |
| 422 | 参数验证失败 |
| 500 | 服务器内部错误 |

## 7. 权限控制矩阵

| 功能 | 教师 | 院级管理员 | 校级管理员 | 超级管理员 |
|------|------|------------|------------|------------|
| 查看自己的听课记录 | ✅ (仅作为听课者) | ✅ | ✅ | ✅ |
| 查看所有听课记录 | ❌ | ✅ (本院系) | ✅ (全校) | ✅ |
| 创建听课记录 | ✅ | ✅ | ✅ | ✅ |
| 删除听课记录 | ❌ | ✅ | ✅ | ✅ |
| 上传评价文件 | ✅ | ✅ | ✅ | ✅ |
| 上传听课视频 | ✅ | ✅ | ✅ | ✅ |
| 下载模板 | ✅ | ✅ | ✅ | ✅ |
| 上传模板 | ❌ | ❌ | ✅ | ✅ |
| 管理模板 | ❌ | ❌ | ✅ | ✅ |
| 查看统计数据 | ❌ | ✅ (本院系) | ✅ | ✅ |

### 权限说明

- **教师权限限制**: 
  - 只能查看自己作为听课者的记录（即自己去听别人的课）
  - 无法查看别人听自己课的记录
  - 无法查看其他教师之间的听课记录

- **管理员权限**:
  - 可以查看权限范围内的所有听课记录
  - 包括作为听课者和被听课者的完整记录
  - 可以进行统计分析和管理操作

## 8. 文件管理规范

### 8.1 支持的文件类型

**评价文件**:
- Excel: .xlsx, .xls
- PDF: .pdf
- Word: .doc, .docx

**视频文件**:
- MP4: .mp4 (推荐)
- AVI: .avi
- MOV: .mov

**模板文件**:
- Excel: .xlsx, .xls

### 8.2 文件大小限制

- 评价文件: 最大 50MB
- 视频文件: 最大 500MB
- 模板文件: 最大 10MB

### 8.3 存储路径结构

```
/var/www/storage/listening/
├── evaluations/           # 评价文件
│   ├── eval_20240115_123456.xlsx
├── videos/               # 听课视频
│   ├── video_20240115_123456.mp4
└── templates/            # 模板文件
    ├── template_20240115_123456.xlsx
```

## 9. API调用示例

### 9.1 创建听课记录

```javascript
const createObservation = async (classDate, courseId) => {
  try {
    const response = await fetch('/api/listening/observations', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        class_date: classDate,
        course_id: courseId
      })
    })
    
    const result = await response.json()
    console.log('创建成功:', result.data)
  } catch (error) {
    console.error('创建失败:', error)
  }
}
```

### 9.2 上传评价文件

```javascript
const uploadEvaluationFile = async (observationId, file) => {
  const formData = new FormData()
  formData.append('evaluation_file', file)
  
  try {
    const response = await fetch(`/api/listening/observations/${observationId}/evaluation`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    
    const result = await response.json()
    console.log('上传成功:', result.data)
  } catch (error) {
    console.error('上传失败:', error)
  }
}
```

### 9.3 下载模板

```javascript
const downloadTemplate = async () => {
  try {
    const response = await fetch('/api/listening/templates/default/download', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '听课评价表模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载失败:', error)
  }
}
```

## 10. 版本更新日志

### v2.0.0 (2024-01-15)
- 🔄 **重大重构**: 简化听课管理功能
- ✨ 基于模板文件的评价方式
- 🗑️ 移除复杂的评价参数设置
- 📝 简化课程搜索功能
- 📋 新增模板管理功能
- 🎥 保留视频上传功能
- 🔐 简化权限控制
- 📊 简化统计数据

## 11. 注意事项

1. **模板管理**：由校级管理员统一管理听课模板
2. **文件提交**：评价基于下载模板填写后上传文件
3. **课程搜索**：根据听课者姓名和上课日期搜索
4. **权限控制**：教师只能查看自己作为听课者的记录，无法查看别人听自己课的记录
5. **数据统计**：移除复杂的评分统计，专注文件数量统计
6. **文件存储**：所有文件存储在服务器本地
7. **用户识别**：听课者信息通过JWT Token自动识别，无需手动输入 