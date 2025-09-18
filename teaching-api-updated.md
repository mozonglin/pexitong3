# 教学模块 API 文档 (更新版)

## 概述

教学模块主要用于管理员查看教师上课签到情况，包括课程管理、签到监督、拍照记录查看等功能。



## 基础信息

- **基础URL**: `http://127.0.0.1:9999`
- **认证方式**: Bearer Token
- **请求格式**: JSON
- **响应格式**: JSON

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-12-19T10:00:00Z"
}
```

## API 接口列表

### 1. 管理员功能

#### 1.1 获取课程列表

**接口地址**: `GET /teaching/courses`

**功能描述**: 管理员获取课程列表，支持分页、筛选和搜索

**请求参数**:
```json
{
  "page": 1,          // 页码，默认1
  "pageSize": 12,     // 每页数量，默认12
  "date": "2024-12-19", // 查询日期，格式YYYY-MM-DD
  "status": "",        // 签到状态：completed/pending/late，可选
  "search": ""         // 搜索关键词（课程名称、教师姓名），可选
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "course_001",
      "name": "高等数学",
      "teacherId": "teacher_001",
      "teacherName": "张教授",
      "date": "2024-12-19",
      "startTime": "08:00",
      "endTime": "09:40",
      "location": "教学楼A101",
      "attendanceStatus": "completed", // completed已签到 pending未签到 late迟到
      "attendanceTime": "2024-12-19T07:55:00Z",
      "attendanceLocation": "教学楼A101",
      "attendanceNote": "",
      "attendancePhotoUrl": "https://example.com/photo.jpg"
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 12,
  "pages": 5
}
```

#### 1.2 获取课程详情

**接口地址**: `GET /teaching/courses/{courseId}`

**功能描述**: 获取指定课程的详细信息

**路径参数**:
- `courseId`: 课程ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "course_001",
    "name": "高等数学",
    "teacherId": "teacher_001",
    "teacherName": "张教授",
    "date": "2024-12-19",
    "startTime": "08:00",
    "endTime": "09:40",
    "location": "教学楼A101",
    "attendanceStatus": "completed",
    "attendanceTime": "2024-12-19T07:55:00Z",
    "attendanceLocation": "教学楼A101",
    "attendanceNote": "正常签到",
    "attendancePhotoUrl": "https://example.com/photo.jpg"
  }
}
```

#### 1.3 获取统计数据

**接口地址**: `GET /teaching/statistics`

**功能描述**: 获取教学签到统计数据

**请求参数**:
```json
{
  "date": "2024-12-19", // 统计日期，可选，默认今天
  "period": "day"       // 统计周期：day/week/month，可选，默认day
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCourses": 120,      // 总课程数
    "todayAttendance": 85,    // 今日签到数
    "attendanceRate": 85.5,   // 签到率（百分比）
    "totalTeachers": 45,      // 教师总数
    "onTimeCount": 80,        // 准时签到数
    "lateCount": 5,           // 迟到数
    "missedCount": 35         // 未签到数
  }
}
```

### 2. 签到照片管理

#### 2.1 获取单个课程签到照片

**接口地址**: `GET /teaching/courses/{courseId}/attendance/photo`

**功能描述**: 获取指定课程的签到照片URL

**路径参数**:
- `courseId`: 课程ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "photoUrl": "https://example.com/photo.jpg",
    "thumbnailUrl": "https://example.com/thumbnail.jpg",
    "uploadTime": "2024-12-19T07:55:00Z",
    "size": 1024000,
    "originalName": "attendance_photo.jpg"
  }
}
```

#### 2.2 获取课程签到照片列表

**接口地址**: `GET /teaching/courses/{courseId}/attendance/photos`

**功能描述**: 获取指定课程的所有签到照片

**路径参数**:
- `courseId`: 课程ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "photo_001",
      "url": "https://example.com/photo1.jpg",
      "thumbnailUrl": "https://example.com/thumbnail1.jpg",
      "uploadTime": "2024-12-19T07:55:00Z",
      "size": 1024000,
      "originalName": "attendance_photo_1.jpg",
      "fileName": "20241219_course001_001.jpg",
      "location": "教学楼A101"
    }
  ]
}
```

#### 2.3 下载签到照片压缩包

**接口地址**: `GET /teaching/courses/{courseId}/attendance/photos/download`

**功能描述**: 下载指定课程的所有签到照片（ZIP格式）

**路径参数**:
- `courseId`: 课程ID

**响应**: 文件流（ZIP格式）

#### 2.4 下载签到报告

**接口地址**: `GET /teaching/courses/{courseId}/attendance/report`

**功能描述**: 下载指定课程的签到报告（PDF格式）

**路径参数**:
- `courseId`: 课程ID

**响应**: 文件流（PDF格式）

### 3. 签到历史记录

#### 3.1 获取签到历史

**接口地址**: `GET /teaching/attendance/history`

**功能描述**: 获取签到历史记录

**请求参数**:
```json
{
  "courseId": "course_001", // 课程ID，可选
  "teacherId": "teacher_001", // 教师ID，可选
  "period": "month",        // 查询周期：week/month/semester
  "startDate": "2024-12-01", // 开始日期，可选
  "endDate": "2024-12-31",   // 结束日期，可选
  "page": 1,                // 页码
  "pageSize": 20            // 每页数量
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "attendance_001",
      "courseId": "course_001",
      "courseName": "高等数学",
      "teacherId": "teacher_001",
      "teacherName": "张教授",
      "date": "2024-12-19",
      "startTime": "08:00",
      "endTime": "09:40",
      "attendanceTime": "2024-12-19T07:55:00Z",
      "status": "completed",
      "note": "正常签到",
      "location": "教学楼A101"
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 20,
  "pages": 5
}
```



## 权限说明

### 管理员权限
- `super_admin`: 超级管理员，拥有所有权限
- `school_admin`: 校级管理员，可查看数据


### 教师权限
- `teacher`: 教师，只能查看和操作自己的课程和签到记录

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |


## 使用示例

### 1. 管理员查看今日课程签到情况
```javascript
// 获取今日课程列表
const response = await api.teaching.getCourses({
  date: '2024-12-19',
  page: 1,
  pageSize: 20
});

// 查看具体课程详情
const courseDetail = await api.teaching.getCourse('course_001');

// 查看签到照片
const photos = await api.teaching.getAttendancePhotos('course_001');
```

### 2. 教师签到
```javascript
// 创建FormData
const formData = new FormData();
formData.append('photo', photoFile);
formData.append('note', '正常上课');
formData.append('location', '教学楼A101');

// 执行签到
const result = await api.teaching.teacherCheckIn('course_001', formData);
```

### 3. 下载签到报告
```javascript
// 下载单个课程的签到报告
const reportBlob = await api.teaching.downloadAttendanceReport('course_001');

// 创建下载链接
const url = URL.createObjectURL(reportBlob);
const link = document.createElement('a');
link.href = url;
link.download = '高等数学_签到报告.pdf';
link.click();
```

## 注意事项

1. **文件上传限制**：
   - 照片格式：JPG、PNG、JPEG
   - 单个文件最大20MB
   - 每次签到最多上传5张照片

2. **签到时间规则**：
   - 课前30分钟可开始签到
   - 课后15分钟内签到记为迟到
   - 超过15分钟记为缺勤

3. **权限控制**：
   - 教师只能操作自己的课程
   - 所有操作都需要记录日志

4. **数据备份**：
   - 签到照片需要定期备份
   - 重要数据需要异地存储
   - 建议保留至少3年的历史数据


