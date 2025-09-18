package com.example.pexitong2.service.equipment;

import com.example.pexitong2.dto.equipment.EquipmentCategoryResponse;
import com.example.pexitong2.entity.EquipmentCategory;
import com.example.pexitong2.repository.EquipmentCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentCategoryService {
    
    @Autowired
    private EquipmentCategoryRepository categoryRepository;
    
    /**
     * 获取所有器材分类
     */
    public List<EquipmentCategoryResponse> getAllCategories() {
        List<EquipmentCategory> categories = categoryRepository.findAllByOrderBySortAscCreatedAtAsc();
        return categories.stream()
                .map(EquipmentCategoryResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取分类
     */
    public EquipmentCategory getCategoryById(String id) {
        return categoryRepository.findById(id).orElse(null);
    }
    
    /**
     * 检查分类是否存在
     */
    public boolean categoryExists(String categoryId) {
        return categoryRepository.existsById(categoryId);
    }
}





