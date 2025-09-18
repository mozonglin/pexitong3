package com.example.pexitong2.dto.equipment;

import com.example.pexitong2.entity.EquipmentAdjustment;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class EquipmentAdjustmentResponse {
    
    @JsonProperty("equipment_id")
    private String equipmentId;
    
    @JsonProperty("adjustment_id")
    private String adjustmentId;
    
    private EquipmentAdjustment.AdjustmentType type;
    private Integer quantity;
    
    @JsonProperty("before_quantity")
    private Integer beforeQuantity;
    
    @JsonProperty("after_quantity")
    private Integer afterQuantity;
    
    private String reason;
    private String operator;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    // 构造函数
    public EquipmentAdjustmentResponse() {}
    
    public EquipmentAdjustmentResponse(EquipmentAdjustment adjustment) {
        this.equipmentId = adjustment.getEquipmentId();
        this.adjustmentId = adjustment.getId();
        this.type = adjustment.getType();
        this.quantity = adjustment.getQuantity();
        this.beforeQuantity = adjustment.getBeforeQuantity();
        this.afterQuantity = adjustment.getAfterQuantity();
        this.reason = adjustment.getReason();
        this.operator = adjustment.getOperator();
        this.createdAt = adjustment.getCreatedAt();
    }
    
    // Getters and Setters
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    
    public String getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(String adjustmentId) { this.adjustmentId = adjustmentId; }
    
    public EquipmentAdjustment.AdjustmentType getType() { return type; }
    public void setType(EquipmentAdjustment.AdjustmentType type) { this.type = type; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Integer getBeforeQuantity() { return beforeQuantity; }
    public void setBeforeQuantity(Integer beforeQuantity) { this.beforeQuantity = beforeQuantity; }
    
    public Integer getAfterQuantity() { return afterQuantity; }
    public void setAfterQuantity(Integer afterQuantity) { this.afterQuantity = afterQuantity; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
