package com.example.pexitong2.repository.pe;

import com.example.pexitong2.entity.pe.PeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PeUserRepository extends JpaRepository<PeUser, String> {
    
    Optional<PeUser> findByStudentId(String studentId);
    
    Optional<PeUser> findByPhoneNumber(String phoneNumber);
    
    boolean existsByStudentId(String studentId);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    @Query("SELECT u FROM PeUser u WHERE " +
           "(:search IS NULL OR u.name LIKE %:search% OR u.studentId LIKE %:search%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:school IS NULL OR u.school LIKE %:school%) AND " +
           "(:college IS NULL OR u.college LIKE %:college%)")
    Page<PeUser> findUsersWithFilters(@Param("search") String search,
                                     @Param("role") PeUser.Role role,
                                     @Param("school") String school,
                                     @Param("college") String college,
                                     Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM PeUser u WHERE " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    long countBySchoolAndCollege(@Param("school") String school, @Param("college") String college);
    
    @Query("SELECT COUNT(u) FROM PeUser u WHERE u.role = :role AND " +
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college)")
    long countByRoleAndSchoolAndCollege(@Param("role") PeUser.Role role, 
                                       @Param("school") String school, 
                                       @Param("college") String college);
    
    // 新增方法：按学校查询学生
    List<PeUser> findBySchool(String school);
    
    // 新增方法：按学校和院系查询学生
    List<PeUser> findBySchoolAndCollege(String school, String college);
    
    // 新增方法：按学校、院系和班级查询学生
    List<PeUser> findBySchoolAndCollegeAndClassName(String school, String college, String className);
}
