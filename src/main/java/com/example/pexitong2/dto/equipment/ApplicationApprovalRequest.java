package com.example.pexitong2.dto.equipment;

import com.example.pexitong2.entity.EquipmentApplication;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ApplicationApprovalRequest {
    
    @NotNull(message = "审批状态不能为空")
    private EquipmentApplication.ApplicationStatus status;
    
    @Size(max = 200, message = "审批备注长度不能超过200个字符")
    private String remark;
    
    // 构造函数
    public ApplicationApprovalRequest() {}
    
    // Getters and Setters
    public EquipmentApplication.ApplicationStatus getStatus() { return status; }
    public void setStatus(EquipmentApplication.ApplicationStatus status) { this.status = status; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}





