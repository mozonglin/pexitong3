package com.example.pexitong2.controller.pe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调试控制器 - 用于检查数据库中的枚举值
 * 生产环境中应该删除此控制器
 */
@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * 检查users1表中的role值
     */
    @GetMapping("/check-roles")
    public Map<String, Object> checkRoles() {
        Map<String, Object> result = new HashMap<>();
        List<String> roles = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT DISTINCT role FROM users1 WHERE role IS NOT NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String role = rs.getString("role");
                roles.add(role);
            }
            
            result.put("success", true);
            result.put("roles", roles);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查activities表中的approval_status值
     */
    @GetMapping("/check-approval-status")
    public Map<String, Object> checkApprovalStatus() {
        Map<String, Object> result = new HashMap<>();
        List<String> statuses = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT DISTINCT approval_status FROM activities WHERE approval_status IS NOT NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String status = rs.getString("approval_status");
                statuses.add(status);
            }
            
            result.put("success", true);
            result.put("statuses", statuses);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查users1表的基本信息
     */
    @GetMapping("/check-users1-info")
    public Map<String, Object> checkUsers1Info() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) as total, " +
                        "COUNT(CASE WHEN role IS NULL THEN 1 END) as null_roles, " +
                        "COUNT(CASE WHEN role = '' THEN 1 END) as empty_roles " +
                        "FROM users1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                result.put("total_users", rs.getInt("total"));
                result.put("null_roles", rs.getInt("null_roles"));
                result.put("empty_roles", rs.getInt("empty_roles"));
            }
            
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}




