package com.example.pexitong2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    // 简化配置：使用默认的数据源配置
    // checkstudent和checkteacher表将通过AuthService中的动态数据源访问
    // Spring Boot会自动扫描所有的Repository接口
} 