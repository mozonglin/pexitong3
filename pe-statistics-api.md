# PE校园管理统计功能 API 文档

## 概述

本文档描述了PE校园管理系统的统计功能API接口，该功能仅开放给校级管理员和院级管理员使用。

## 基础信息

- **基础URL**: `https://api.example.com/pe/admin/statistics`
- **认证方式**: Bearer Token (JWT)
- **数据格式**: JSON
- **编码**: UTF-8

## 权限说明

### 角色权限矩阵

| 功能 | 校级管理员 | 院级管理员 | 说明 |
|------|------------|------------|------|
| 设置PE积分指标 | ✅ | ❌ | 只有校级管理员可以设置本校的PE积分指标 |
| 查看学校统计 | ✅ | ❌ | 查看全校学生PE积分达标率和院系排名 |
| 查看院系统计 | ✅ | ✅ | 校级管理员查看全校所有班级数据，院级管理员只能查看本院班级数据 |

### 权限验证

所有接口都需要在请求头中提供有效的JWT Token：
```
Authorization: Bearer <your_jwt_token>
```

## 数据库结构说明

### 新增字段

#### users表（管理员表）新增字段：
- `weekly_target` (INT): 周PE积分指标
- `monthly_target` (INT): 月PE积分指标  
- `total_target` (INT): 总PE积分指标

#### users1表（学生表）新增字段：
- `class_name` (VARCHAR(100)): 班级名称

### 字段映射关系
- users表的`department_name`字段对应院系名称
- users1表的`college`字段对应院系名称
- users1表的`class_name`字段对应班级名称

## API接口详情

### 1. 设置PE积分指标

**接口**: `POST /pe/admin/statistics/targets`

**权限**: 仅校级管理员 (`school_admin`)

**描述**: 校级管理员设置本校的PE积分指标，包括周指标、月指标和总指标

**请求头**:
```
Content-Type: application/json
Authorization: Bearer <jwt_token>
```

**请求体**:
```json
{
  "weeklyTarget": 10,
  "monthlyTarget": 40,
  "totalTarget": 100
}
```

**请求参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| weeklyTarget | Integer | 是 | 周PE积分指标 |
| monthlyTarget | Integer | 是 | 月PE积分指标 |
| totalTarget | Integer | 是 | 总PE积分指标 |

**响应示例**:
```json
{
  "code": 200,
  "message": "PE积分指标设置成功",
  "data": null
}
```

**错误响应**:
```json
{
  "code": 403,
  "message": "权限不足，只有校级管理员可以设置PE积分指标",
  "data": null
}
```

### 2. 获取学校统计数据

**接口**: `GET /pe/admin/statistics/school`

**权限**: 仅校级管理员 (`school_admin`)

**描述**: 获取全校PE积分统计数据，包括整体达标率、各院系达标率、院系排名等

**请求头**:
```
Authorization: Bearer <jwt_token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "school": "北京体育大学",
    "totalStudents": 1250,
    "targets": {
      "weeklyTarget": 10,
      "monthlyTarget": 40,
      "totalTarget": 100
    },
    "overallCompliance": {
      "weeklyComplianceRate": 78.5,
      "monthlyComplianceRate": 82.3,
      "totalComplianceRate": 65.8
    },
    "collegeStats": [
      {
        "collegeName": "体育教育学院",
        "studentCount": 450,
        "complianceRates": {
          "weeklyComplianceRate": 85.2,
          "monthlyComplianceRate": 88.7,
          "totalComplianceRate": 72.1
        }
      },
      {
        "collegeName": "运动训练学院",
        "studentCount": 380,
        "complianceRates": {
          "weeklyComplianceRate": 76.3,
          "monthlyComplianceRate": 79.5,
          "totalComplianceRate": 61.8
        }
      }
    ],
    "collegeRankings": [
      {
        "rank": 1,
        "collegeName": "体育教育学院",
        "overallComplianceRate": 82.0,
        "complianceRates": {
          "weeklyComplianceRate": 85.2,
          "monthlyComplianceRate": 88.7,
          "totalComplianceRate": 72.1
        }
      },
      {
        "rank": 2,
        "collegeName": "运动训练学院", 
        "overallComplianceRate": 72.5,
        "complianceRates": {
          "weeklyComplianceRate": 76.3,
          "monthlyComplianceRate": 79.5,
          "totalComplianceRate": 61.8
        }
      }
    ]
  }
}
```

**响应数据说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| school | String | 学校名称 |
| totalStudents | Integer | 学生总数 |
| targets | Object | PE积分指标 |
| targets.weeklyTarget | Integer | 周积分指标 |
| targets.monthlyTarget | Integer | 月积分指标 |
| targets.totalTarget | Integer | 总积分指标 |
| overallCompliance | Object | 全校整体达标率 |
| overallCompliance.weeklyComplianceRate | Double | 周达标率(%) |
| overallCompliance.monthlyComplianceRate | Double | 月达标率(%) |
| overallCompliance.totalComplianceRate | Double | 总达标率(%) |
| collegeStats | Array | 各院系统计数据 |
| collegeStats[].collegeName | String | 院系名称 |
| collegeStats[].studentCount | Integer | 院系学生数 |
| collegeStats[].complianceRates | Object | 院系达标率 |
| collegeRankings | Array | 院系排名（按综合达标率排序） |
| collegeRankings[].rank | Integer | 排名 |
| collegeRankings[].collegeName | String | 院系名称 |
| collegeRankings[].overallComplianceRate | Double | 综合达标率（三项平均） |

### 3. 获取院系统计数据

**接口**: `GET /pe/admin/statistics/college`

**权限**: 院级管理员 (`department_admin`) 和校级管理员 (`school_admin`)

**描述**: 获取院系PE积分统计数据
- **校级管理员**：返回全校所有院系的所有班级数据
- **院级管理员**：只返回本院的班级数据

**请求头**:
```
Authorization: Bearer <jwt_token>
```

**响应示例（院级管理员）**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "school": "北京体育大学",
    "college": "体育教育学院",
    "totalStudents": 450,
    "targets": {
      "weeklyTarget": 10,
      "monthlyTarget": 40,
      "totalTarget": 100
    },
    "overallCompliance": {
      "weeklyComplianceRate": 85.2,
      "monthlyComplianceRate": 88.7,
      "totalComplianceRate": 72.1
    },
    "classStats": [
      {
        "className": "体教2021级1班",
        "studentCount": 35,
        "complianceRates": {
          "weeklyComplianceRate": 88.6,
          "monthlyComplianceRate": 91.4,
          "totalComplianceRate": 77.1
        }
      },
      {
        "className": "体教2021级2班",
        "studentCount": 33,
        "complianceRates": {
          "weeklyComplianceRate": 84.8,
          "monthlyComplianceRate": 87.9,
          "totalComplianceRate": 69.7
        }
      }
    ],
    "classRankings": [
      {
        "rank": 1,
        "className": "体教2021级1班",
        "overallComplianceRate": 85.7,
        "complianceRates": {
          "weeklyComplianceRate": 88.6,
          "monthlyComplianceRate": 91.4,
          "totalComplianceRate": 77.1
        }
      },
      {
        "rank": 2,
        "className": "体教2021级2班",
        "overallComplianceRate": 80.8,
        "complianceRates": {
          "weeklyComplianceRate": 84.8,
          "monthlyComplianceRate": 87.9,
          "totalComplianceRate": 69.7
        }
      }
    ]
  }
}
```

**响应示例（校级管理员访问）**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "school": "北京体育大学",
    "college": "全校所有院系",
    "totalStudents": 1250,
    "targets": {
      "weeklyTarget": 10,
      "monthlyTarget": 40,
      "totalTarget": 100
    },
    "overallCompliance": {
      "weeklyComplianceRate": 78.5,
      "monthlyComplianceRate": 82.3,
      "totalComplianceRate": 65.8
    },
    "classStats": [
      {
        "className": "体教2021级1班",
        "studentCount": 35,
        "complianceRates": {
          "weeklyComplianceRate": 88.6,
          "monthlyComplianceRate": 91.4,
          "totalComplianceRate": 77.1
        }
      },
      {
        "className": "运训2021级1班",
        "studentCount": 32,
        "complianceRates": {
          "weeklyComplianceRate": 76.3,
          "monthlyComplianceRate": 79.5,
          "totalComplianceRate": 61.8
        }
      }
    ],
    "classRankings": [
      {
        "rank": 1,
        "className": "体教2021级1班",
        "overallComplianceRate": 85.7,
        "complianceRates": {
          "weeklyComplianceRate": 88.6,
          "monthlyComplianceRate": 91.4,
          "totalComplianceRate": 77.1
        }
      },
      {
        "rank": 2,
        "className": "运训2021级1班",
        "overallComplianceRate": 72.5,
        "complianceRates": {
          "weeklyComplianceRate": 76.3,
          "monthlyComplianceRate": 79.5,
          "totalComplianceRate": 61.8
        }
      }
    ]
  }
}
```

**响应数据说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| school | String | 学校名称 |
| college | String | 院系名称（院级管理员显示具体院系，校级管理员显示"全校所有院系"） |
| totalStudents | Integer | 学生总数（院级管理员显示本院学生数，校级管理员显示全校学生数） |
| targets | Object | PE积分指标（来自校级管理员设置） |
| overallCompliance | Object | 本院整体达标率 |
| classStats | Array | 各班统计数据 |
| classStats[].className | String | 班级名称 |
| classStats[].studentCount | Integer | 班级学生数 |
| classStats[].complianceRates | Object | 班级达标率 |
| classRankings | Array | 班级排名（按综合达标率排序） |
| classRankings[].rank | Integer | 排名 |
| classRankings[].className | String | 班级名称 |
| classRankings[].overallComplianceRate | Double | 综合达标率（三项平均） |

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，Token无效或过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 常见错误响应

### 权限不足
```json
{
  "code": 403,
  "message": "权限不足，只有校级管理员可以设置PE积分指标",
  "data": null
}
```

### Token无效
```json
{
  "code": 401,
  "message": "Token无效或已过期",
  "data": null
}
```

### 未设置指标
```json
{
  "code": 400,
  "message": "校级管理员尚未设置PE积分指标",
  "data": null
}
```

## 达标率计算说明

### 计算逻辑

1. **周达标率**: 根据学生从账户创建到现在的总积分，估算平均周积分，与周指标比较
2. **月达标率**: 根据学生从账户创建到现在的总积分，估算平均月积分，与月指标比较  
3. **总达标率**: 直接比较学生当前总积分与总指标
4. **综合达标率**: 取周、月、总达标率的平均值，用于排名

### 计算公式

```
估算周积分 = 总积分 / 从创建到现在的周数
估算月积分 = 总积分 / 从创建到现在的月数
周达标率 = 达到周指标的学生数 / 总学生数 × 100%
月达标率 = 达到月指标的学生数 / 总学生数 × 100%
总达标率 = 达到总指标的学生数 / 总学生数 × 100%
综合达标率 = (周达标率 + 月达标率 + 总达标率) / 3
```

## 使用示例

### 设置PE积分指标

```bash
curl -X POST "https://api.example.com/pe/admin/statistics/targets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "weeklyTarget": 10,
    "monthlyTarget": 40,
    "totalTarget": 100
  }'
```

### 获取学校统计

```bash
curl -X GET "https://api.example.com/pe/admin/statistics/school" \
  -H "Authorization: Bearer your_jwt_token"
```

### 获取院系统计

```bash
curl -X GET "https://api.example.com/pe/admin/statistics/college" \
  -H "Authorization: Bearer your_jwt_token"
```

## 注意事项

1. **权限控制**: 院级管理员只能查看本院数据，校级管理员可以查看全校数据
2. **数据依赖**: 院级管理员查看统计前，校级管理员必须先设置PE积分指标
3. **字段映射**: 
   - 管理员的`department_name`对应学生的`college`字段
   - 学生的`class_name`字段用于班级统计
4. **达标率计算**: 基于学生账户创建时间进行估算，实际应用中可根据需求优化计算逻辑
5. **排名算法**: 按综合达标率（三项平均）降序排列

## 部署说明

1. 执行数据库更新脚本 `pe_database_update.sql`
2. 重启应用服务
3. 确保JWT Token配置正确
4. 测试各权限角色的接口访问

## 版本信息

- **版本**: v1.0
- **更新日期**: 2025-01-09
- **兼容性**: 基于现有PE管理系统架构
