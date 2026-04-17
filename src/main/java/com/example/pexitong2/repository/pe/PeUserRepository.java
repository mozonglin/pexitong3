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
           "(:school IS NULL OR u.school = :school) AND " +
           "(:college IS NULL OR u.college = :college) AND " +
           "(:className IS NULL OR u.className = :className)")
    Page<PeUser> findUsersWithFilters(@Param("search") String search,
                                     @Param("role") PeUser.Role role,
                                     @Param("school") String school,
                                     @Param("college") String college,
                                     @Param("className") String className,
                                     Pageable pageable);

    /** 聚合查询：按学校分组统计人数（原生SQL） */
    @Query(value = "SELECT school AS name, COUNT(*) AS cnt FROM users1 " +
           "WHERE school IS NOT NULL AND school != '' GROUP BY school ORDER BY cnt DESC",
           nativeQuery = true)
    List<Object[]> countGroupBySchool();

    /** 聚合查询：按学院分组统计人数（原生SQL，指定学校） */
    @Query(value = "SELECT college AS name, COUNT(*) AS cnt, " +
           "SUM(CASE WHEN role = 'CHECKER' THEN 1 ELSE 0 END) AS checkerCnt, " +
           "SUM(CASE WHEN role = 'SUB_CHECKER' THEN 1 ELSE 0 END) AS subCheckerCnt " +
           "FROM users1 WHERE school = :school AND college IS NOT NULL AND college != '' " +
           "GROUP BY college ORDER BY cnt DESC",
           nativeQuery = true)
    List<Object[]> countGroupByCollege(@Param("school") String school);

    /** 聚合查询：按班级分组统计人数（原生SQL，指定学校+学院） */
    @Query(value = "SELECT class_name AS name, COUNT(*) AS cnt, " +
           "SUM(CASE WHEN role = 'CHECKER' THEN 1 ELSE 0 END) AS checkerCnt, " +
           "SUM(CASE WHEN role = 'SUB_CHECKER' THEN 1 ELSE 0 END) AS subCheckerCnt " +
           "FROM users1 WHERE school = :school AND college = :college " +
           "GROUP BY class_name ORDER BY class_name",
           nativeQuery = true)
    List<Object[]> countGroupByClass(@Param("school") String school, @Param("college") String college);

    /** 聚合查询：指定学校下有多少个学院（原生SQL） */
    @Query(value = "SELECT COUNT(DISTINCT college) FROM users1 WHERE school = :school AND college IS NOT NULL AND college != ''",
           nativeQuery = true)
    long countDistinctCollegeBySchool(@Param("school") String school);
    
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

    /** 按学校与 PE 角色查询（统计口径一般仅学生） */
    List<PeUser> findBySchoolAndRole(String school, PeUser.Role role);
    
    // 新增方法：按学校和院系查询学生
    List<PeUser> findBySchoolAndCollege(String school, String college);
    
    // 新增方法：按学校、院系和班级查询学生
    List<PeUser> findBySchoolAndCollegeAndClassName(String school, String college, String className);
}
