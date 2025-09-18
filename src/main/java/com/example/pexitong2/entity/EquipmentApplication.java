package com.example.pexitong2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_applications")
public class EquipmentApplication {
    
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;
    
    @Column(name = "equipment_id", nullable = false, length = 36)
    private String equipmentId;
    
    @Column(name = "borrower_id", nullable = false, length = 36)
    private String borrowerId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, length = 200)
    private String purpose;
    
    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;
    
    @Column(name = "expected_return_date", nullable = false)
    private LocalDateTime expectedReturnDate;
    
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.pending;
    
    @Column(length = 200)
    private String remark;
    
    @Column(name = "approved_by", length = 36)
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "actual_quantity")
    private Integer actualQuantity; // 实际归还数量
    
    @Enumerated(EnumType.STRING)
    @Column(name = "return_condition")
    private ReturnCondition returnCondition;
    
    @Column(name = "returned_by", length = 36)
    private String returnedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public EquipmentApplication() {}
    
    public EquipmentApplication(String equipmentId, String borrowerId, Integer quantity, 
                               String purpose, LocalDateTime borrowDate, LocalDateTime expectedReturnDate) {
        this.equipmentId = equipmentId;
        this.borrowerId = borrowerId;
        this.quantity = quantity;
        this.purpose = purpose;
        this.borrowDate = borrowDate;
        this.expectedReturnDate = expectedReturnDate;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    
    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }
    
    public LocalDateTime getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    
    public LocalDateTime getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDateTime actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; }
    
    public ReturnCondition getReturnCondition() { return returnCondition; }
    public void setReturnCondition(ReturnCondition returnCondition) { this.returnCondition = returnCondition; }
    
    public String getReturnedBy() { return returnedBy; }
    public void setReturnedBy(String returnedBy) { this.returnedBy = returnedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 枚举定义
    public enum ApplicationStatus {
        pending, approved, rejected, returned
    }
    
    public enum ReturnCondition {
        good, damaged, lost
    }
}





