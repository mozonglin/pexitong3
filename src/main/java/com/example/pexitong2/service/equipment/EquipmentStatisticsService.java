package com.example.pexitong2.service.equipment;

import com.example.pexitong2.dto.equipment.EquipmentStatisticsResponse;
import com.example.pexitong2.entity.Equipment;
import com.example.pexitong2.entity.EquipmentApplication;
import com.example.pexitong2.entity.EquipmentCategory;
import com.example.pexitong2.repository.EquipmentApplicationRepository;
import com.example.pexitong2.repository.EquipmentCategoryRepository;
import com.example.pexitong2.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EquipmentStatisticsService {
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private EquipmentCategoryRepository categoryRepository;
    
    @Autowired
    private EquipmentApplicationRepository applicationRepository;
    
    /**
     * 获取器材统计信息
     */
    public EquipmentStatisticsResponse getStatistics(String period) {
        // 器材汇总统计
        EquipmentStatisticsResponse.EquipmentSummary equipmentSummary = getEquipmentSummary();
        
        // 申请汇总统计
        EquipmentStatisticsResponse.ApplicationSummary applicationSummary = getApplicationSummary();
        
        // 热门器材统计
        List<EquipmentStatisticsResponse.PopularEquipment> popularEquipment = getPopularEquipment();
        
        return new EquipmentStatisticsResponse(equipmentSummary, applicationSummary, popularEquipment);
    }
    
    /**
     * 获取器材汇总统计
     */
    private EquipmentStatisticsResponse.EquipmentSummary getEquipmentSummary() {
        long totalItems = equipmentRepository.countByIsDeletedFalse();
        long totalCategories = categoryRepository.count();
        long availableItems = equipmentRepository.countAvailableEquipment();
        
        Long borrowedItems = equipmentRepository.countBorrowedEquipment();
        if (borrowedItems == null) borrowedItems = 0L;
        
        Long damagedItems = equipmentRepository.countDamagedEquipment();
        if (damagedItems == null) damagedItems = 0L;
        
        return new EquipmentStatisticsResponse.EquipmentSummary(
                totalItems, totalCategories, availableItems, borrowedItems, damagedItems);
    }
    
    /**
     * 获取申请汇总统计
     */
    private EquipmentStatisticsResponse.ApplicationSummary getApplicationSummary() {
        long pendingApplications = applicationRepository.countByStatus(EquipmentApplication.ApplicationStatus.pending);
        long todayApplications = applicationRepository.countTodayApplications();
        
        // 计算本周开始时间
        LocalDateTime weekStart = LocalDateTime.now()
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        long weekApplications = applicationRepository.countWeekApplications(weekStart);
        
        // 计算本月开始时间
        LocalDateTime monthStart = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        long monthApplications = applicationRepository.countMonthApplications(monthStart);
        
        return new EquipmentStatisticsResponse.ApplicationSummary(
                pendingApplications, todayApplications, weekApplications, monthApplications);
    }
    
    /**
     * 获取热门器材统计
     */
    private List<EquipmentStatisticsResponse.PopularEquipment> getPopularEquipment() {
        List<Object[]> borrowCounts = applicationRepository.findEquipmentBorrowCounts(PageRequest.of(0, 10));
        List<EquipmentStatisticsResponse.PopularEquipment> popularList = new ArrayList<>();
        
        for (Object[] row : borrowCounts) {
            String equipmentId = (String) row[0];
            Long borrowCount = (Long) row[1];
            
            // 获取器材信息
            Optional<Equipment> equipmentOpt = equipmentRepository.findByIdAndIsDeletedFalse(equipmentId);
            if (equipmentOpt.isPresent()) {
                Equipment equipment = equipmentOpt.get();
                
                // 获取分类名称
                String categoryName = "";
                Optional<EquipmentCategory> categoryOpt = categoryRepository.findById(equipment.getCategoryId());
                if (categoryOpt.isPresent()) {
                    categoryName = categoryOpt.get().getName();
                }
                
                popularList.add(new EquipmentStatisticsResponse.PopularEquipment(
                        equipmentId, equipment.getName(), borrowCount, categoryName));
            }
        }
        
        return popularList;
    }
}





