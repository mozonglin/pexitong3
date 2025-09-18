package com.example.pexitong2.dto.equipment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EquipmentRequest {
    
    @NotBlank(message = "器材分类不能为空")
    @Size(max = 36, message = "分类ID长度不能超过36个字符")
    @JsonProperty("category_id")
    private String categoryId;
    
    @NotBlank(message = "器材名称不能为空")
    @Size(max = 50, message = "器材名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 30, message = "型号长度不能超过30个字符")
    private String model;
    
    @Size(max = 200, message = "规格描述长度不能超过200个字符")
    private String specification;
    
    @NotNull(message = "总数量不能为空")
    @Min(value = 1, message = "总数量必须大于0")
    @JsonProperty("total_quantity")
    private Integer totalQuantity;
    
    @DecimalMin(value = "0.0", message = "单价不能为负数")
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @JsonProperty("purchase_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
    
    @Min(value = 1, message = "保修期必须大于0")
    @JsonProperty("warranty_period")
    private Integer warrantyPeriod;
    
    @Size(max = 50, message = "存放位置长度不能超过50个字符")
    @JsonProperty("storage_location")
    private String storageLocation;
    
    // 构造函数
    public EquipmentRequest() {}
    
    // Getters and Setters
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
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public Integer getWarrantyPeriod() { return warrantyPeriod; }
    public void setWarrantyPeriod(Integer warrantyPeriod) { this.warrantyPeriod = warrantyPeriod; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
}
