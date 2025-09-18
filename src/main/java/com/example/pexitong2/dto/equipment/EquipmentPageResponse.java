package com.example.pexitong2.dto.equipment;

import java.util.List;

public class EquipmentPageResponse {
    
    private List<EquipmentResponse> items;
    private Pagination pagination;
    
    // 构造函数
    public EquipmentPageResponse() {}
    
    public EquipmentPageResponse(List<EquipmentResponse> items, Pagination pagination) {
        this.items = items;
        this.pagination = pagination;
    }
    
    // Getters and Setters
    public List<EquipmentResponse> getItems() { return items; }
    public void setItems(List<EquipmentResponse> items) { this.items = items; }
    
    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }
    
    // 分页信息类
    public static class Pagination {
        private Long total;
        private Integer page;
        private Integer limit;
        private Integer pages;
        
        public Pagination() {}
        
        public Pagination(Long total, Integer page, Integer limit) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.pages = (int) Math.ceil((double) total / limit);
        }
        
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        
        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
        
        public Integer getPages() { return pages; }
        public void setPages(Integer pages) { this.pages = pages; }
    }
}





