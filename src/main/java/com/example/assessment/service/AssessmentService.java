package com.example.assessment.service;

import com.example.assessment.model.Assessment;
import com.example.assessment.repository.AssessmentRepository;
import com.example.course.Course;
import com.example.course.CourseService;
import com.example.user.User;
import com.example.user.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.utils.Helper.getCellValueAsString;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    public Optional<Assessment> findById(Long id) {
        return assessmentRepository.findById(id);
    }

    public Assessment save(Assessment assessment) {
        return assessmentRepository.save(assessment);
    }

    public void deleteById(Long id) {
        assessmentRepository.deleteById(id);
    }

    public Page<Assessment> findAll(Pageable pageable) {
        return assessmentRepository.findAll(pageable);
    }

    public List<Assessment> findAll() {
        return assessmentRepository.findAll();
    }

    public Page<Assessment> search(String searchQuery, Pageable pageable) {
        return assessmentRepository.search(searchQuery, pageable);
    }

    public boolean existsByTitle(String title) {
        return assessmentRepository.existsByTitle(title);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    public void importExcel(MultipartFile file) {
        User currentUser = getCurrentUser();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Assessment> assessments = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Extract cells
                    Cell titleCell = row.getCell(0);
                    Cell descriptionCell = row.getCell(1);
                    Cell courseCell = row.getCell(2);
                    Cell createdAtCell = row.getCell(3);

                    if (titleCell != null) {
                        String title = getCellValueAsString(titleCell).trim();
                        String description = descriptionCell != null ? getCellValueAsString(descriptionCell).trim() : null;

                        Course course = null;
                        if (courseCell != null) {
                            String courseName = getCellValueAsString(courseCell).trim();
                            course = courseService.findByName(courseName);
                        }

                        LocalDateTime createdAt = createdAtCell != null
                                ? LocalDateTime.parse(getCellValueAsString(createdAtCell).trim())
                                : LocalDateTime.now();

                        if (!assessmentRepository.existsByTitle(title)) {
                            Assessment assessment = new Assessment();
                            assessment.setTitle(title);
                            assessment.setCourse(course);
                            assessment.setCreatedAt(createdAt);
                            assessment.setCreatedBy(currentUser);
                            assessments.add(assessment);
                        }
                    }
                }
            }

            for (Assessment assessment : assessments) {
                assessmentRepository.save(assessment);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error importing assessments from Excel", e);
        }
    }

    public ByteArrayInputStream exportToExcel(List<Assessment> assessments) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Assessments");

        // Header row
        String[] headers = {"ID", "Title", "Description", "Course", "Created At", "Created By"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Data rows
        int rowNum = 1;
        for (Assessment assessment : assessments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(assessment.getId());
            row.createCell(1).setCellValue(assessment.getTitle());
            row.createCell(3).setCellValue(assessment.getCourse() != null ? assessment.getCourse().getName() : "");
            row.createCell(4).setCellValue(assessment.getCreatedAt().toString());
            row.createCell(5).setCellValue(assessment.getCreatedBy() != null ? assessment.getCreatedBy().getUsername() : "");
        }

        // Write to output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting assessments to Excel", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
