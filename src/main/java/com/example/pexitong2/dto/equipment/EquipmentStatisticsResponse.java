package com.example.pexitong2.dto.equipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EquipmentStatisticsResponse {
    
    @JsonProperty("equipment_summary")
    private EquipmentSummary equipmentSummary;
    
    @JsonProperty("application_summary")
    private ApplicationSummary applicationSummary;
    
    @JsonProperty("popular_equipment")
    private List<PopularEquipment> popularEquipment;
    
    // 构造函数
    public EquipmentStatisticsResponse() {}
    
    public EquipmentStatisticsResponse(EquipmentSummary equipmentSummary, 
                                     ApplicationSummary applicationSummary, 
                                     List<PopularEquipment> popularEquipment) {
        this.equipmentSummary = equipmentSummary;
        this.applicationSummary = applicationSummary;
        this.popularEquipment = popularEquipment;
    }
    
    // Getters and Setters
    public EquipmentSummary getEquipmentSummary() { return equipmentSummary; }
    public void setEquipmentSummary(EquipmentSummary equipmentSummary) { this.equipmentSummary = equipmentSummary; }
    
    public ApplicationSummary getApplicationSummary() { return applicationSummary; }
    public void setApplicationSummary(ApplicationSummary applicationSummary) { this.applicationSummary = applicationSummary; }
    
    public List<PopularEquipment> getPopularEquipment() { return popularEquipment; }
    public void setPopularEquipment(List<PopularEquipment> popularEquipment) { this.popularEquipment = popularEquipment; }
    
    // 内部类：器材汇总
    public static class EquipmentSummary {
        @JsonProperty("total_items")
        private Long totalItems;
        
        @JsonProperty("total_categories")
        private Long totalCategories;
        
        @JsonProperty("available_items")
        private Long availableItems;
        
        @JsonProperty("borrowed_items")
        private Long borrowedItems;
        
        @JsonProperty("damaged_items")
        private Long damagedItems;
        
        public EquipmentSummary() {}
        
        public EquipmentSummary(Long totalItems, Long totalCategories, Long availableItems, 
                               Long borrowedItems, Long damagedItems) {
            this.totalItems = totalItems;
            this.totalCategories = totalCategories;
            this.availableItems = availableItems;
            this.borrowedItems = borrowedItems;
            this.damagedItems = damagedItems;
        }
        
        public Long getTotalItems() { return totalItems; }
        public void setTotalItems(Long totalItems) { this.totalItems = totalItems; }
        
        public Long getTotalCategories() { return totalCategories; }
        public void setTotalCategories(Long totalCategories) { this.totalCategories = totalCategories; }
        
        public Long getAvailableItems() { return availableItems; }
        public void setAvailableItems(Long availableItems) { this.availableItems = availableItems; }
        
        public Long getBorrowedItems() { return borrowedItems; }
        public void setBorrowedItems(Long borrowedItems) { this.borrowedItems = borrowedItems; }
        
        public Long getDamagedItems() { return damagedItems; }
        public void setDamagedItems(Long damagedItems) { this.damagedItems = damagedItems; }
    }
    
    // 内部类：申请汇总
    public static class ApplicationSummary {
        @JsonProperty("pending_applications")
        private Long pendingApplications;
        
        @JsonProperty("today_applications")
        private Long todayApplications;
        
        @JsonProperty("week_applications")
        private Long weekApplications;
        
        @JsonProperty("month_applications")
        private Long monthApplications;
        
        public ApplicationSummary() {}
        
        public ApplicationSummary(Long pendingApplications, Long todayApplications, 
                                 Long weekApplications, Long monthApplications) {
            this.pendingApplications = pendingApplications;
            this.todayApplications = todayApplications;
            this.weekApplications = weekApplications;
            this.monthApplications = monthApplications;
        }
        
        public Long getPendingApplications() { return pendingApplications; }
        public void setPendingApplications(Long pendingApplications) { this.pendingApplications = pendingApplications; }
        
        public Long getTodayApplications() { return todayApplications; }
        public void setTodayApplications(Long todayApplications) { this.todayApplications = todayApplications; }
        
        public Long getWeekApplications() { return weekApplications; }
        public void setWeekApplications(Long weekApplications) { this.weekApplications = weekApplications; }
        
        public Long getMonthApplications() { return monthApplications; }
        public void setMonthApplications(Long monthApplications) { this.monthApplications = monthApplications; }
    }
    
    // 内部类：热门器材
    public static class PopularEquipment {
        @JsonProperty("equipment_id")
        private String equipmentId;
        
        @JsonProperty("equipment_name")
        private String equipmentName;
        
        @JsonProperty("borrow_count")
        private Long borrowCount;
        
        @JsonProperty("category_name")
        private String categoryName;
        
        public PopularEquipment() {}
        
        public PopularEquipment(String equipmentId, String equipmentName, 
                               Long borrowCount, String categoryName) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.borrowCount = borrowCount;
            this.categoryName = categoryName;
        }
        
        public String getEquipmentId() { return equipmentId; }
        public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
        
        public String getEquipmentName() { return equipmentName; }
        public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
        
        public Long getBorrowCount() { return borrowCount; }
        public void setBorrowCount(Long borrowCount) { this.borrowCount = borrowCount; }
        
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    }
}
