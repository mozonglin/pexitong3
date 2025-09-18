package com.example.pexitong2.service;

import com.example.pexitong2.dto.CreateObservationRequest;
import com.example.pexitong2.dto.ObservationResponse;
import com.example.pexitong2.entity.FileInfo;
import com.example.pexitong2.entity.ListeningObservation;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.CourseRepository;
import com.example.pexitong2.repository.ListeningObservationRepository;
import com.example.pexitong2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ListeningObservationService {
    
    @Autowired
    private ListeningObservationRepository observationRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * 创建听课记录
     */
    @Transactional
    public ObservationResponse createObservation(String observerId, CreateObservationRequest request) {
        System.out.println("开始创建听课记录，observerId: " + observerId + ", courseId: " + request.getCourseId());
        
        // 验证观察者是否存在
        if (!userRepository.existsById(observerId)) {
            throw new IllegalArgumentException("听课者不存在：" + observerId);
        }
        
        // 验证课程是否存在
        boolean courseExists = courseRepository.existsById(request.getCourseId());
        System.out.println("课程ID " + request.getCourseId() + " 是否存在: " + courseExists);
        
        if (!courseExists) {
            throw new IllegalArgumentException("课程不存在：" + request.getCourseId());
        }
        
        // 创建听课记录
        ListeningObservation observation = new ListeningObservation(
            observerId, 
            request.getCourseId(), 
            request.getClassDate()
        );
        
        System.out.println("准备保存听课记录...");
        ListeningObservation savedObservation = observationRepository.save(observation);
        System.out.println("听课记录保存成功，ID: " + savedObservation.getId());
        
        return ObservationResponse.fromEntity(savedObservation);
    }
    
    /**
     * 分页查询听课记录
     */
    public Page<ObservationResponse> getObservations(String userId, String userType, 
                                                    String keyword, String departmentName, 
                                                    LocalDate startDate, LocalDate endDate, 
                                                    Pageable pageable) {
        Page<ListeningObservation> observations;
        
        // 获取当前用户信息以确定学校
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String currentUserSchool = currentUser.getSchool();
        
        switch (userType) {
            case "teacher":
                // 教师只能查看自己作为听课者的记录
                observations = observationRepository.findObservationsByObserver(
                    userId, keyword, startDate, endDate, pageable);
                break;
            case "department_admin":
                // 院级管理员查看本院系的记录，限制在本校内
                String userDepartmentName = currentUser.getDepartmentName();
                if (departmentName == null) {
                    departmentName = userDepartmentName; // 如果没有指定院系，使用用户所在院系
                }
                observations = observationRepository.findObservationsByDepartmentNameAndSchool(
                    departmentName, keyword, startDate, endDate, currentUserSchool, pageable);
                break;
            case "school_admin":
                // 校级管理员可以查看本校所有记录
                if (departmentName != null) {
                    observations = observationRepository.findObservationsByDepartmentNameAndSchool(
                        departmentName, keyword, startDate, endDate, currentUserSchool, pageable);
                } else {
                    observations = observationRepository.findAllObservationsBySchool(
                        keyword, startDate, endDate, currentUserSchool, pageable);
                }
                break;
            case "super_admin":
                // 超级管理员可以查看所有记录（不限制学校）
                if (departmentName != null) {
                    observations = observationRepository.findObservationsByDepartmentName(
                        departmentName, keyword, startDate, endDate, pageable);
                } else {
                    observations = observationRepository.findAllObservations(
                        keyword, startDate, endDate, pageable);
                }
                break;
            default:
                throw new IllegalArgumentException("无权限查看听课记录");
        }
        
        return observations.map(ObservationResponse::fromEntity);
    }
    
    /**
     * 根据ID获取听课记录详情
     */
    public Optional<ObservationResponse> getObservationById(Long id, String userId, String userType) {
        Optional<ListeningObservation> observationOpt = observationRepository.findById(id);
        
        if (observationOpt.isEmpty()) {
            return Optional.empty();
        }
        
        ListeningObservation observation = observationOpt.get();
        
        // 权限检查
        if ("teacher".equals(userType) && !observation.getObserverId().equals(userId)) {
            throw new IllegalArgumentException("无权限查看此听课记录");
        }
        
        return Optional.of(ObservationResponse.fromEntity(observation));
    }
    
    /**
     * 上传评价文件
     */
    @Transactional
    public ObservationResponse uploadEvaluationFile(Long observationId, MultipartFile file, 
                                                   String userId, String userType) throws IOException {
        ListeningObservation observation = observationRepository.findById(observationId)
            .orElseThrow(() -> new IllegalArgumentException("听课记录不存在：" + observationId));
        
        // 权限检查：只有记录的创建者或管理员可以上传文件
        if ("teacher".equals(userType) && !observation.getObserverId().equals(userId)) {
            throw new IllegalArgumentException("无权限上传评价文件");
        }
        
        // 如果已有评价文件，先删除旧文件
        if (observation.getEvaluationFilePath() != null) {
            fileStorageService.deleteFile(observation.getEvaluationFilePath());
        }
        
        // 存储新文件
        FileInfo fileInfo = fileStorageService.storeEvaluationFile(file);
        observation.setEvaluationFile(fileInfo);
        
        ListeningObservation savedObservation = observationRepository.save(observation);
        return ObservationResponse.fromEntity(savedObservation);
    }
    
    /**
     * 上传视频文件
     */
    @Transactional
    public ObservationResponse uploadVideoFile(Long observationId, MultipartFile file, 
                                             String userId, String userType) throws IOException {
        ListeningObservation observation = observationRepository.findById(observationId)
            .orElseThrow(() -> new IllegalArgumentException("听课记录不存在：" + observationId));
        
        // 权限检查
        if ("teacher".equals(userType) && !observation.getObserverId().equals(userId)) {
            throw new IllegalArgumentException("无权限上传视频文件");
        }
        
        // 如果已有视频文件，先删除旧文件
        if (observation.getVideoFilePath() != null) {
            fileStorageService.deleteFile(observation.getVideoFilePath());
        }
        
        // 存储新文件
        FileInfo fileInfo = fileStorageService.storeVideoFile(file);
        observation.setVideoFile(fileInfo);
        
        ListeningObservation savedObservation = observationRepository.save(observation);
        return ObservationResponse.fromEntity(savedObservation);
    }
    
    /**
     * 删除听课记录
     */
    @Transactional
    public void deleteObservation(Long id, String userType) {
        // 只有管理员可以删除听课记录
        if ("teacher".equals(userType)) {
            throw new IllegalArgumentException("无权限删除听课记录");
        }
        
        ListeningObservation observation = observationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("听课记录不存在：" + id));
        
        // 删除相关文件
        if (observation.getEvaluationFilePath() != null) {
            fileStorageService.deleteFile(observation.getEvaluationFilePath());
        }
        if (observation.getVideoFilePath() != null) {
            fileStorageService.deleteFile(observation.getVideoFilePath());
        }
        
        observationRepository.deleteById(id);
    }
    
    /**
     * 获取听课记录统计信息
     */
    public long getTotalObservationCount() {
        return observationRepository.count();
    }
    
    /**
     * 获取某用户的听课记录数量
     */
    public long getUserObservationCount(String userId) {
        return observationRepository.countByObserverId(userId);
    }
    
    /**
     * 获取日期范围内的听课记录数量
     */
    public long getObservationCountByDateRange(LocalDate startDate, LocalDate endDate) {
        return observationRepository.countByClassDateBetween(startDate, endDate);
    }
    
    /**
     * 获取有评价文件的记录数量
     */
    public long getEvaluationFileCount() {
        return observationRepository.countWithEvaluationFile();
    }
    
    /**
     * 获取有视频文件的记录数量
     */
    public long getVideoFileCount() {
        return observationRepository.countWithVideoFile();
    }
    
    /**
     * 按院系统计听课记录数量（基于department_name）
     */
    public List<Object[]> getObservationCountByDepartmentName() {
        return observationRepository.countObservationsByDepartmentName();
    }
} 