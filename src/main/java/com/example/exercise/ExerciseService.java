package com.example.exercise;

import com.example.module.Module;
import com.example.module.ModuleRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.utils.Helper.getCellValueAsString;

@Service
public class ExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;


    public Optional<Exercise> getExerciseById(Long id) {
        return exerciseRepository.findById(id);
    }

    public List<Exercise> findAllExercises() {
        return exerciseRepository.findAll();
    }

    public Exercise saveExercise(Exercise exercise) {

        return exerciseRepository.save(exercise);
    }

    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }

    public Page<Exercise> getAllExercises(Pageable pageable) {
        return exerciseRepository.findAll(pageable); // Fetch all exercises with pagination
    }

    public Page<Exercise> searchExercises(String searchQuery, Pageable pageable) {
        return exerciseRepository.searchExercises(searchQuery, pageable); // Search exercises with pagination
    }

    boolean exerciseExists(String exerciseName) {
        return exerciseRepository.existsByName(exerciseName);
    }

    public void importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Exercise> exercises = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell idCell = row.getCell(0);
                    Cell nameCell = row.getCell(1);
                    Cell descriptionCell = row.getCell(2);
                    Cell levelCell = row.getCell(3);
                    Cell moduleCell = row.getCell(4);

                    if (nameCell != null && descriptionCell != null) {
                        String exerciseName = getCellValueAsString(nameCell).trim(); // Get exercise name
                        String exerciseDescription = getCellValueAsString(descriptionCell).trim(); // Get description
                        String exerciseLevel = levelCell != null ? getCellValueAsString(levelCell).trim() : null; // Get level if available
                        String moduleName = moduleCell != null ? getCellValueAsString(moduleCell).trim() : null; // Get module name

                        // Check if exercise already exists based on some criteria (e.g., name)
                        if (!exerciseExists(exerciseName)) {
                            Exercise exercise = new Exercise();
                            exercise.setName(exerciseName);
                            exercise.setDescription(exerciseDescription);
                            exercise.setLevel(Exercise.Level.valueOf(exerciseLevel));

                            exercises.add(exercise);
                        }
                    }
                }
            }

            // Save exercises to the database
            for (Exercise exercise : exercises) {
                exerciseRepository.save(exercise);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error importing exercises from Excel", e);
        }
    }

    // Method to export exercises to Excel
    public ByteArrayInputStream exportExercisesToExcel(List<Exercise> exercises) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Exercises");

        // Create the header row
        String[] headers = { "ID", "Name", "Description", "Level", "Module" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);
        sheet.getRow(0).createCell(2).setCellValue(headers[2]);
        sheet.getRow(0).createCell(3).setCellValue(headers[3]);
        sheet.getRow(0).createCell(4).setCellValue(headers[4]);

        // Populate data rows
        int rowNum = 1;
        for (Exercise exercise : exercises) {
            sheet.createRow(rowNum).createCell(0).setCellValue(exercise.getId());
            sheet.getRow(rowNum).createCell(1).setCellValue(exercise.getName());
            sheet.getRow(rowNum).createCell(2).setCellValue(exercise.getDescription());
            sheet.getRow(rowNum).createCell(3).setCellValue(exercise.getLevel().ordinal());

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

    public Page<Exercise> getExercisesByLanguageAndLevel(Long languageId, String level, Pageable pageable) {
        Exercise.Level exerciseLevel = (level == null || level.trim().isEmpty())
                ? null
                : Exercise.Level.valueOf(level.toUpperCase());

        return exerciseRepository.findByFilters(languageId, exerciseLevel, pageable);
    }
}