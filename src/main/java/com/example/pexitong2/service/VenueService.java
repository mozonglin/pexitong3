package com.example.pexitong2.service;

import com.example.pexitong2.dto.venue.*;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.Venue;
import com.example.pexitong2.entity.VenueReservation;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.VenueRepository;
import com.example.pexitong2.repository.VenueReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VenueService {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private VenueReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== 权限校验 ====================

    private void validateAdminPermission(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        User.UserType type = user.getUserType();
        if (type != User.UserType.department_admin
                && type != User.UserType.school_admin
                && type != User.UserType.super_admin) {
            throw new RuntimeException("权限不足，只有院级及以上管理员可以管理场馆");
        }
    }

    private String getUserSchool(String userId) {
        return userRepository.findById(userId)
                .map(User::getSchool)
                .orElse(null);
    }

    // ==================== 场馆管理 ====================

    public List<VenueResponse> getAllVenues(String type, String status, String keyword, String userId) {
        String school = getUserSchool(userId);
        if (school == null) return List.of();
        return venueRepository.findBySchoolWithFilters(school, type, status, keyword)
                .stream()
                .map(VenueResponse::new)
                .collect(Collectors.toList());
    }

    public VenueResponse getVenueById(String id) {
        Venue venue = venueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("场馆不存在"));
        return new VenueResponse(venue);
    }

    @Transactional
    public VenueResponse addVenue(VenueRequest req, String userId) {
        validateAdminPermission(userId);
        Venue venue = new Venue();
        venue.setName(req.getName());
        venue.setType(req.getType());
        venue.setCapacity(req.getCapacity());
        venue.setLocation(req.getLocation());
        venue.setPrice(req.getPrice() != null ? req.getPrice() : BigDecimal.ZERO);
        venue.setOpenTime(req.getOpenTime());
        venue.setDescription(req.getDescription());
        venue.setStatus("available");
        venue.setSchool(getUserSchool(userId));
        venue.setCreatedBy(userId);
        return new VenueResponse(venueRepository.save(venue));
    }

    @Transactional
    public VenueResponse updateVenue(String id, VenueRequest req, String userId) {
        validateAdminPermission(userId);
        Venue venue = venueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("场馆不存在"));
        if (req.getName() != null) venue.setName(req.getName());
        if (req.getType() != null) venue.setType(req.getType());
        if (req.getCapacity() != null) venue.setCapacity(req.getCapacity());
        if (req.getLocation() != null) venue.setLocation(req.getLocation());
        if (req.getPrice() != null) venue.setPrice(req.getPrice());
        if (req.getOpenTime() != null) venue.setOpenTime(req.getOpenTime());
        if (req.getDescription() != null) venue.setDescription(req.getDescription());
        return new VenueResponse(venueRepository.save(venue));
    }

    @Transactional
    public void updateVenueStatus(String id, String status, String userId) {
        validateAdminPermission(userId);
        Venue venue = venueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("场馆不存在"));
        venue.setStatus(status);
        venueRepository.save(venue);
    }

    @Transactional
    public void deleteVenue(String id, String userId) {
        validateAdminPermission(userId);
        Venue venue = venueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("场馆不存在"));
        venue.setIsDeleted(true);
        venueRepository.save(venue);
    }

    // ==================== 预约管理 ====================

    public List<VenueReservationResponse> getReservationsByVenueAndDate(String venueId, String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        List<VenueReservation> reservations = reservationRepository
                .findByVenueIdAndReservationDateOrderByStartTimeAsc(venueId, localDate);
        return enrichReservations(reservations);
    }

    public List<VenueReservationResponse> getAllReservations(String venueId, String status,
                                                              String dateFrom, String dateTo) {
        LocalDate from = dateFrom != null ? LocalDate.parse(dateFrom) : null;
        LocalDate to = dateTo != null ? LocalDate.parse(dateTo) : null;
        List<VenueReservation> reservations = reservationRepository.findWithFilters(venueId, status, from, to);
        return enrichReservations(reservations);
    }

    public List<VenueReservationResponse> getMyReservations(String userId) {
        List<VenueReservation> reservations = reservationRepository
                .findByBookerIdOrderByReservationDateDescCreatedAtDesc(userId);
        return enrichReservations(reservations);
    }

    @Transactional
    public VenueReservationResponse createReservation(VenueReservationRequest req, String userId) {
        Venue venue = venueRepository.findByIdAndIsDeletedFalse(req.getVenueId())
                .orElseThrow(() -> new RuntimeException("场馆不存在"));
        if (!"available".equals(venue.getStatus())) {
            throw new RuntimeException("该场馆当前不可预约，状态: " + venue.getStatus());
        }

        LocalDate date = LocalDate.parse(req.getReservationDate());
        LocalTime start = LocalTime.parse(req.getStartTime());
        LocalTime end = LocalTime.parse(req.getEndTime());

        if (!end.isAfter(start)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("不能预约过去的日期");
        }
        if (reservationRepository.existsConflict(req.getVenueId(), date, start, end)) {
            throw new RuntimeException("该时间段已有预约，请选择其他时间");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        VenueReservation reservation = new VenueReservation();
        reservation.setVenueId(req.getVenueId());
        reservation.setBookerId(userId);
        reservation.setBookerName(booker.getRealName());
        reservation.setBookerPhone(req.getBookerPhone() != null ? req.getBookerPhone() : booker.getPhone());
        reservation.setPurpose(req.getPurpose());
        reservation.setReservationDate(date);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setPeopleCount(req.getPeopleCount() != null ? req.getPeopleCount() : 1);
        reservation.setRemark(req.getRemark());
        reservation.setStatus("pending");

        VenueReservation saved = reservationRepository.save(reservation);
        VenueReservationResponse response = new VenueReservationResponse(saved);
        response.setVenueName(venue.getName());
        response.setVenueType(venue.getType());
        response.setVenueLocation(venue.getLocation());
        return response;
    }

    @Transactional
    public void approveReservation(String id, ReservationApprovalRequest req, String adminId) {
        validateAdminPermission(adminId);
        VenueReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));
        if (!"pending".equals(reservation.getStatus())) {
            throw new RuntimeException("该预约已处理，当前状态: " + reservation.getStatus());
        }

        if (req.isApproved()) {
            if (reservationRepository.existsConflictExcluding(
                    reservation.getVenueId(), reservation.getReservationDate(),
                    reservation.getStartTime(), reservation.getEndTime(), id)) {
                throw new RuntimeException("该时间段已有批准的预约，无法再次批准");
            }
            reservation.setStatus("approved");
        } else {
            if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
                throw new RuntimeException("拒绝时必须填写原因");
            }
            reservation.setStatus("rejected");
            reservation.setRejectReason(req.getRejectReason());
        }
        reservation.setApprovedBy(adminId);
        reservation.setApprovedAt(LocalDateTime.now());
        if (req.getRemark() != null) reservation.setRemark(req.getRemark());
        reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(String id, String userId) {
        VenueReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));

        boolean isAdmin = isAdminUser(userId);
        boolean isOwner = reservation.getBookerId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("无权取消该预约");
        }
        if ("completed".equals(reservation.getStatus()) || "cancelled".equals(reservation.getStatus())) {
            throw new RuntimeException("该预约已完成或已取消");
        }
        reservation.setStatus("cancelled");
        reservationRepository.save(reservation);
    }

    @Transactional
    public void completeReservation(String id, String adminId) {
        validateAdminPermission(adminId);
        VenueReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));
        if (!"approved".equals(reservation.getStatus())) {
            throw new RuntimeException("只有已批准的预约才能标记完成");
        }
        reservation.setStatus("completed");
        reservationRepository.save(reservation);
    }

    // ==================== 辅助方法 ====================

    private boolean isAdminUser(String userId) {
        return userRepository.findById(userId)
                .map(u -> u.getUserType() == User.UserType.department_admin
                        || u.getUserType() == User.UserType.school_admin
                        || u.getUserType() == User.UserType.super_admin)
                .orElse(false);
    }

    private List<VenueReservationResponse> enrichReservations(List<VenueReservation> reservations) {
        return reservations.stream().map(r -> {
            VenueReservationResponse resp = new VenueReservationResponse(r);
            venueRepository.findById(r.getVenueId()).ifPresent(v -> {
                resp.setVenueName(v.getName());
                resp.setVenueType(v.getType());
                resp.setVenueLocation(v.getLocation());
            });
            userRepository.findById(r.getApprovedBy() != null ? r.getApprovedBy() : "")
                    .ifPresent(u -> resp.setApprovedByName(u.getRealName()));
            return resp;
        }).collect(Collectors.toList());
    }
}
