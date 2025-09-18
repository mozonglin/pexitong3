# 校园体育管理系统后端

## 项目概述

这是一个基于Spring Boot开发的校园体育管理系统后端，专注于用户认证和权限管理功能。系统严格按照API文档实现，支持多级权限管理，包括学生、教师、院级管理员、校级管理员和超级管理员五种角色。

## 技术栈

- **框架**: Spring Boot 3.5.3
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Java版本**: Java 21
- **构建工具**: Gradle

## 功能特性

### 用户认证系统
- ✅ 发送短信验证码（模拟实现）
- ✅ 用户注册（支持学生/教师身份验证）
- ✅ 用户登录
- ✅ 用户登出
- ✅ Token刷新（简化实现）

### 用户信息管理
- ✅ 获取当前用户信息
- ✅ 更新用户资料
- ✅ 修改密码

### 权限管理
- ✅ 五级权限体系
- ✅ 基于角色的权限控制
- ✅ 角色变更记录

### 身份验证机制
- ✅ 学生/教师预导入数据验证
- ✅ 多重身份信息匹配（姓名+学工号+学校）
- ✅ 手机验证码验证

## 数据库设计

### 主数据库 (pexitong2)
- `users` - 用户表
- `verification_codes` - 验证码表
- `role_change_logs` - 角色变更记录表

### 验证数据库 (checkuser)
- `checkstudent` - 学生预导入表
- `checkteacher` - 教师预导入表

## 项目结构

```
src/main/java/com/example/pexitong2/
├── config/                 # 配置类
│   ├── AppConfig.java      # 应用配置
│   └── DatabaseConfig.java # 数据库配置
├── controller/             # 控制器层
│   ├── AuthController.java # 认证接口
│   └── UserController.java # 用户管理接口
├── dto/                    # 数据传输对象
│   ├── ApiResponse.java    # 通用响应格式
│   ├── LoginRequest.java   # 登录请求
│   ├── RegisterRequest.java # 注册请求
│   └── SendVerificationCodeRequest.java # 验证码请求
├── entity/                 # 实体类
│   ├── User.java          # 用户实体
│   ├── CheckStudent.java  # 学生验证实体
│   ├── CheckTeacher.java  # 教师验证实体
│   ├── VerificationCode.java # 验证码实体
│   └── RoleChangeLog.java # 角色变更记录实体
├── repository/             # 数据访问层
├── service/               # 服务层
│   ├── AuthService.java   # 认证服务
│   ├── UserService.java   # 用户服务
│   └── VerificationCodeService.java # 验证码服务
└── util/                  # 工具类
    ├── JwtUtil.java       # JWT工具（简化实现）
    └── PasswordUtil.java  # 密码工具
```

## 安装和运行

### 环境要求
- Java 21+
- MySQL 8.0+
- Gradle 7.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd pexitong2
   ```

2. **创建数据库**
   ```bash
   mysql -u root -p < init.sql
   ```

3. **配置数据库连接**
   编辑 `src/main/resources/application.properties`：
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **运行项目**
   ```bash
   ./gradlew bootRun
   ```

5. **访问应用**
   - 服务器地址: http://localhost:8080
   - API文档: 参考 `auth-api.md`

## API 接口

### 认证接口
- `POST /auth/send-verification-code` - 发送验证码
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `POST /auth/refresh` - 刷新Token

### 用户管理接口
- `GET /user/profile` - 获取用户信息
- `PUT /user/profile` - 更新用户信息
- `POST /user/change-password` - 修改密码

## 测试数据

### 预导入学生数据
| 学校 | 学院 | 学号 | 姓名 |
|------|------|------|------|
| 济南校区 | 金融学院 | 2021-4 | 李骏铭 |
| 北京体育大学 | 体育教育学院 | 2024001001 | 张三 |

### 预导入教师数据
| 学校 | 工号 | 学院 | 姓名 |
|------|------|------|------|
| 山东科技大学 | 20201101 | 体育学院 | 刘圆思 |
| 北京体育大学 | T2024001 | 体育教育学院 | 李老师 |

### 超级管理员账户
- 用户名: `superadmin`
- 密码: `Admin123456`
- 角色: `super_admin`

## 使用示例

### 1. 发送验证码
```bash
curl -X POST http://localhost:8080/auth/send-verification-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13812345678",
    "type": "register"
  }'
```

### 2. 用户注册
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "李骏铭",
    "userType": "student",
    "school": "济南校区",
    "studentId": "2021-4",
    "phone": "13812345678",
    "verificationCode": "123456"
  }'
```

### 3. 用户登录
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "生成的用户名",
    "password": "生成的密码",
    "userType": "student"
  }'
```

## 系统特性

### 安全机制
- 密码加盐哈希存储
- 验证码5分钟有效期
- 防止重复注册
- 多重身份验证

### 权限体系
- **学生**: 基础用户权限
- **教师**: 内容创建权限  
- **院级管理员**: 院系管理权限
- **校级管理员**: 全校管理权限
- **超级管理员**: 系统级权限

### 注册流程
1. 选择用户类型（学生/教师）
2. 填写基本信息
3. 获取手机验证码
4. 系统验证预导入数据
5. 生成用户名和初始密码
6. 短信发送登录凭证

## 注意事项

1. **验证码模拟**: 当前使用随机生成模拟，生产环境需接入真实短信服务
2. **JWT标准**: 使用标准JWT库(jjwt)实现，支持完整的Token生成和验证
3. **Spring Security**: 已完整集成Spring Security，支持JWT认证和权限控制
4. **会话管理**: 暂未实现，根据需求可后续添加
5. **数据库连接**: checkuser数据库通过JDBC直接访问，主数据库使用JPA

## 开发说明

- 严格按照API文档实现功能
- 不添加多余功能
- 注册验证基于预导入数据表
- 密码使用安全哈希存储
- 支持跨域访问

## 启动步骤

### 1. 环境准备
- 确保Java 21已安装
- 确保MySQL 8.0+已安装并运行
- 数据库服务运行在 `192.168.1.104:3306`

### 2. 数据库初始化
```bash
# 在MySQL中执行
mysql -u root -p < init.sql
```

### 3. 项目启动
```bash
# 清理并构建项目
./gradlew clean build

# 运行项目
./gradlew bootRun
```

### 4. 验证启动
- 访问: http://localhost:8080/test/health
- 如果返回JSON响应，则启动成功

### 5. 运行测试脚本
```bash
# Linux/Mac
./test-api.sh

# Windows
test-api.bat
```

### 6. 手动测试注册流程
```bash
# 1. 发送验证码
curl -X POST http://localhost:8080/auth/send-verification-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "13812345678", "type": "register"}'

# 2. 使用控制台显示的验证码进行注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "李骏铭",
    "userType": "student", 
    "school": "济南校区",
    "studentId": "2021-4",
    "phone": "13812345678",
    "verificationCode": "控制台显示的验证码"
  }'

# 3. 使用返回的用户名和密码登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "返回的用户名",
    "password": "返回的密码",
    "userType": "student"
  }'
```

## 故障排除

### 1. 依赖问题
如果遇到JWT或Spring Security相关的编译错误：
```bash
# 重新加载依赖
./gradlew build --refresh-dependencies
```

### 1.1 测试系统组件
```bash
# 测试JWT功能
curl http://localhost:8080/test/jwt

# 测试密码加密
curl http://localhost:8080/test/password

# 测试健康状态
curl http://localhost:8080/test/health
```

### 2. 数据库连接问题
- 检查MySQL服务是否运行
- 验证数据库地址: `192.168.1.104:3306`
- 确认用户名密码: `root/123456`

### 3. 预导入数据问题
- 确保执行了 `init.sql`
- 检查 `checkuser` 数据库是否创建
- 验证示例数据是否插入成功

## 许可证

本项目仅用于学习和开发目的。 