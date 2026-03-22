package com.example.pexitong2.repository;

import com.example.pexitong2.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, String> {

    List<Venue> findByIsDeletedFalse();

    List<Venue> findByTypeAndIsDeletedFalse(String type);

    List<Venue> findByStatusAndIsDeletedFalse(String status);

    @Query("SELECT v FROM Venue v WHERE v.isDeleted = false AND " +
           "(:type IS NULL OR v.type = :type) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:keyword IS NULL OR v.name LIKE %:keyword%)")
    List<Venue> findWithFilters(@Param("type") String type,
                                @Param("status") String status,
                                @Param("keyword") String keyword);

    Optional<Venue> findByIdAndIsDeletedFalse(String id);

    List<Venue> findBySchoolAndIsDeletedFalse(String school);

    @Query("SELECT v FROM Venue v WHERE v.isDeleted = false AND v.school = :school AND " +
           "(:type IS NULL OR v.type = :type) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:keyword IS NULL OR v.name LIKE %:keyword%)")
    List<Venue> findBySchoolWithFilters(@Param("school") String school,
                                        @Param("type") String type,
                                        @Param("status") String status,
                                        @Param("keyword") String keyword);
}
