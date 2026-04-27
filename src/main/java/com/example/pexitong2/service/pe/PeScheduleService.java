package com.example.pexitong2.service.pe;

import com.example.pexitong2.entity.User;
import com.example.pexitong2.entity.pe.PeSchedule;
import com.example.pexitong2.entity.pe.TempClass;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.repository.pe.PeScheduleRepository;
import com.example.pexitong2.repository.pe.TempClassRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
public class PeScheduleService {

    @Autowired
    private PeScheduleRepository peScheduleRepository;

    @Autowired
    private TempClassRepository tempClassRepository;

    @Autowired
    private UserRepository userRepository;

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

    public List<PeSchedule> getSchedules(String school, String semester) {
        if (semester != null && !semester.isBlank()) {
            return peScheduleRepository.findBySchoolAndSemester(school, semester);
        }
        return peScheduleRepository.findBySchool(school);
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
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val)) {
                return String.valueOf((int) val);
            }
            return String.valueOf(val);
        }
        return null;
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
}
