package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import com.example.pexitong2.dto.venue.*;
import com.example.pexitong2.service.VenueService;
import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private JwtUtil jwtUtil;

    // ==================== 场馆管理接口 ====================

    @GetMapping("/venues")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getVenues(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            List<VenueResponse> venues = venueService.getAllVenues(type, status, keyword, userId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", venues));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/venues/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenueById(@PathVariable String id) {
        try {
            VenueResponse venue = venueService.getVenueById(id);
            return ResponseEntity.ok(ApiResponse.success("获取成功", venue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/venues")
    public ResponseEntity<ApiResponse<VenueResponse>> addVenue(
            @Valid @RequestBody VenueRequest req,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            VenueResponse venue = venueService.addVenue(req, userId);
            return ResponseEntity.ok(ApiResponse.success("添加成功", venue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/venues/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(
            @PathVariable String id,
            @RequestBody VenueRequest req,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            VenueResponse venue = venueService.updateVenue(id, req, userId);
            return ResponseEntity.ok(ApiResponse.success("更新成功", venue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/venues/{id}/status")
    public ResponseEntity<ApiResponse<Object>> updateVenueStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            String status = body.get("status");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("状态不能为空"));
            }
            venueService.updateVenueStatus(id, status, userId);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/venues/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteVenue(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            venueService.deleteVenue(id, userId);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 预约管理接口 ====================

    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<List<VenueReservationResponse>>> getReservations(
            @RequestParam(required = false, name = "venue_id") String venueId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "date_from") String dateFrom,
            @RequestParam(required = false, name = "date_to") String dateTo,
            HttpServletRequest httpRequest) {
        try {
            List<VenueReservationResponse> list = venueService.getAllReservations(venueId, status, dateFrom, dateTo);
            return ResponseEntity.ok(ApiResponse.success("获取成功", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reservations/schedule")
    public ResponseEntity<ApiResponse<List<VenueReservationResponse>>> getSchedule(
            @RequestParam(name = "venue_id") String venueId,
            @RequestParam String date) {
        try {
            List<VenueReservationResponse> list = venueService.getReservationsByVenueAndDate(venueId, date);
            return ResponseEntity.ok(ApiResponse.success("获取成功", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reservations/my")
    public ResponseEntity<ApiResponse<List<VenueReservationResponse>>> getMyReservations(
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            List<VenueReservationResponse> list = venueService.getMyReservations(userId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reservations")
    public ResponseEntity<ApiResponse<VenueReservationResponse>> createReservation(
            @Valid @RequestBody VenueReservationRequest req,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            VenueReservationResponse reservation = venueService.createReservation(req, userId);
            return ResponseEntity.ok(ApiResponse.success("预约成功", reservation));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reservations/{id}/approve")
    public ResponseEntity<ApiResponse<Object>> approveReservation(
            @PathVariable String id,
            @RequestBody ReservationApprovalRequest req,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            venueService.approveReservation(id, req, userId);
            return ResponseEntity.ok(ApiResponse.success(req.isApproved() ? "审批通过" : "已拒绝", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelReservation(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            venueService.cancelReservation(id, userId);
            return ResponseEntity.ok(ApiResponse.success("取消成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reservations/{id}/complete")
    public ResponseEntity<ApiResponse<Object>> completeReservation(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserId(httpRequest);
            venueService.completeReservation(id, userId);
            return ResponseEntity.ok(ApiResponse.success("已标记完成", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 辅助方法 ====================

    private String extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("未提供有效的认证令牌");
    }
}
