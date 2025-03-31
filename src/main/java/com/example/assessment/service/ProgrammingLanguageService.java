package com.example.assessment.service;


import com.example.assessment.model.ProgrammingLanguage;
import com.example.assessment.repository.ProgrammingLanguageRepository;
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
public class ProgrammingLanguageService {

    @Autowired
    private ProgrammingLanguageRepository programmingLanguageRepository;

    public List<ProgrammingLanguage> getAllProgrammingLanguages() {
        return programmingLanguageRepository.findAll();
    }

    public ProgrammingLanguage saveProgrammingLanguage(ProgrammingLanguage programmingLanguage) {
        return programmingLanguageRepository.save(programmingLanguage);
    }


    public ProgrammingLanguage getProgrammingLanguageById(Integer id) {
        return programmingLanguageRepository.findById(id).orElse(null);
    }

    public ProgrammingLanguage createProgrammingLanguage(String programmingLanguageName) {
        ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
        programmingLanguage.setLanguage(programmingLanguageName);
        return programmingLanguageRepository.save(programmingLanguage);
    }

    public ProgrammingLanguage updateProgrammingLanguage(Integer id, String programmingLanguageName) {
        Optional<ProgrammingLanguage> existingProgrammingLanguage = programmingLanguageRepository.findById(id);
        if (existingProgrammingLanguage.isPresent()) {
            ProgrammingLanguage programmingLanguage = existingProgrammingLanguage.get();
            programmingLanguage.setLanguage(programmingLanguageName);
            return programmingLanguageRepository.save(programmingLanguage);
        }
        return null;
    }

    public void deleteProgrammingLanguage(Integer id) {
        programmingLanguageRepository.deleteById(id);
    }

    public void saveAll(List<ProgrammingLanguage> programmingLanguages) {
        programmingLanguageRepository.saveAll(programmingLanguages);  // Save all programmingLanguages at once
    }
    // Method to fetch paginated programmingLanguages
    public Page<ProgrammingLanguage> getProgrammingLanguages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return programmingLanguageRepository.findAll(pageable);
    }
    public Page<ProgrammingLanguage> searchProgrammingLanguages(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return programmingLanguageRepository.findByLanguageContainingIgnoreCase(searchQuery, pageable);
    }

    // Import programmingLanguages from an Excel file
    public List<ProgrammingLanguage> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<ProgrammingLanguage> programmingLanguages = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    String programmingLanguageName = row.getCell(1).getStringCellValue(); // Assume programmingLanguage name is in column 01

                    Cell cell = row.getCell(1); // Assume programmingLanguage name is in column 01

                    if (cell != null) {
                        // Check if the programmingLanguage already exists
                        if (!programmingLanguageExists(programmingLanguageName)) {
                            ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();

                            programmingLanguage.setLanguage(programmingLanguageName);
                            programmingLanguages.add(programmingLanguage);
                        }
                    }
                }
            }

            // Save programmingLanguages to the database
            return programmingLanguageRepository.saveAll(programmingLanguages); // Assuming you have a programmingLanguageRepository bean
        } catch (IOException e) {
            throw new RuntimeException("Error importing programmingLanguages from Excel", e);
        }
    }

    private boolean programmingLanguageExists(String programmingLanguageName) {
        return programmingLanguageRepository.findByLanguage(programmingLanguageName).isPresent(); // Assuming you have this method in ProgrammingLanguageRepository
    }

    // Method to export programmingLanguages to Excel
    public ByteArrayInputStream exportProgrammingLanguagesToExcel(List<ProgrammingLanguage> programmingLanguages) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("ProgrammingLanguages");

        // Create the header row
        String[] headers = { "ProgrammingLanguage ID", "ProgrammingLanguage",  };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);


        // Populate data rows
        int rowNum = 1;
        for (ProgrammingLanguage programmingLanguage : programmingLanguages) {
            sheet.createRow(rowNum).createCell(0).setCellValue(programmingLanguage.getId());
            sheet.getRow(rowNum).createCell(1).setCellValue(programmingLanguage.getLanguage());

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

    public boolean isProgrammingLanguageNameExists(String programmingLanguageName) {
        return this.programmingLanguageExists(programmingLanguageName);
    }

    public List<ProgrammingLanguage> findAll() {
        return programmingLanguageRepository.findAll();
    }
}
