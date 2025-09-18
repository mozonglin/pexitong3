package com.example.pexitong2.dto.equipment;

import com.example.pexitong2.entity.EquipmentApplication;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class ApplicationReturnRequest {
    
    @NotNull(message = "实际归还数量不能为空")
    @Min(value = 1, message = "实际归还数量必须大于0")
    @JsonProperty("actual_quantity")
    private Integer actualQuantity;
    
    @NotNull(message = "器材状态不能为空")
    private EquipmentApplication.ReturnCondition condition;
    
    @Size(max = 200, message = "归还备注长度不能超过200个字符")
    private String remark;
    
    // 构造函数
    public ApplicationReturnRequest() {}
    
    // Getters and Setters
    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; }
    
    public EquipmentApplication.ReturnCondition getCondition() { return condition; }
    public void setCondition(EquipmentApplication.ReturnCondition condition) { this.condition = condition; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
