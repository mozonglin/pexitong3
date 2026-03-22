package com.example.pexitong2.service.equipment;

import com.example.pexitong2.dto.equipment.*;
import com.example.pexitong2.entity.Equipment;
import com.example.pexitong2.entity.EquipmentAdjustment;
import com.example.pexitong2.entity.EquipmentCategory;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.EquipmentAdjustmentRepository;
import com.example.pexitong2.repository.EquipmentRepository;
import com.example.pexitong2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EquipmentManagementService {
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private EquipmentAdjustmentRepository adjustmentRepository;
    
    @Autowired
    private EquipmentCategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    private String getUserSchool(String userId) {
        return userRepository.findById(userId)
                .map(User::getSchool)
                .orElse(null);
    }
    
    /**
     * 获取器材列表（支持分页和筛选，按学校隔离）
     */
    public EquipmentPageResponse getEquipmentList(Integer page, Integer limit, String categoryId,
                                                  String keyword, String status, String operatorId) {
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1) limit = 10;
        if (limit > 100) limit = 100;
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        String school = getUserSchool(operatorId);

        Page<Equipment> equipmentPage;
        if (school != null) {
            equipmentPage = equipmentRepository.findBySchoolWithFilters(
                    school,
                    (categoryId != null && !categoryId.isEmpty()) ? categoryId : null,
                    (keyword != null && !keyword.isEmpty()) ? keyword : null,
                    (status != null && !status.isEmpty() && !status.equals("all")) ? status : null,
                    pageable);
        } else {
            equipmentPage = equipmentRepository.findWithFilters(categoryId, keyword, status, pageable);
        }
        
        List<EquipmentResponse> equipmentResponses = equipmentPage.getContent().stream()
                .map(equipment -> {
                    EquipmentResponse response = new EquipmentResponse(equipment);
                    EquipmentCategory category = categoryService.getCategoryById(equipment.getCategoryId());
                    if (category != null) {
                        response.setCategoryName(category.getName());
                    }
                    return response;
                })
                .collect(Collectors.toList());
        
        EquipmentPageResponse.Pagination pagination = new EquipmentPageResponse.Pagination(
                equipmentPage.getTotalElements(), page, limit);
        
        return new EquipmentPageResponse(equipmentResponses, pagination);
    }
    
    /**
     * 添加器材
     */
    @Transactional
    public EquipmentResponse addEquipment(EquipmentRequest request, String operatorId) {
        // 验证分类是否存在
        if (!categoryService.categoryExists(request.getCategoryId())) {
            throw new RuntimeException("器材分类不存在");
        }
        
        // 创建器材实体
        Equipment equipment = new Equipment();
        equipment.setCategoryId(request.getCategoryId());
        equipment.setName(request.getName());
        equipment.setModel(request.getModel());
        equipment.setSpecification(request.getSpecification());
        equipment.setTotalQuantity(request.getTotalQuantity());
        equipment.setAvailableQuantity(request.getTotalQuantity());
        equipment.setUnitPrice(request.getUnitPrice());
        equipment.setPurchaseDate(request.getPurchaseDate());
        equipment.setWarrantyPeriod(request.getWarrantyPeriod());
        equipment.setStorageLocation(request.getStorageLocation());
        equipment.setSchool(getUserSchool(operatorId));
        equipment.setCreatedBy(operatorId);
        
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return new EquipmentResponse(savedEquipment);
    }
    
    /**
     * 更新器材信息
     */
    @Transactional
    public EquipmentResponse updateEquipment(String id, EquipmentUpdateRequest request) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(id);
        if (!equipmentOpt.isPresent()) {
            throw new RuntimeException("器材不存在");
        }
        
        Equipment equipment = equipmentOpt.get();
        
        // 更新字段
        if (request.getName() != null) {
            equipment.setName(request.getName());
        }
        if (request.getModel() != null) {
            equipment.setModel(request.getModel());
        }
        if (request.getSpecification() != null) {
            equipment.setSpecification(request.getSpecification());
        }
        if (request.getUnitPrice() != null) {
            equipment.setUnitPrice(request.getUnitPrice());
        }
        if (request.getStorageLocation() != null) {
            equipment.setStorageLocation(request.getStorageLocation());
        }
        
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return new EquipmentResponse(savedEquipment);
    }
    
    /**
     * 删除器材
     */
    @Transactional
    public void deleteEquipment(String id) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(id);
        if (!equipmentOpt.isPresent()) {
            throw new RuntimeException("器材不存在");
        }
        
        Equipment equipment = equipmentOpt.get();
        
        // 检查是否有借用记录
        if (equipment.getBorrowedQuantity() > 0) {
            throw new RuntimeException("器材仍有借用记录，无法删除");
        }
        
        // 软删除
        equipment.setIsDeleted(true);
        equipmentRepository.save(equipment);
    }
    
    /**
     * 库存调整
     */
    @Transactional
    public EquipmentAdjustmentResponse adjustInventory(String id, EquipmentAdjustmentRequest request, String operatorId) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(id);
        if (!equipmentOpt.isPresent()) {
            throw new RuntimeException("器材不存在");
        }
        
        Equipment equipment = equipmentOpt.get();
        
        // 记录调整前的数量
        Integer beforeQuantity;
        Integer afterQuantity;
        
        switch (request.getType()) {
            case increase:
                beforeQuantity = equipment.getTotalQuantity();
                afterQuantity = beforeQuantity + request.getQuantity();
                
                equipment.setTotalQuantity(afterQuantity);
                equipment.setAvailableQuantity(equipment.getAvailableQuantity() + request.getQuantity());
                break;
                
            case decrease:
                beforeQuantity = equipment.getTotalQuantity();
                if (beforeQuantity < request.getQuantity()) {
                    throw new RuntimeException("减少数量不能超过总数量");
                }
                if (equipment.getAvailableQuantity() < request.getQuantity()) {
                    throw new RuntimeException("减少数量不能超过可用数量");
                }
                
                afterQuantity = beforeQuantity - request.getQuantity();
                equipment.setTotalQuantity(afterQuantity);
                equipment.setAvailableQuantity(equipment.getAvailableQuantity() - request.getQuantity());
                break;
                
            case damage:
                beforeQuantity = equipment.getAvailableQuantity();
                if (beforeQuantity < request.getQuantity()) {
                    throw new RuntimeException("损坏数量不能超过可用数量");
                }
                
                afterQuantity = beforeQuantity - request.getQuantity();
                equipment.setAvailableQuantity(afterQuantity);
                equipment.setDamagedQuantity(equipment.getDamagedQuantity() + request.getQuantity());
                break;
                
            default:
                throw new RuntimeException("不支持的调整类型");
        }
        
        // 保存器材更新
        equipmentRepository.save(equipment);
        
        // 创建调整记录
        EquipmentAdjustment adjustment = new EquipmentAdjustment(
                id, request.getType(), request.getQuantity(),
                beforeQuantity, afterQuantity, request.getReason(), operatorId
        );
        
        EquipmentAdjustment savedAdjustment = adjustmentRepository.save(adjustment);
        return new EquipmentAdjustmentResponse(savedAdjustment);
    }
    
    /**
     * 获取器材详情
     */
    public EquipmentResponse getEquipmentById(String id) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(id);
        if (!equipmentOpt.isPresent()) {
            throw new RuntimeException("器材不存在");
        }
        
        Equipment equipment = equipmentOpt.get();
        EquipmentResponse response = new EquipmentResponse(equipment);
        
        // 设置分类名称
        EquipmentCategory category = categoryService.getCategoryById(equipment.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
        }
        
        return response;
    }
    
    /**
     * 更新器材库存（内部方法，用于借用/归还时调用）
     */
    @Transactional
    public void updateEquipmentQuantity(String equipmentId, int borrowedDelta, int availableDelta, int damagedDelta) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(equipmentId);
        if (!equipmentOpt.isPresent()) {
            throw new RuntimeException("器材不存在");
        }
        
        Equipment equipment = equipmentOpt.get();
        equipment.setBorrowedQuantity(equipment.getBorrowedQuantity() + borrowedDelta);
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + availableDelta);
        equipment.setDamagedQuantity(equipment.getDamagedQuantity() + damagedDelta);
        
        equipmentRepository.save(equipment);
    }
}
