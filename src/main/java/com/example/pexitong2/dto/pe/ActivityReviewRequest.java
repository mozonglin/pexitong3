package com.example.pexitong2.dto.pe;

public class ActivityReviewRequest {
    
    private String status;  // APPROVED / REJECTED
    private String comment;
    
    public ActivityReviewRequest() {}
    
    public ActivityReviewRequest(String status, String comment) {
        this.status = status;
        this.comment = comment;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}




