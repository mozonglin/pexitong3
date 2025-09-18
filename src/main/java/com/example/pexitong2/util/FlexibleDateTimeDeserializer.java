package com.example.pexitong2.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 灵活的日期时间反序列化器，支持多种格式
 */
public class FlexibleDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,  // 2025-08-11T07:30:05.000Z
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,   // 2025-08-11T07:30:05
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),  // 2025-08-11 07:30:05
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),  // 带Z但无时区偏移
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")  // 带毫秒和Z
    };
    
    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String dateString = parser.getText();
        
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        // 尝试多种格式解析
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                if (formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME) {
                    // 对于带时区的格式，先解析为OffsetDateTime，然后转换为LocalDateTime
                    return OffsetDateTime.parse(dateString, formatter).toLocalDateTime();
                } else {
                    return LocalDateTime.parse(dateString, formatter);
                }
            } catch (DateTimeParseException e) {
                // 继续尝试下一种格式
            }
        }
        
        // 如果所有格式都失败，抛出异常
        throw new IllegalArgumentException("无法解析日期时间格式: " + dateString + 
            "。支持的格式包括: ISO_OFFSET_DATE_TIME, ISO_LOCAL_DATE_TIME, yyyy-MM-dd HH:mm:ss 等");
    }
}




