package com.example.pexitong2.dto.venue;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationApprovalRequest {

    private boolean approved;

    @JsonProperty("reject_reason")
    private String rejectReason;

    private String remark;

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
