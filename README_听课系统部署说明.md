# 听课系统部署说明

## 修复问题总结

### 1. 简化院系设计
- **问题**: 原设计创建了独立的`departments`表，但用户表中已有`department_name`字段
- **解决方案**: 删除独立的院系表，直接使用`users.department_name`字段进行院系统计和过滤
- **影响**: 简化了数据库结构，减少了不必要的关联查询

### 2. 修复API响应字段命名
- **问题**: DTO类使用`camelCase`命名，但API文档要求`snake_case`
- **解决方案**: 使用`@JsonProperty`注解将Java字段映射为符合API文档的命名格式
- **示例**: `observerName` → `observer_name`, `courseName` → `course_name`

## 部署步骤

### 1. 数据库初始化
使用简化版的SQL脚本：
```bash
mysql -u root -p pexitong2 < listening_system_simplified.sql
```

### 2. 配置文件更新
确保`application.properties`中的配置正确：
```properties
# 文件上传配置（支持大文件）
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# 听课系统文件存储配置
listening.file.base-path=F:/listening_files
listening.file.url-prefix=/api/files/download
```

### 3. 创建文件存储目录
在应用启动前创建以下目录结构：
```
F:/listening_files/
├── evaluations/    # 评价文件
├── videos/         # 听课视频
└── templates/      # 模板文件
```

## API响应示例

### 修复后的听课记录响应格式
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "observer_name": "王主任",
    "observer_type": "department_admin", 
    "observer_id": "3",
    "course_name": "体育与健康",
    "teacher_name": "李老师",
    "teacher_id": "5",
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

## 主要API端点

### 听课记录管理
- `GET /api/v1/listening/observations` - 获取听课记录列表
- `POST /api/v1/listening/observations` - 创建听课记录
- `GET /api/v1/listening/observations/{id}` - 获取听课记录详情
- `DELETE /api/v1/listening/observations/{id}` - 删除听课记录
- `POST /api/v1/listening/observations/{id}/evaluation` - 上传评价文件
- `POST /api/v1/listening/observations/{id}/video` - 上传视频文件

### 课程搜索
- `GET /api/v1/listening/courses/search` - 搜索课程

### 模板管理
- `GET /api/v1/listening/templates` - 获取模板列表
- `POST /api/v1/listening/templates` - 上传模板
- `GET /api/v1/listening/templates/{id}/download` - 下载模板
- `GET /api/v1/listening/templates/default/download` - 下载默认模板
- `PUT /api/v1/listening/templates/{id}/default` - 设置默认模板
- `DELETE /api/v1/listening/templates/{id}` - 删除模板

### 管理员功能
- `GET /api/v1/listening/admin/stats` - 获取统计数据

## 权限说明

- **教师**: 只能查看自己作为听课者的记录
- **院级管理员**: 可查看本院系（`department_name`）的所有听课记录
- **校级管理员**: 可查看全校听课记录，管理模板
- **超级管理员**: 拥有所有权限

## 注意事项

1. **院系过滤**: 现在基于`users.department_name`进行院系过滤，而不是独立的院系表
2. **文件存储**: 确保文件存储目录有足够的磁盘空间和写入权限
3. **字段命名**: API响应已修复为`snake_case`格式，符合API文档规范
4. **数据库**: 使用简化版SQL脚本，无需创建独立的院系表

## 测试建议

1. 使用不同角色的用户测试权限控制
2. 测试文件上传功能（评价文件、视频文件、模板文件）
3. 验证API响应字段命名是否符合文档要求
4. 测试院系过滤功能是否正常工作 