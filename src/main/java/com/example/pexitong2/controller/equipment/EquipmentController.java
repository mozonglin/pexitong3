package com.example.pexitong2.controller.equipment;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.dto.equipment.*;
import com.example.pexitong2.service.equipment.EquipmentApplicationService;
import com.example.pexitong2.service.equipment.EquipmentCategoryService;
import com.example.pexitong2.service.equipment.EquipmentManagementService;
import com.example.pexitong2.service.equipment.EquipmentPermissionService;
import com.example.pexitong2.service.equipment.EquipmentStatisticsService;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {
    
    @Autowired
    private EquipmentCategoryService categoryService;
    
    @Autowired
    private EquipmentManagementService equipmentService;
    
    @Autowired
    private EquipmentApplicationService applicationService;
    
    @Autowired
    private EquipmentStatisticsService statisticsService;
    
    @Autowired
    private EquipmentPermissionService permissionService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取器材分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<EquipmentCategoryResponse>>> getCategories(HttpServletRequest request) {
        try {
            String userId = getUserIdFromRequest(request);
            permissionService.validateEquipmentManagementPermission(userId);
            
            List<EquipmentCategoryResponse> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success("获取成功", categories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取器材列表
     */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<EquipmentPageResponse>> getEquipmentList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(name = "category_id", required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        try {
            String userId = getUserIdFromRequest(request);
            permissionService.validateEquipmentManagementPermission(userId);
            
            EquipmentPageResponse result = equipmentService.getEquipmentList(
                    page, limit, categoryId, keyword, status);
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 添加器材
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<EquipmentResponse>> addEquipment(
            @Valid @RequestBody EquipmentRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            permissionService.validateEquipmentManagementPermission(userId);
            
            EquipmentResponse result = equipmentService.addEquipment(request, userId);
            return ResponseEntity.ok(ApiResponse.success("添加成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新器材信息
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipment(
            @PathVariable String id,
            @Valid @RequestBody EquipmentUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            permissionService.validateEquipmentManagementPermission(userId);
            
            EquipmentResponse result = equipmentService.updateEquipment(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除器材
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteEquipment(
            @PathVariable String id,
            HttpServletRequest request) {
        try {
            String userId = getUserIdFromRequest(request);
            permissionService.validateDeleteEquipmentPermission(userId);
            
            equipmentService.deleteEquipment(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 库存调整
     */
    @PostMapping("/items/{id}/adjust")
    public ResponseEntity<ApiResponse<EquipmentAdjustmentResponse>> adjustInventory(
            @PathVariable String id,
            @Valid @RequestBody EquipmentAdjustmentRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            permissionService.validateEquipmentManagementPermission(userId);
            
            EquipmentAdjustmentResponse result = equipmentService.adjustInventory(id, request, userId);
            return ResponseEntity.ok(ApiResponse.success("库存调整成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取借用申请列表
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<ApplicationPageResponse>> getApplicationList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(name = "equipment_id", required = false) String equipmentId,
            @RequestParam(name = "borrower_id", required = false) String borrowerId,
            @RequestParam(name = "date_from", required = false) String dateFrom,
            @RequestParam(name = "date_to", required = false) String dateTo,
            HttpServletRequest request) {
        try {
            String userId = getUserIdFromRequest(request);
            permissionService.validateEquipmentManagementPermission(userId);
            
            ApplicationPageResponse result = applicationService.getApplicationList(
                    page, limit, status, equipmentId, borrowerId, dateFrom, dateTo);
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 审批借用申请
     */
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<ApiResponse<Object>> approveApplication(
            @PathVariable String id,
            @Valid @RequestBody ApplicationApprovalRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            permissionService.validateEquipmentManagementPermission(userId);
            
            applicationService.approveApplication(id, request, userId);
            return ResponseEntity.ok(ApiResponse.success("审批成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 器材归还
     */
    @PostMapping("/applications/{id}/return")
    public ResponseEntity<ApiResponse<Object>> returnEquipment(
            @PathVariable String id,
            @Valid @RequestBody ApplicationReturnRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            permissionService.validateEquipmentManagementPermission(userId);
            
            applicationService.returnEquipment(id, request, userId);
            return ResponseEntity.ok(ApiResponse.success("归还成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取器材统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<EquipmentStatisticsResponse>> getStatistics(
            @RequestParam(required = false) String period,
            HttpServletRequest request) {
        try {
            String userId = getUserIdFromRequest(request);
            permissionService.validateEquipmentManagementPermission(userId);
            
            EquipmentStatisticsResponse result = statisticsService.getStatistics(period);
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 从请求中获取用户ID
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("未提供有效的认证令牌");
    }
}





