package com.example.pexitong2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson配置类，用于处理时间序列化和时区
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper，设置时区为北京时间
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 设置时区为北京时间
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        
        // 注册JavaTimeModule处理Java 8时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 自定义LocalDateTime序列化格式（北京时间）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        
        mapper.registerModule(javaTimeModule);
        
        return mapper;
    }
}
