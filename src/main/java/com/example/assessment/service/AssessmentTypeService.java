package com.example.assessment.service;

import com.example.assessment.model.AssessmentType;
import com.example.assessment.repository.AssessmentTypeRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AssessmentTypeService {

    @Autowired
    private AssessmentTypeRepository assessmentTypeRepository;

    public List<AssessmentType> getAllAssessmentTypes() {
        return assessmentTypeRepository.findAll();
    }

    public AssessmentType getAssessmentTypeById(Integer id) {
        return assessmentTypeRepository.findById(id).orElse(null);
    }

    public AssessmentType createAssessmentType(String assessmentTypeName) {
        AssessmentType assessmentType = new AssessmentType();
        assessmentType.setName(assessmentTypeName);
        return assessmentTypeRepository.save(assessmentType);
    }

    public AssessmentType updateAssessmentType(Integer id, String assessmentTypeName) {
        Optional<AssessmentType> existingAssessmentType = assessmentTypeRepository.findById(id);
        if (existingAssessmentType.isPresent()) {
            AssessmentType assessmentType = existingAssessmentType.get();
            assessmentType.setName(assessmentTypeName);
            return assessmentTypeRepository.save(assessmentType);
        }
        return null;
    }

    public void deleteAssessmentType(Integer id) {
        assessmentTypeRepository.deleteById(id);
    }

    public void saveAll(List<AssessmentType> assessmentTypes) {
        assessmentTypeRepository.saveAll(assessmentTypes);  // Save all assessmentTypes at once
    }
    // Method to fetch paginated assessmentTypes
    public Page<AssessmentType> getAssessmentTypes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return assessmentTypeRepository.findAll(pageable);
    }
    public Page<AssessmentType> searchAssessmentTypes(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return assessmentTypeRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
    }

    // Import assessmentTypes from an Excel file
    public List<AssessmentType> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<AssessmentType> assessmentTypes = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    String assessmentTypeName = row.getCell(1).getStringCellValue(); // Assume assessmentType name is in column 0
                    Cell cell = row.getCell(1); // Assume assessmentType name is in column 0
                    if (cell != null) {
                        // Check if the assessmentType already exists
                        if (!assessmentTypeExists(assessmentTypeName)) {
                            AssessmentType assessmentType = new AssessmentType();
                            assessmentType.setName(assessmentTypeName);
                            assessmentTypes.add(assessmentType);
                        }
                    }
                }
            }

            // Save assessmentTypes to the database
            return assessmentTypeRepository.saveAll(assessmentTypes); // Assuming you have a assessmentTypeRepository bean
        } catch (IOException e) {
            throw new RuntimeException("Error importing assessmentTypes from Excel", e);
        }
    }

    private boolean assessmentTypeExists(String assessmentTypeName) {
        return assessmentTypeRepository.findByName(assessmentTypeName).isPresent(); // Assuming you have this method in AssessmentTypeRepository
    }

    // Method to export assessmentTypes to Excel
    public ByteArrayInputStream exportAssessmentTypesToExcel(List<AssessmentType> assessmentTypes) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("AssessmentTypes");

        // Create the header row
        String[] headers = { "AssessmentType ID", "AssessmentType Name" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);

        // Populate data rows
        int rowNum = 1;
        for (AssessmentType assessmentType : assessmentTypes) {
            sheet.createRow(rowNum).createCell(0).setCellValue(assessmentType.getId());
            sheet.getRow(rowNum).createCell(1).setCellValue(assessmentType.getName());
            rowNum++;
        }

        // Write to ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public boolean isAssessmentTypeNameExists(String assessmentTypeName) {
        return this.assessmentTypeExists(assessmentTypeName);
    }
}
