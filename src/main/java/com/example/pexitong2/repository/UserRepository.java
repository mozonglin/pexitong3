package com.example.pexitong2.repository;

import com.example.pexitong2.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByStudentId(String studentId);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByUsername(String username);
    
    boolean existsByStudentId(String studentId);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:userType IS NULL OR u.userType = :userType) AND " +
           "(:school IS NULL OR u.school LIKE %:school%) AND " +
           "(:keyword IS NULL OR u.realName LIKE %:keyword% OR u.username LIKE %:keyword%) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFilters(@Param("userType") User.UserType userType,
                                   @Param("school") String school,
                                   @Param("keyword") String keyword,
                                   @Param("status") User.UserStatus status,
                                   Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:userType IS NULL OR u.userType = :userType) AND " +
           "(:school IS NULL OR u.school LIKE %:school%) AND " +
           "(:departmentName IS NULL OR u.departmentName LIKE %:departmentName%) AND " +
           "(:keyword IS NULL OR u.realName LIKE %:keyword% OR u.username LIKE %:keyword%) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFiltersAndDepartment(@Param("userType") User.UserType userType,
                                                @Param("school") String school,
                                                @Param("departmentName") String departmentName,
                                                @Param("keyword") String keyword,
                                                @Param("status") User.UserStatus status,
                                                Pageable pageable);
    
    // 统计某用户类型的用户数量
    long countByUserType(User.UserType userType);
    
    // 统计某用户类型和学校的用户数量
    long countByUserTypeAndSchool(User.UserType userType, String school);
    
    // 统计某用户类型、学校和院系的用户数量
    long countByUserTypeAndSchoolAndDepartmentName(User.UserType userType, String school, String departmentName);
    
    // 新增方法：根据学校和用户类型查找用户
    Optional<User> findBySchoolAndUserType(String school, User.UserType userType);

    // 根据学校和多种用户类型查找用户（用于教师列表查询）
    List<User> findBySchoolAndUserTypeIn(String school, List<User.UserType> userTypes);

    // 统计某学校指定角色集合的用户数量
    long countBySchoolAndUserTypeIn(String school, List<User.UserType> userTypes);

    @Query("SELECT DISTINCT u.school FROM User u WHERE u.school IS NOT NULL AND u.school != '' ORDER BY u.school")
    List<String> findDistinctSchools();

    @Query("SELECT u FROM User u WHERE u.school = :school AND u.userType != 'student' AND u.status = 'active' ORDER BY u.userType, u.realName")
    List<User> findNonStudentUsersBySchool(@Param("school") String school);

    List<User> findByRealNameAndSchool(String realName, String school);
} 