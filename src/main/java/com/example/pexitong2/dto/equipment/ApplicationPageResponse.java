package com.example.pexitong2.dto.equipment;

import java.util.List;

public class ApplicationPageResponse {
    
    private List<ApplicationResponse> items;
    private EquipmentPageResponse.Pagination pagination;
    
    // 构造函数
    public ApplicationPageResponse() {}
    
    public ApplicationPageResponse(List<ApplicationResponse> items, EquipmentPageResponse.Pagination pagination) {
        this.items = items;
        this.pagination = pagination;
    }
    
    // Getters and Setters
    public List<ApplicationResponse> getItems() { return items; }
    public void setItems(List<ApplicationResponse> items) { this.items = items; }
    
    public EquipmentPageResponse.Pagination getPagination() { return pagination; }
    public void setPagination(EquipmentPageResponse.Pagination pagination) { this.pagination = pagination; }
}





