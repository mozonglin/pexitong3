package com.example.pexitong2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_items")
public class Equipment {
    
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;
    
    @Column(name = "category_id", nullable = false, length = 36)
    private String categoryId;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 30)
    private String model;
    
    @Column(length = 200)
    private String specification;
    
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "borrowed_quantity", nullable = false)
    private Integer borrowedQuantity = 0;
    
    @Column(name = "damaged_quantity", nullable = false)
    private Integer damagedQuantity = 0;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "warranty_period")
    private Integer warrantyPeriod; // 保修期（月）
    
    @Column(name = "storage_location", length = 50)
    private String storageLocation;

    @Column(name = "school", length = 100)
    private String school;
    
    @Column(name = "created_by", length = 36)
    private String createdBy;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Equipment() {}
    
    public Equipment(String categoryId, String name, String model, String specification, 
                    Integer totalQuantity, BigDecimal unitPrice, String createdBy) {
        this.categoryId = categoryId;
        this.name = name;
        this.model = model;
        this.specification = specification;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.unitPrice = unitPrice;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
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

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}





