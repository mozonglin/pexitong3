package com.example.pexitong2.dto.equipment;

import com.example.pexitong2.entity.Equipment;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EquipmentResponse {
    
    private String id;
    
    @JsonProperty("category_id")
    private String categoryId;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    private String name;
    private String model;
    private String specification;
    
    @JsonProperty("total_quantity")
    private Integer totalQuantity;
    
    @JsonProperty("available_quantity")
    private Integer availableQuantity;
    
    @JsonProperty("borrowed_quantity")
    private Integer borrowedQuantity;
    
    @JsonProperty("damaged_quantity")
    private Integer damagedQuantity;
    
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @JsonProperty("purchase_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
    
    @JsonProperty("warranty_period")
    private Integer warrantyPeriod;
    
    @JsonProperty("storage_location")
    private String storageLocation;

    private String school;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public EquipmentResponse() {}
    
    public EquipmentResponse(Equipment equipment) {
        this.id = equipment.getId();
        this.categoryId = equipment.getCategoryId();
        this.name = equipment.getName();
        this.model = equipment.getModel();
        this.specification = equipment.getSpecification();
        this.totalQuantity = equipment.getTotalQuantity();
        this.availableQuantity = equipment.getAvailableQuantity();
        this.borrowedQuantity = equipment.getBorrowedQuantity();
        this.damagedQuantity = equipment.getDamagedQuantity();
        this.unitPrice = equipment.getUnitPrice();
        this.purchaseDate = equipment.getPurchaseDate();
        this.warrantyPeriod = equipment.getWarrantyPeriod();
        this.storageLocation = equipment.getStorageLocation();
        this.school = equipment.getSchool();
        this.createdBy = equipment.getCreatedBy();
        this.createdAt = equipment.getCreatedAt();
        this.updatedAt = equipment.getUpdatedAt();
    }
    
    public EquipmentResponse(Equipment equipment, String categoryName) {
        this(equipment);
        this.categoryName = categoryName;
    }
    
    // Getters and Setters
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    
    public Integer getBorrowedQuantity() { return borrowedQuantity; }
    public void setBorrowedQuantity(Integer borrowedQuantity) { this.borrowedQuantity = borrowedQuantity; }
    
    public Integer getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(Integer damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public Integer getWarrantyPeriod() { return warrantyPeriod; }
    public void setWarrantyPeriod(Integer warrantyPeriod) { this.warrantyPeriod = warrantyPeriod; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
