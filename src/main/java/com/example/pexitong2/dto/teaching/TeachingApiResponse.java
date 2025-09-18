package com.example.pexitong2.dto.teaching;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 教学系统专用API响应格式
 * 符合teaching-api-updated.md文档规范
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeachingApiResponse<T> {
    
    private int code;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private String timestamp;
    
    // 分页字段（只在分页查询时包含）
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer pages;
    
    public TeachingApiResponse() {
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
    
    public TeachingApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
    
    // 成功响应（无分页）
    public static <T> TeachingApiResponse<T> success(T data) {
        return new TeachingApiResponse<>(200, "success", data);
    }
    
    public static <T> TeachingApiResponse<T> success(String message, T data) {
        return new TeachingApiResponse<>(200, message, data);
    }
    
    // 成功响应（带分页）
    public static <T> TeachingApiResponse<T> success(T data, Long total, Integer page, Integer pageSize) {
        TeachingApiResponse<T> response = new TeachingApiResponse<>(200, "success", data);
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setPages((int) Math.ceil((double) total / pageSize));
        return response;
    }
    
    // 错误响应
    public static <T> TeachingApiResponse<T> error(int code, String message) {
        return new TeachingApiResponse<>(code, message, null);
    }
    
    public static <T> TeachingApiResponse<T> error(String message) {
        return new TeachingApiResponse<>(400, message, null);
    }
    
    // Getters and Setters
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public void setTotal(Long total) {
        this.total = total;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getPages() {
        return pages;
    }
    
    public void setPages(Integer pages) {
        this.pages = pages;
    }
}




