package com.example.pexitong2.dto.equipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class EquipmentUpdateRequest {
    
    @Size(max = 50, message = "器材名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 30, message = "型号长度不能超过30个字符")
    private String model;
    
    @Size(max = 200, message = "规格描述长度不能超过200个字符")
    private String specification;
    
    @DecimalMin(value = "0.0", message = "单价不能为负数")
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @Size(max = 50, message = "存放位置长度不能超过50个字符")
    @JsonProperty("storage_location")
    private String storageLocation;
    
    // 构造函数
    public EquipmentUpdateRequest() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
}
