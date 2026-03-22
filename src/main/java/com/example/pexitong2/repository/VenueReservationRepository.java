package com.example.pexitong2.repository;

import com.example.pexitong2.entity.VenueReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface VenueReservationRepository extends JpaRepository<VenueReservation, String> {

    List<VenueReservation> findByVenueIdAndReservationDateOrderByStartTimeAsc(String venueId, LocalDate date);

    List<VenueReservation> findByBookerIdOrderByReservationDateDescCreatedAtDesc(String bookerId);

    @Query("SELECT r FROM VenueReservation r WHERE " +
           "(:venueId IS NULL OR r.venueId = :venueId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:dateFrom IS NULL OR r.reservationDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR r.reservationDate <= :dateTo) " +
           "ORDER BY r.reservationDate DESC, r.startTime ASC")
    List<VenueReservation> findWithFilters(@Param("venueId") String venueId,
                                           @Param("status") String status,
                                           @Param("dateFrom") LocalDate dateFrom,
                                           @Param("dateTo") LocalDate dateTo);

    @Query("SELECT COUNT(r) > 0 FROM VenueReservation r WHERE " +
           "r.venueId = :venueId AND r.reservationDate = :date AND " +
           "r.status IN ('pending', 'approved') AND " +
           "r.startTime < :endTime AND r.endTime > :startTime")
    boolean existsConflict(@Param("venueId") String venueId,
                           @Param("date") LocalDate date,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(r) > 0 FROM VenueReservation r WHERE " +
           "r.venueId = :venueId AND r.reservationDate = :date AND " +
           "r.status IN ('pending', 'approved') AND " +
           "r.startTime < :endTime AND r.endTime > :startTime AND " +
           "r.id <> :excludeId")
    boolean existsConflictExcluding(@Param("venueId") String venueId,
                                    @Param("date") LocalDate date,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime,
                                    @Param("excludeId") String excludeId);
}
