package com.example.pexitong2.dto.equipment;

import com.example.pexitong2.entity.EquipmentApplication;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ApplicationResponse {
    
    private String id;
    
    @JsonProperty("equipment_id")
    private String equipmentId;
    
    @JsonProperty("equipment_name")
    private String equipmentName;
    
    @JsonProperty("equipment_model")
    private String equipmentModel;
    
    @JsonProperty("borrower_id")
    private String borrowerId;
    
    @JsonProperty("borrower_name")
    private String borrowerName;
    
    @JsonProperty("borrower_type")
    private String borrowerType;
    
    @JsonProperty("borrower_contact")
    private String borrowerContact;
    
    private Integer quantity;
    private String purpose;
    
    @JsonProperty("borrow_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime borrowDate;
    
    @JsonProperty("expected_return_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime expectedReturnDate;
    
    @JsonProperty("actual_return_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime actualReturnDate;
    
    private EquipmentApplication.ApplicationStatus status;
    private String remark;
    
    @JsonProperty("approved_by")
    private String approvedBy;
    
    @JsonProperty("approved_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime approvedAt;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public ApplicationResponse() {}
    
    public ApplicationResponse(EquipmentApplication application) {
        this.id = application.getId();
        this.equipmentId = application.getEquipmentId();
        this.borrowerId = application.getBorrowerId();
        this.quantity = application.getQuantity();
        this.purpose = application.getPurpose();
        this.borrowDate = application.getBorrowDate();
        this.expectedReturnDate = application.getExpectedReturnDate();
        this.actualReturnDate = application.getActualReturnDate();
        this.status = application.getStatus();
        this.remark = application.getRemark();
        this.approvedBy = application.getApprovedBy();
        this.approvedAt = application.getApprovedAt();
        this.createdAt = application.getCreatedAt();
        this.updatedAt = application.getUpdatedAt();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    
    public String getEquipmentModel() { return equipmentModel; }
    public void setEquipmentModel(String equipmentModel) { this.equipmentModel = equipmentModel; }
    
    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
    
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    
    public String getBorrowerType() { return borrowerType; }
    public void setBorrowerType(String borrowerType) { this.borrowerType = borrowerType; }
    
    public String getBorrowerContact() { return borrowerContact; }
    public void setBorrowerContact(String borrowerContact) { this.borrowerContact = borrowerContact; }
    
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
    
    public EquipmentApplication.ApplicationStatus getStatus() { return status; }
    public void setStatus(EquipmentApplication.ApplicationStatus status) { this.status = status; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
