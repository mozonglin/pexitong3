# 阳光跑统计API文档

## 概述

阳光跑统计功能为校级和院级管理员提供学生阳光跑数据的统计和排名功能。

### 数据说明

- **距离单位**: 米（数据库存储为小数，如 82.77310080397267，统计时取整为 82 米）
- **时长单位**: 秒（数据库存储为毫秒，如 15657 毫秒，统计时转换为 15 秒）
- **排名依据**: 按人均跑步距离排序（从高到低）

### 权限说明

- **校级管理员**: 可查看全校各院系的阳光跑统计和排名
- **院级管理员**: 可查看本院各班级的阳光跑统计和排名
- **超级管理员**: 拥有所有权限

---

## 1. 获取校级阳光跑统计（按院系）

### 基本信息

- **接口路径**: `/pe/admin/statistics/sunshine-run/school`
- **请求方式**: `GET`
- **权限要求**: 校级管理员、超级管理员
- **功能说明**: 获取全校各院系的阳光跑统计数据和排名

### 请求头

```
Authorization: Bearer {token}
```

### 请求参数

无

### 响应格式

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "school": "某某大学",
    "scope": "全校",
    "totalStudents": 5000,
    "overall": {
      "totalRuns": 15000,
      "totalDistance": 750000,
      "totalDuration": 450000,
      "avgRunsPerStudent": 3.0,
      "avgDistancePerStudent": 150.0,
      "avgDurationPerStudent": 90.0
    },
    "groupStats": [
      {
        "groupName": "计算机学院",
        "studentCount": 800,
        "aggregate": {
          "totalRuns": 2400,
          "totalDistance": 144000,
          "totalDuration": 86400,
          "avgRunsPerStudent": 3.0,
          "avgDistancePerStudent": 180.0,
          "avgDurationPerStudent": 108.0
        }
      },
      {
        "groupName": "经济学院",
        "studentCount": 600,
        "aggregate": {
          "totalRuns": 1800,
          "totalDistance": 90000,
          "totalDuration": 54000,
          "avgRunsPerStudent": 3.0,
          "avgDistancePerStudent": 150.0,
          "avgDurationPerStudent": 90.0
        }
      }
    ],
    "groupRankings": [
      {
        "rank": 1,
        "groupName": "计算机学院",
        "avgDistancePerStudent": 180.0,
        "aggregate": {
          "totalRuns": 2400,
          "totalDistance": 144000,
          "totalDuration": 86400,
          "avgRunsPerStudent": 3.0,
          "avgDistancePerStudent": 180.0,
          "avgDurationPerStudent": 108.0
        }
      },
      {
        "rank": 2,
        "groupName": "经济学院",
        "avgDistancePerStudent": 150.0,
        "aggregate": {
          "totalRuns": 1800,
          "totalDistance": 90000,
          "totalDuration": 54000,
          "avgRunsPerStudent": 3.0,
          "avgDistancePerStudent": 150.0,
          "avgDurationPerStudent": 90.0
        }
      }
    ]
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|-----|------|------|
| school | String | 学校名称 |
| scope | String | 统计范围（"全校"） |
| totalStudents | Integer | 学生总数 |
| overall | Object | 整体统计数据 |
| overall.totalRuns | Long | 总跑步次数 |
| overall.totalDistance | Long | 总跑步距离（米） |
| overall.totalDuration | Long | 总跑步时长（秒） |
| overall.avgRunsPerStudent | Double | 人均跑步次数 |
| overall.avgDistancePerStudent | Double | 人均跑步距离（米） |
| overall.avgDurationPerStudent | Double | 人均跑步时长（秒） |
| groupStats | Array | 各院系统计列表 |
| groupStats[].groupName | String | 院系名称 |
| groupStats[].studentCount | Integer | 该院系学生数 |
| groupStats[].aggregate | Object | 该院系的汇总数据 |
| groupRankings | Array | 院系排名列表（按人均距离降序） |
| groupRankings[].rank | Integer | 排名 |
| groupRankings[].groupName | String | 院系名称 |
| groupRankings[].avgDistancePerStudent | Double | 人均跑步距离（排名依据） |
| groupRankings[].aggregate | Object | 该院系的汇总数据 |

### 错误响应

```json
{
  "code": 500,
  "message": "权限不足，只有校级管理员可以查看统计数据",
  "data": null
}
```

---

## 2. 获取院级阳光跑统计（按班级）

### 基本信息

- **接口路径**: `/pe/admin/statistics/sunshine-run/college`
- **请求方式**: `GET`
- **权限要求**: 院级管理员、校级管理员、超级管理员
- **功能说明**: 
  - 院级管理员：查看本院各班级的阳光跑统计和排名
  - 校级管理员：查看全校所有班级的阳光跑统计和排名

### 请求头

```
Authorization: Bearer {token}
```

### 请求参数

无

### 响应格式

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "school": "某某大学",
    "scope": "计算机学院",
    "totalStudents": 800,
    "overall": {
      "totalRuns": 2400,
      "totalDistance": 144000,
      "totalDuration": 86400,
      "avgRunsPerStudent": 3.0,
      "avgDistancePerStudent": 180.0,
      "avgDurationPerStudent": 108.0
    },
    "groupStats": [
      {
        "groupName": "计算机2021级1班",
        "studentCount": 40,
        "aggregate": {
          "totalRuns": 150,
          "totalDistance": 9000,
          "totalDuration": 5400,
          "avgRunsPerStudent": 3.75,
          "avgDistancePerStudent": 225.0,
          "avgDurationPerStudent": 135.0
        }
      },
      {
        "groupName": "计算机2021级2班",
        "studentCount": 38,
        "aggregate": {
          "totalRuns": 120,
          "totalDistance": 6840,
          "totalDuration": 4104,
          "avgRunsPerStudent": 3.16,
          "avgDistancePerStudent": 180.0,
          "avgDurationPerStudent": 108.0
        }
      }
    ],
    "groupRankings": [
      {
        "rank": 1,
        "groupName": "计算机2021级1班",
        "avgDistancePerStudent": 225.0,
        "aggregate": {
          "totalRuns": 150,
          "totalDistance": 9000,
          "totalDuration": 5400,
          "avgRunsPerStudent": 3.75,
          "avgDistancePerStudent": 225.0,
          "avgDurationPerStudent": 135.0
        }
      },
      {
        "rank": 2,
        "groupName": "计算机2021级2班",
        "avgDistancePerStudent": 180.0,
        "aggregate": {
          "totalRuns": 120,
          "totalDistance": 6840,
          "totalDuration": 4104,
          "avgRunsPerStudent": 3.16,
          "avgDistancePerStudent": 180.0,
          "avgDurationPerStudent": 108.0
        }
      }
    ]
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|-----|------|------|
| school | String | 学校名称 |
| scope | String | 统计范围（院系名称或"全校所有院系"） |
| totalStudents | Integer | 学生总数 |
| overall | Object | 整体统计数据 |
| overall.totalRuns | Long | 总跑步次数 |
| overall.totalDistance | Long | 总跑步距离（米） |
| overall.totalDuration | Long | 总跑步时长（秒） |
| overall.avgRunsPerStudent | Double | 人均跑步次数 |
| overall.avgDistancePerStudent | Double | 人均跑步距离（米） |
| overall.avgDurationPerStudent | Double | 人均跑步时长（秒） |
| groupStats | Array | 各班级统计列表 |
| groupStats[].groupName | String | 班级名称 |
| groupStats[].studentCount | Integer | 该班级学生数 |
| groupStats[].aggregate | Object | 该班级的汇总数据 |
| groupRankings | Array | 班级排名列表（按人均距离降序） |
| groupRankings[].rank | Integer | 排名 |
| groupRankings[].groupName | String | 班级名称 |
| groupRankings[].avgDistancePerStudent | Double | 人均跑步距离（排名依据） |
| groupRankings[].aggregate | Object | 该班级的汇总数据 |

### 校级管理员访问说明

当校级管理员访问此接口时：
- `scope` 字段为 "全校所有院系"
- `groupStats` 和 `groupRankings` 包含全校所有班级的数据
- 可用于查看全校班级排名

### 错误响应

```json
{
  "code": 500,
  "message": "权限不足，只有院级管理员及以上可以查看院系统计数据",
  "data": null
}
```

---

## 使用示例

### 示例1：校级管理员查看院系排名

```bash
curl -X GET "http://localhost:8080/pe/admin/statistics/sunshine-run/school" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 示例2：院级管理员查看本院班级排名

```bash
curl -X GET "http://localhost:8080/pe/admin/statistics/sunshine-run/college" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 示例3：校级管理员查看全校班级排名

```bash
curl -X GET "http://localhost:8080/pe/admin/statistics/sunshine-run/college" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 数据库字段说明

### users1 表新增字段

| 字段名 | 类型 | 说明 | 示例 |
|-------|------|------|------|
| sunshine_total_runs | INT | 跑步总次数 | 3 |
| sunshine_total_distance | DOUBLE | 跑步总距离（小数） | 82.77310080397267 |
| sunshine_total_duration | BIGINT | 跑步总时长（毫秒） | 15657 |

### 数据转换规则

1. **距离转换**: 小数点直接取整
   - 数据库: 82.77310080397267
   - 显示: 82 米

2. **时长转换**: 毫秒转秒
   - 数据库: 15657 毫秒
   - 显示: 15 秒

3. **人均值计算**: 保留两位小数
   - 公式: Math.round(值 * 100.0) / 100.0

---

## 注意事项

1. **权限控制**
   - 所有接口都需要携带有效的 JWT Token
   - Token 需包含在 Authorization 请求头中
   - 不同角色只能访问对应权限的数据

2. **数据范围**
   - 校级管理员可以查看全校数据
   - 院级管理员只能查看本院数据
   - 数据范围由后端根据用户角色自动过滤

3. **排名规则**
   - 排名依据：人均跑步距离（avgDistancePerStudent）
   - 排序方式：从高到低
   - 相同距离的排名按出现顺序

4. **空数据处理**
   - 如果某院系/班级没有学生，不会出现在统计中
   - 如果学生的阳光跑数据为 NULL，按 0 计算
   - 如果分组为空，返回空数组

---

## 更新日志

### 2025-11-18
- ✅ 创建阳光跑统计功能
- ✅ 实现校级院系统计和排名
- ✅ 实现院级班级统计和排名
- ✅ 添加数据转换逻辑（距离取整、时长转秒）
- ✅ 完成 API 接口和文档

