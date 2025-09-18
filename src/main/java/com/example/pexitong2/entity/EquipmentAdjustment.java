package com.example.pexitong2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_adjustments")
public class EquipmentAdjustment {
    
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;
    
    @Column(name = "equipment_id", nullable = false, length = 36)
    private String equipmentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdjustmentType type;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;
    
    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;
    
    @Column(nullable = false, length = 200)
    private String reason;
    
    @Column(nullable = false, length = 36)
    private String operator;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 构造函数
    public EquipmentAdjustment() {}
    
    public EquipmentAdjustment(String equipmentId, AdjustmentType type, Integer quantity, 
                              Integer beforeQuantity, Integer afterQuantity, String reason, String operator) {
        this.equipmentId = equipmentId;
        this.type = type;
        this.quantity = quantity;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.reason = reason;
        this.operator = operator;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    
    public AdjustmentType getType() { return type; }
    public void setType(AdjustmentType type) { this.type = type; }
    
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
    
    // 枚举定义
    public enum AdjustmentType {
        increase, decrease, damage
    }
}





