# 器材管理API响应示例

## 修正后的JSON响应格式

所有响应字段已修正为下划线命名格式，完全符合API文档规范。

### 1. 获取器材列表响应

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "id": "eq001",
        "category_id": "cat001",
        "category_name": "球类器材",
        "name": "标准足球",
        "model": "Nike Premier League",
        "specification": "5号球，FIFA认证标准",
        "total_quantity": 50,
        "available_quantity": 35,
        "borrowed_quantity": 12,
        "damaged_quantity": 3,
        "unit_price": 89.99,
        "purchase_date": "2024-01-15",
        "warranty_period": 12,
        "storage_location": "器材室A区1号柜",
        "created_by": "teacher001",
        "createdAt": "2024-01-15T08:00:00Z",
        "updatedAt": "2024-03-01T14:30:00Z"
      }
    ],
    "pagination": {
      "total": 1,
      "page": 1,
      "limit": 10,
      "pages": 1
    }
  }
}
```

### 2. 获取借用申请列表响应

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "id": "app001",
        "equipment_id": "eq001",
        "equipment_name": "标准足球",
        "equipment_model": "Nike Premier League",
        "borrower_id": "student001",
        "borrower_name": "张三",
        "borrower_type": "student",
        "borrower_contact": "13800138001",
        "quantity": 5,
        "purpose": "体育课教学使用",
        "borrow_date": "2024-03-02T08:00:00Z",
        "expected_return_date": "2024-03-02T18:00:00Z",
        "actual_return_date": null,
        "status": "pending",
        "remark": "",
        "approved_by": null,
        "approved_at": null,
        "createdAt": "2024-03-01T10:00:00Z",
        "updatedAt": "2024-03-01T10:00:00Z"
      }
    ],
    "pagination": {
      "total": 1,
      "page": 1,
      "limit": 10,
      "pages": 1
    }
  }
}
```

### 3. 库存调整响应

```json
{
  "code": 200,
  "message": "库存调整成功",
  "data": {
    "equipment_id": "eq001",
    "adjustment_id": "adj001",
    "type": "increase",
    "quantity": 10,
    "before_quantity": 50,
    "after_quantity": 60,
    "reason": "采购新批次器材",
    "operator": "teacher001",
    "createdAt": "2024-03-01T16:00:00Z"
  }
}
```

### 4. 统计信息响应

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "equipment_summary": {
      "total_items": 150,
      "total_categories": 8,
      "available_items": 120,
      "borrowed_items": 25,
      "damaged_items": 5
    },
    "application_summary": {
      "pending_applications": 3,
      "today_applications": 8,
      "week_applications": 45,
      "month_applications": 180
    },
    "popular_equipment": [
      {
        "equipment_id": "eq001",
        "equipment_name": "标准足球",
        "borrow_count": 25,
        "category_name": "球类器材"
      }
    ]
  }
}
```

## 请求参数示例

### 添加器材请求

```json
{
  "category_id": "cat001",
  "name": "标准篮球",
  "model": "Nike Elite",
  "specification": "7号球，PU皮面",
  "total_quantity": 30,
  "unit_price": 120.00,
  "purchase_date": "2024-03-01",
  "warranty_period": 24,
  "storage_location": "器材室B区2号柜"
}
```

### 器材归还请求

```json
{
  "actual_quantity": 5,
  "condition": "good",
  "remark": "器材状态良好，已全部归还"
}
```

## 字段命名规范

所有JSON字段已统一使用下划线命名法（snake_case），包括：

**器材相关字段：**
- `category_id` - 分类ID
- `category_name` - 分类名称
- `total_quantity` - 总数量
- `available_quantity` - 可用数量
- `borrowed_quantity` - 借出数量
- `damaged_quantity` - 损坏数量
- `unit_price` - 单价
- `purchase_date` - 购买日期
- `warranty_period` - 保修期
- `storage_location` - 存放位置
- `created_by` - 创建人

**申请相关字段：**
- `equipment_id` - 器材ID
- `equipment_name` - 器材名称
- `equipment_model` - 器材型号
- `borrower_id` - 借用人ID
- `borrower_name` - 借用人姓名
- `borrower_type` - 借用人类型
- `borrower_contact` - 借用人联系方式
- `borrow_date` - 借用日期
- `expected_return_date` - 预期归还日期
- `actual_return_date` - 实际归还日期
- `approved_by` - 审批人
- `approved_at` - 审批时间

**统计相关字段：**
- `equipment_summary` - 器材汇总
- `application_summary` - 申请汇总
- `popular_equipment` - 热门器材
- `total_items` - 总器材数
- `pending_applications` - 待审批申请数
- 等等...

这些修正确保了API响应完全符合文档规范，提供一致的接口体验。





