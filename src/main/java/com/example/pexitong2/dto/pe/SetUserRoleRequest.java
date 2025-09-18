package com.example.pexitong2.dto.pe;

public class SetUserRoleRequest {
    
    private String role;  // STUDENT, CHECKER, SUB_CHECKER, ADMIN
    
    public SetUserRoleRequest() {}
    
    public SetUserRoleRequest(String role) {
        this.role = role;
    }
    
    // Getters and Setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}




