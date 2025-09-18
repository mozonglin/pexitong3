package com.example.pexitong2.entity.teaching;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "teacher_attendance_records")
public class TeacherAttendanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "teacher_id", nullable = false, length = 50)
    private String teacherId;
    
    @Column(name = "teacher_name", nullable = false, length = 50)
    private String teacherName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.pending;
    
    @Column(name = "attendance_time")
    private LocalDateTime attendanceTime;
    
    @Column(name = "attendance_location", length = 200)
    private String attendanceLocation;
    
    @Column(name = "attendance_note", columnDefinition = "TEXT")
    private String attendanceNote;
    
    // 课程信息（冗余存储）
    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;
    
    @Column(name = "class_name", nullable = false, length = 50)
    private String className;
    
    @Column(name = "classroom", length = 100)
    private String classroom;
    
    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联照片
    @OneToMany(mappedBy = "attendanceRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherAttendancePhoto> photos;
    
    // 枚举类型
    public enum AttendanceStatus {
        completed("已签到"),
        pending("未签到"), 
        late("迟到"),
        missed("缺勤");
        
        private final String description;
        
        AttendanceStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public TeacherAttendanceRecord() {}
    
    public TeacherAttendanceRecord(Long courseId, String teacherId, String teacherName,
                                   String courseName, String className, String classroom,
                                   LocalDate classDate, LocalTime startTime, LocalTime endTime) {
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.courseName = courseName;
        this.className = className;
        this.classroom = classroom;
        this.classDate = classDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attendanceStatus = AttendanceStatus.pending;
    }
    
    // Getter 和 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }
    
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
    
    public LocalDateTime getAttendanceTime() {
        return attendanceTime;
    }
    
    public void setAttendanceTime(LocalDateTime attendanceTime) {
        this.attendanceTime = attendanceTime;
    }
    
    public String getAttendanceLocation() {
        return attendanceLocation;
    }
    
    public void setAttendanceLocation(String attendanceLocation) {
        this.attendanceLocation = attendanceLocation;
    }
    
    public String getAttendanceNote() {
        return attendanceNote;
    }
    
    public void setAttendanceNote(String attendanceNote) {
        this.attendanceNote = attendanceNote;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getClassroom() {
        return classroom;
    }
    
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
    
    public LocalDate getClassDate() {
        return classDate;
    }
    
    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<TeacherAttendancePhoto> getPhotos() {
        return photos;
    }
    
    public void setPhotos(List<TeacherAttendancePhoto> photos) {
        this.photos = photos;
    }
}
