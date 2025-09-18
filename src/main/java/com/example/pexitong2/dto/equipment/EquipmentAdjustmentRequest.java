package com.example.pexitong2.dto.equipment;

import com.example.pexitong2.entity.EquipmentAdjustment;
import jakarta.validation.constraints.*;

public class EquipmentAdjustmentRequest {
    
    @NotNull(message = "调整类型不能为空")
    private EquipmentAdjustment.AdjustmentType type;
    
    @NotNull(message = "调整数量不能为空")
    @Min(value = 1, message = "调整数量必须大于0")
    private Integer quantity;
    
    @NotBlank(message = "调整原因不能为空")
    @Size(max = 200, message = "调整原因长度不能超过200个字符")
    private String reason;
    
    // 构造函数
    public EquipmentAdjustmentRequest() {}
    
    // Getters and Setters
    public EquipmentAdjustment.AdjustmentType getType() { return type; }
    public void setType(EquipmentAdjustment.AdjustmentType type) { this.type = type; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}





