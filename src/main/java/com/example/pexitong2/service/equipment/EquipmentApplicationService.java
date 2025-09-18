package com.example.pexitong2.service.equipment;

import com.example.pexitong2.dto.equipment.*;
import com.example.pexitong2.entity.Equipment;
import com.example.pexitong2.entity.EquipmentApplication;
import com.example.pexitong2.entity.pe.PeUser;
import com.example.pexitong2.repository.EquipmentApplicationRepository;
import com.example.pexitong2.repository.EquipmentRepository;
import com.example.pexitong2.repository.pe.PeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EquipmentApplicationService {
    
    @Autowired
    private EquipmentApplicationRepository applicationRepository;
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private PeUserRepository peUserRepository;
    
    @Autowired
    private EquipmentManagementService equipmentManagementService;
    
    /**
     * 获取借用申请列表
     */
    public ApplicationPageResponse getApplicationList(Integer page, Integer limit, String status, 
                                                     String equipmentId, String borrowerId, 
                                                     String dateFrom, String dateTo) {
        // 参数验证和默认值设置
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1) limit = 10;
        if (limit > 100) limit = 100;
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        
        // 解析状态参数
        EquipmentApplication.ApplicationStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = EquipmentApplication.ApplicationStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的状态参数");
            }
        }
        
        // 解析日期参数
        LocalDateTime dateFromParsed = null;
        LocalDateTime dateToParsed = null;
        if (dateFrom != null && !dateFrom.isEmpty()) {
            dateFromParsed = LocalDateTime.parse(dateFrom + "T00:00:00");
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            dateToParsed = LocalDateTime.parse(dateTo + "T23:59:59");
        }
        
        // 查询申请
        Page<EquipmentApplication> applicationPage = applicationRepository.findWithFilters(
                statusEnum, equipmentId, borrowerId, dateFromParsed, dateToParsed, pageable);
        
        // 转换为响应DTO
        List<ApplicationResponse> applicationResponses = applicationPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // 构建分页信息
        EquipmentPageResponse.Pagination pagination = new EquipmentPageResponse.Pagination(
                applicationPage.getTotalElements(), page, limit);
        
        return new ApplicationPageResponse(applicationResponses, pagination);
    }
    
    /**
     * 审批借用申请
     */
    @Transactional
    public void approveApplication(String applicationId, ApplicationApprovalRequest request, String approverId) {
        Optional<EquipmentApplication> applicationOpt = applicationRepository.findById(applicationId);
        if (!applicationOpt.isPresent()) {
            throw new RuntimeException("申请不存在");
        }
        
        EquipmentApplication application = applicationOpt.get();
        
        // 检查申请状态
        if (application.getStatus() != EquipmentApplication.ApplicationStatus.pending) {
            throw new RuntimeException("申请状态不允许审批");
        }
        
        // 如果是批准申请，需要检查库存
        if (request.getStatus() == EquipmentApplication.ApplicationStatus.approved) {
            Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(application.getEquipmentId());
            if (!equipmentOpt.isPresent()) {
                throw new RuntimeException("器材不存在");
            }
            
            Equipment equipment = equipmentOpt.get();
            if (equipment.getAvailableQuantity() < application.getQuantity()) {
                throw new RuntimeException("库存不足，当前可用数量：" + equipment.getAvailableQuantity() + 
                                         "，申请数量：" + application.getQuantity());
            }
            
            // 扣减库存
            equipmentManagementService.updateEquipmentQuantity(
                    application.getEquipmentId(), 
                    application.getQuantity(),  // 增加借出数量
                    -application.getQuantity(), // 减少可用数量
                    0  // 损坏数量不变
            );
        }
        
        // 更新申请状态
        application.setStatus(request.getStatus());
        application.setRemark(request.getRemark());
        application.setApprovedBy(approverId);
        application.setApprovedAt(LocalDateTime.now());
        
        applicationRepository.save(application);
    }
    
    /**
     * 器材归还
     */
    @Transactional
    public void returnEquipment(String applicationId, ApplicationReturnRequest request, String operatorId) {
        Optional<EquipmentApplication> applicationOpt = applicationRepository.findById(applicationId);
        if (!applicationOpt.isPresent()) {
            throw new RuntimeException("申请不存在");
        }
        
        EquipmentApplication application = applicationOpt.get();
        
        // 检查申请状态
        if (application.getStatus() != EquipmentApplication.ApplicationStatus.approved) {
            throw new RuntimeException("申请状态不允许归还");
        }
        
        if (application.getActualReturnDate() != null) {
            throw new RuntimeException("器材已归还");
        }
        
        // 验证归还数量
        if (request.getActualQuantity() > application.getQuantity()) {
            throw new RuntimeException("归还数量不能超过借用数量");
        }
        
        // 更新库存
        int borrowedDelta = -request.getActualQuantity(); // 减少借出数量
        int availableDelta = 0;
        int damagedDelta = 0;
        
        switch (request.getCondition()) {
            case good:
                availableDelta = request.getActualQuantity(); // 增加可用数量
                break;
            case damaged:
                damagedDelta = request.getActualQuantity(); // 增加损坏数量
                break;
            case lost:
                // 丢失的器材不回到库存
                break;
        }
        
        equipmentManagementService.updateEquipmentQuantity(
                application.getEquipmentId(), borrowedDelta, availableDelta, damagedDelta);
        
        // 更新申请状态
        application.setStatus(EquipmentApplication.ApplicationStatus.returned);
        application.setActualReturnDate(LocalDateTime.now());
        application.setActualQuantity(request.getActualQuantity());
        application.setReturnCondition(request.getCondition());
        application.setReturnedBy(operatorId);
        if (request.getRemark() != null) {
            application.setRemark(request.getRemark());
        }
        
        applicationRepository.save(application);
    }
    
    /**
     * 转换申请实体为响应DTO
     */
    private ApplicationResponse convertToResponse(EquipmentApplication application) {
        ApplicationResponse response = new ApplicationResponse(application);
        
        // 设置器材信息
        Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(application.getEquipmentId());
        if (equipmentOpt.isPresent()) {
            Equipment equipment = equipmentOpt.get();
            response.setEquipmentName(equipment.getName());
            response.setEquipmentModel(equipment.getModel());
        }
        
        // 设置借用人信息 - 从users1表获取
        Optional<PeUser> peUserOpt = peUserRepository.findById(application.getBorrowerId());
        if (peUserOpt.isPresent()) {
            PeUser peUser = peUserOpt.get();
            response.setBorrowerName(peUser.getName());
            response.setBorrowerType(peUser.getRole().toString().toLowerCase());
            response.setBorrowerContact(peUser.getPhoneNumber());
        } else {
            // 如果在users1表中找不到，设置默认值
            response.setBorrowerName("未知用户");
            response.setBorrowerType("student");
            response.setBorrowerContact("");
        }
        
        return response;
    }
}
