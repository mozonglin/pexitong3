package com.example.pexitong2.service;

import com.example.pexitong2.dto.CourseSearchResponse;
import com.example.pexitong2.entity.Course;
import com.example.pexitong2.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    /**
     * 搜索课程（根据关键词和日期），限制在指定学校内
     */
    public List<CourseSearchResponse> searchCourses(String keyword, LocalDate date, String school) {
        List<Course> courses = courseRepository.searchCourses(keyword, date, school);
        return courses.stream()
                .map(CourseSearchResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页搜索课程，限制在指定学校内
     */
    public Page<CourseSearchResponse> searchCoursesWithPage(String keyword, 
                                                           LocalDate startDate, LocalDate endDate, 
                                                           String school,
                                                           Pageable pageable) {
        Page<Course> courses = courseRepository.searchCoursesWithPage(
            keyword, startDate, endDate, school, pageable);
        return courses.map(CourseSearchResponse::fromEntity);
    }
    
    /**
     * 根据教师姓名和日期搜索课程，限制在指定学校内
     */
    public List<CourseSearchResponse> searchCoursesByTeacherAndDate(String teacherName, LocalDate date, String school) {
        List<Course> courses = courseRepository.findByTeacherNameContainingAndClassDate(teacherName, date, school);
        return courses.stream()
                .map(CourseSearchResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取课程
     */
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    /**
     * 根据教师ID获取课程
     */
    public List<CourseSearchResponse> getCoursesByTeacher(String teacherId) {
        List<Course> courses = courseRepository.findByTeacherId(teacherId);
        return courses.stream()
                .map(CourseSearchResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据日期获取课程，限制在指定学校内
     */
    public List<CourseSearchResponse> getCoursesByDate(LocalDate date, String school) {
        List<Course> courses = courseRepository.findByClassDateAndSchool(date, school);
        return courses.stream()
                .map(CourseSearchResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据日期范围获取课程
     */
    public List<CourseSearchResponse> getCoursesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Course> courses = courseRepository.findByClassDateBetween(startDate, endDate);
        return courses.stream()
                .map(CourseSearchResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建课程
     */
    public Course createCourse(String courseName, String teacherId, String teacherName, 
                              String className, String classroom, String classTime, 
                              LocalDate classDate) {
        Course course = new Course(courseName, teacherId, teacherName, 
                                  className, classroom, classTime, classDate);
        return courseRepository.save(course);
    }
    
    /**
     * 更新课程
     */
    public Course updateCourse(Long id, String courseName, String teacherId, String teacherName, 
                              String className, String classroom, String classTime, 
                              LocalDate classDate) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在：" + id));
        
        course.setCourseName(courseName);
        course.setTeacherId(teacherId);
        course.setTeacherName(teacherName);
        course.setClassName(className);
        course.setClassroom(classroom);
        course.setClassTime(classTime);
        course.setClassDate(classDate);
        
        return courseRepository.save(course);
    }
    
    /**
     * 删除课程
     */
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("课程不存在：" + id);
        }
        courseRepository.deleteById(id);
    }
    
    /**
     * 统计某教师的课程数量
     */
    public long countCoursesByTeacher(String teacherId) {
        return courseRepository.countByTeacherId(teacherId);
    }
    
    // 删除：统计某院系的课程数量方法（不再需要）
    
    /**
     * 检查课程是否存在
     */
    public boolean courseExists(Long id) {
        return courseRepository.existsById(id);
    }
} 