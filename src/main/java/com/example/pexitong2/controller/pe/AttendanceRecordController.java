package com.example.pexitong2.controller.pe;

import com.example.pexitong2.dto.pe.*;
import com.example.pexitong2.service.pe.AttendanceRecordService;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 签到记录管理控制器
 */
@RestController
@RequestMapping("/pe/attendance-records")
@CrossOrigin(origins = "*")
public class AttendanceRecordController {
    
    @Autowired
    private AttendanceRecordService attendanceRecordService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取签到记录列表
     */
    @GetMapping
    public PeApiResponse<PageResponse<AttendanceRecordResponse>> getAttendanceRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String activityId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean isCheckedOut,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            PageResponse<AttendanceRecordResponse> records = attendanceRecordService.getAttendanceRecords(
                page, pageSize, activityId, studentId, studentName, startDate, endDate,
                isCheckedOut, minDuration, maxDuration, currentUserId);
            
            return PeApiResponse.success("获取成功", records);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个签到记录详情
     */
    @GetMapping("/{id}")
    public PeApiResponse<AttendanceRecordResponse> getAttendanceRecordById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            AttendanceRecordResponse record = attendanceRecordService.getAttendanceRecordById(id, currentUserId);
            
            return PeApiResponse.success("获取成功", record);
            
        } catch (Exception e) {
            return PeApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 导出签到记录
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAttendanceRecords(
            @RequestParam(required = false) String activityId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean isCheckedOut,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestHeader("Authorization") String token) {
        
        try {
            String currentUserId = getCurrentUserId(token);
            
            byte[] excelData = attendanceRecordService.exportAttendanceRecords(
                activityId, studentId, studentName, startDate, endDate,
                isCheckedOut, minDuration, maxDuration, currentUserId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "attendance_records.xlsx");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
            
        } catch (Exception e) {
            // 对于导出接口，返回错误信息作为文本
            return ResponseEntity.badRequest()
                .body(e.getMessage().getBytes());
        }
    }
    
    /**
     * 从Token中获取当前用户ID
     */
    private String getCurrentUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtUtil.extractUserId(token);
    }
}
