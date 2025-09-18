# PE管理端启动问题排查

## 问题描述

在启动应用时遇到以下错误：
```
The bean 'activityRepository' could not be registered. A bean with that name has already been defined and overriding is disabled.
```

## 解决方案

### 已执行的修复：

1. **移除手动Repository配置**
   - 删除了`DatabaseConfig`中的`@EnableJpaRepositories`注解
   - 让Spring Boot自动扫描Repository接口

2. **移除Repository接口上的@Repository注解**
   - JpaRepository接口不需要`@Repository`注解
   - Spring Boot会自动识别继承了JpaRepository的接口

3. **配置优化**
   - 添加了`spring.main.allow-bean-definition-overriding=false`确保不允许Bean覆盖
   - 添加了`spring.jpa.open-in-view=false`优化JPA配置

### 修复后的配置：

**DatabaseConfig.java:**
```java
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    // Spring Boot会自动扫描所有的Repository接口
}
```

**Repository接口示例:**
```java
public interface ActivityRepository extends JpaRepository<Activity, String> {
    // 不需要@Repository注解
}
```

## 验证启动

### 方法1：直接启动
```bash
mvn spring-boot:run
```

### 方法2：运行测试
```bash
mvn test -Dtest=ApplicationStartupTest
```

### 方法3：IDE中运行
- 右键点击`Pexitong2Application.java`
- 选择"Run 'Pexitong2Application'"

## 预期结果

应用成功启动后，你应该看到类似的日志：
```
Started Pexitong2Application in X.XXX seconds
```

## API测试

启动成功后，可以使用以下URL进行基本测试：
- 健康检查：`http://localhost:9999/actuator/health` (如果启用了Actuator)
- PE API基础路径：`http://localhost:9999/api/pe/*`

## 常见问题

### 1. 数据库连接问题
确保MySQL数据库正在运行，且连接配置正确：
```properties
spring.datasource.url=jdbc:mysql://192.168.1.100:3306/pexitong3?...
spring.datasource.username=root
spring.datasource.password=790128mM!
```

### 2. 端口占用
如果9999端口被占用，可以修改`application.properties`：
```properties
server.port=8080
```

### 3. JPA实体扫描问题
确保所有实体类都在正确的包路径下：
- 原有实体：`com.example.pexitong2.entity`
- PE实体：`com.example.pexitong2.entity.pe`

## 下一步

启动成功后，可以：
1. 使用测试脚本验证API功能
2. 登录管理端获取Token
3. 调用PE管理API进行测试




