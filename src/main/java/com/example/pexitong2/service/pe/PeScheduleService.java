package com.example.pexitong2.service.pe;

import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.PeSchedule;
import com.example.pexitong2.entity.pe.TempClass;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.pe.PeScheduleRepository;
import com.example.pexitong2.repository.pe.TempClassRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PeScheduleService {

    @Autowired
    private PeScheduleRepository peScheduleRepository;

    @Autowired
    private TempClassRepository tempClassRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 预检查 Excel 中的老师姓名，返回 users 表中找不到的姓名集合
     */
    public List<String> preCheckTeachers(InputStream inputStream, String school) {
        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    columnIndex.put(cell.getStringCellValue().trim(), i);
                }
            }

            Set<String> uniqueNames = new LinkedHashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String teacherName = getCellStringValue(row, columnIndex.get("老师姓名"));
                if (teacherName != null && !teacherName.isEmpty()) {
                    uniqueNames.add(teacherName);
                }
            }
            workbook.close();

            List<String> unknownTeachers = new ArrayList<>();
            for (String name : uniqueNames) {
                List<User> found = userRepository.findByRealNameAndSchool(name, school);
                if (found.isEmpty()) {
                    unknownTeachers.add(name);
                }
            }
            return unknownTeachers;
        } catch (Exception e) {
            throw new RuntimeException("Excel预检查失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public int importFromExcel(InputStream inputStream, String school, String semester) {
        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    columnIndex.put(cell.getStringCellValue().trim(), i);
                }
            }

            int count = 0;
            Set<String> importedTempClassKeys = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String teacherName = getCellStringValue(row, columnIndex.get("老师姓名"));
                if (teacherName == null || teacherName.isEmpty()) continue;

                int dayOfWeek = (int) getCellNumericValue(row, columnIndex.get("星期"));
                String startTime = getCellStringValue(row, columnIndex.get("开始时间"));
                String endTime = getCellStringValue(row, columnIndex.get("结束时间"));
                String location = getCellStringValue(row, columnIndex.get("地点"));
                String className = getCellStringValue(row, columnIndex.get("班级名称"));
                int capacity = (int) getCellNumericValue(row, columnIndex.get("人数上限"));

                String teacherId = null;
                List<User> teachers = userRepository.findByRealNameAndSchool(teacherName, school);
                if (!teachers.isEmpty()) {
                    teacherId = teachers.get(0).getId();
                }

                if (teacherId != null && !teacherId.isBlank() && className != null && !className.isBlank()) {
                    String tempClassKey = teacherId + "||" + className.trim();
                    boolean exists = importedTempClassKeys.contains(tempClassKey) ||
                            tempClassRepository.existsBySchoolAndSemesterAndTeacherIdAndClassName(
                                    school, semester, teacherId, className.trim());
                    if (exists) {
                        continue;
                    }
                    importedTempClassKeys.add(tempClassKey);
                }

                String scheduleId = UUID.randomUUID().toString();
                PeSchedule schedule = new PeSchedule();
                schedule.setId(scheduleId);
                schedule.setTeacherId(teacherId);
                schedule.setTeacherName(teacherName);
                schedule.setSchool(school);
                schedule.setSemester(semester);
                schedule.setDayOfWeek(dayOfWeek);
                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                schedule.setLocation(location);
                peScheduleRepository.save(schedule);

                TempClass tempClass = new TempClass();
                tempClass.setId(UUID.randomUUID().toString());
                tempClass.setScheduleId(scheduleId);
                tempClass.setTeacherId(teacherId);
                tempClass.setClassName(className);
                tempClass.setSchool(school);
                tempClass.setSemester(semester);
                tempClass.setCapacity(capacity);
                tempClass.setCurrentCount(0);
                tempClass.setDayOfWeek(dayOfWeek);
                tempClass.setStartTime(startTime);
                tempClass.setEndTime(endTime);
                tempClass.setLocation(location);
                tempClassRepository.save(tempClass);

                count++;
            }

            workbook.close();
            return count;
        } catch (Exception e) {
            throw new RuntimeException("Excel导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 接收前端已解析好的老师姓名列表，返回 users 表中找不到的姓名
     */
    public List<String> preCheckTeacherNames(List<String> teacherNames, String school) {
        Set<String> uniqueNames = new LinkedHashSet<>(teacherNames);
        List<String> unknownTeachers = new ArrayList<>();
        for (String name : uniqueNames) {
            if (name == null || name.isBlank()) continue;
            List<User> found = userRepository.findByRealNameAndSchool(name, school);
            if (found.isEmpty()) {
                unknownTeachers.add(name);
            }
        }
        return unknownTeachers;
    }

    /**
     * 接收前端已解析好的行数据列表进行导入（无需解析 Excel）
     */
    @Transactional
    public int importFromRows(List<Map<String, Object>> rows, String school, String semester) {
        int count = 0;
        Set<String> importedTempClassKeys = new HashSet<>();
        for (Map<String, Object> row : rows) {
            String teacherName = Objects.toString(row.get("teacherName"), "").trim();
            if (teacherName.isEmpty()) continue;

            int dayOfWeek = toInt(row.get("dayOfWeek"));
            String startTime = Objects.toString(row.get("startTime"), "").trim();
            String endTime = Objects.toString(row.get("endTime"), "").trim();
            String location = Objects.toString(row.get("location"), "").trim();
            String className = Objects.toString(row.get("className"), "").trim();
            int capacity = toInt(row.get("capacity"));

            String teacherId = null;
            List<User> teachers = userRepository.findByRealNameAndSchool(teacherName, school);
            if (!teachers.isEmpty()) {
                teacherId = teachers.get(0).getId();
            }

            if (teacherId != null && !teacherId.isBlank() && !className.isBlank()) {
                String tempClassKey = teacherId + "||" + className;
                boolean exists = importedTempClassKeys.contains(tempClassKey) ||
                        tempClassRepository.existsBySchoolAndSemesterAndTeacherIdAndClassName(
                                school, semester, teacherId, className);
                if (exists) {
                    continue;
                }
                importedTempClassKeys.add(tempClassKey);
            }

            String scheduleId = UUID.randomUUID().toString();
            PeSchedule schedule = new PeSchedule();
            schedule.setId(scheduleId);
            schedule.setTeacherId(teacherId);
            schedule.setTeacherName(teacherName);
            schedule.setSchool(school);
            schedule.setSemester(semester);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setLocation(location);
            peScheduleRepository.save(schedule);

            TempClass tempClass = new TempClass();
            tempClass.setId(UUID.randomUUID().toString());
            tempClass.setScheduleId(scheduleId);
            tempClass.setTeacherId(teacherId);
            tempClass.setClassName(className);
            tempClass.setSchool(school);
            tempClass.setSemester(semester);
            tempClass.setCapacity(capacity);
            tempClass.setCurrentCount(0);
            tempClass.setDayOfWeek(dayOfWeek);
            tempClass.setStartTime(startTime);
            tempClass.setEndTime(endTime);
            tempClass.setLocation(location);
            tempClassRepository.save(tempClass);

            count++;
        }
        return count;
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return (int) Double.parseDouble(val.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<Map<String, Object>> getScheduleRows(String school, String semester) {
        String baseSql =
                "SELECT s.id AS id, " +
                        "s.teacher_id AS teacherId, " +
                        "s.teacher_name AS teacherName, " +
                        "s.school AS school, " +
                        "s.semester AS semester, " +
                        "s.day_of_week AS dayOfWeek, " +
                        "s.start_time AS startTime, " +
                        "s.end_time AS endTime, " +
                        "s.location AS location, " +
                        "tc.class_name AS className, " +
                        "tc.capacity AS capacity " +
                        "FROM pe_schedules s " +
                        "LEFT JOIN temp_classes tc ON tc.schedule_id = s.id " +
                        "WHERE s.school = ? ";
        List<Map<String, Object>> rows;
        if (semester != null && !semester.isBlank()) {
            rows = jdbcTemplate.queryForList(baseSql + "AND s.semester = ? ORDER BY s.created_at DESC", school, semester);
        } else {
            rows = jdbcTemplate.queryForList(baseSql + "ORDER BY s.created_at DESC", school);
        }
        for (Map<String, Object> row : rows) {
            String normalizedStart = normalizeTimeString(row.get("startTime") != null ? String.valueOf(row.get("startTime")) : null);
            String normalizedEnd = normalizeTimeString(row.get("endTime") != null ? String.valueOf(row.get("endTime")) : null);
            row.put("startTime", normalizedStart);
            row.put("endTime", normalizedEnd);
            if (normalizedStart != null && !normalizedStart.isBlank() && normalizedEnd != null && !normalizedEnd.isBlank()) {
                row.put("time", normalizedStart + "-" + normalizedEnd);
            } else if (normalizedStart != null && !normalizedStart.isBlank()) {
                row.put("time", normalizedStart);
            } else {
                row.put("time", "");
            }
        }
        return rows;
    }

    @Transactional
    public void deleteSchedule(String id) {
        PeSchedule schedule = peScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课表记录不存在"));
        List<TempClass> tempClasses = tempClassRepository.findBySchoolAndSemester(
                schedule.getSchool(), schedule.getSemester());
        for (TempClass tc : tempClasses) {
            if (id.equals(tc.getScheduleId())) {
                tempClassRepository.delete(tc);
            }
        }
        peScheduleRepository.delete(schedule);
    }

    private String getCellStringValue(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.BLANK) {
            return null;
        }
        return new DataFormatter().formatCellValue(cell).trim();
    }

    private double getCellNumericValue(Row row, Integer colIdx) {
        if (colIdx == null) return 0;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private String normalizeTimeString(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        try {
            double excelValue = Double.parseDouble(raw.trim());
            double fraction = excelValue - Math.floor(excelValue);
            int totalSeconds = (int) Math.round(fraction * 24 * 60 * 60);
            if (totalSeconds >= 24 * 60 * 60) {
                totalSeconds = totalSeconds % (24 * 60 * 60);
            }
            return LocalTime.ofSecondOfDay(totalSeconds).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (NumberFormatException ignored) {
            return raw;
        }
    }
}
