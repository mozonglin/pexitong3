package com.example.pexitong2.repository.teaching;

import com.example.pexitong2.entity.teaching.TeacherAttendancePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherAttendancePhotoRepository extends JpaRepository<TeacherAttendancePhoto, Long> {
    
    /**
     * 根据签到记录ID查找照片
     */
    List<TeacherAttendancePhoto> findByAttendanceRecordIdOrderByUploadTimeAsc(Long attendanceRecordId);
    
    /**
     * 根据课程ID查找照片
     */
    List<TeacherAttendancePhoto> findByCourseIdOrderByUploadTimeAsc(Long courseId);
    
    /**
     * 根据教师ID查找照片
     */
    List<TeacherAttendancePhoto> findByTeacherIdOrderByUploadTimeDesc(String teacherId);
    
    /**
     * 根据文件名查找照片
     */
    TeacherAttendancePhoto findByFileName(String fileName);
    
    /**
     * 查询指定课程的第一张照片
     */
    @Query("SELECT tap FROM TeacherAttendancePhoto tap " +
           "WHERE tap.courseId = :courseId " +
           "ORDER BY tap.uploadTime ASC LIMIT 1")
    TeacherAttendancePhoto findFirstPhotoByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 统计指定签到记录的照片数量
     */
    long countByAttendanceRecordId(Long attendanceRecordId);
    
    /**
     * 统计指定课程的照片数量
     */
    long countByCourseId(Long courseId);
}




