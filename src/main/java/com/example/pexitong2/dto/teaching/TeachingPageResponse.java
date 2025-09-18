package com.example.pexitong2.dto.teaching;

import java.util.List;

/**
 * 分页响应DTO
 */
public class TeachingPageResponse<T> {
    
    private List<T> data;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer pages;
    
    // 构造函数
    public TeachingPageResponse() {}
    
    public TeachingPageResponse(List<T> data, Long total, Integer page, Integer pageSize) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }
    
    // Getter 和 Setter
    public List<T> getData() {
        return data;
    }
    
    public void setData(List<T> data) {
        this.data = data;
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




