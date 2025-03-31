package com.example.exercise;

import com.example.assessment.model.ProgrammingLanguage;
import com.example.assessment.service.ProgrammingLanguageService;
import com.example.testcase.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.*;

@Controller
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ProgrammingLanguageService programmingLanguageService;
    private final ExerciseService exerciseService;
    private final TestCaseService testCaseService;

    // Common attributes for all views
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Exercises");
        model.addAttribute("links", "/style.css");
        model.addAttribute("links", "/print.css");
    }

    @GetMapping
    public String getList(
            @RequestParam(value = "language", required = false) Long languageId,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Exercise> exercisesPage = exerciseService.getExercisesByLanguageAndLevel(languageId, level, pageable);

        model.addAttribute("exercises", exercisesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", exercisesPage.getTotalPages());
        model.addAttribute("languages", programmingLanguageService.findAll());
        model.addAttribute("currentLanguage" , languageId);
        model.addAttribute("currentLevel" , level);

        return "exercises/list";
    }

    // Show create exercise form (existing method)
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("exercise", new Exercise());
        TestCaseFormList testCaseFormList = new TestCaseFormList();
        testCaseFormList.setTestCasesList(List.of(new TestCaseForm())); // Ít nhất 1 test case
        model.addAttribute("testCaseFormList", testCaseFormList); // Initialize the wrapper
        List<ProgrammingLanguage> programmingLanguages = programmingLanguageService.getAllProgrammingLanguages();
        model.addAttribute("programmingLanguages", programmingLanguages);
        model.addAttribute("content", "exercises/create");
        return "layout";
    }

    // Create a new exercise (updated method)
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createExercise(@ModelAttribute Exercise exercise, @ModelAttribute TestCaseRequest request) {
        if (exercise == null) {
            return ResponseEntity.badRequest().body("exercise cannot be null");
        }

        List<TestCase> testCasesFinal = new ArrayList<>();

        if ("json".equalsIgnoreCase(request.getTestCaseMethod())) {
            if (request.getTestCasesJson() == null || request.getTestCasesJson().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("TestCases JSON is EMPTY!");
            }

            // Parse JSON to List<TestCaseForm>
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<TestCaseForm> testCaseForms = objectMapper.readValue(request.getTestCasesJson(), new TypeReference<List<TestCaseForm>>() {});
                if (testCaseForms.isEmpty()) {
                    return ResponseEntity.badRequest().body("No Test Cases in JSON!");
                }
                for (TestCaseForm tcForm : testCaseForms) {
                    TestCase tc = new TestCase();
                    tc.setInput(tcForm.getInput());
                    tc.setExpectedOutput(tcForm.getExpectedOutput());
                    tc.setExercise(exercise);
                    testCasesFinal.add(tc);
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid JSON format!");
            }
        } else if ("ui".equalsIgnoreCase(request.getTestCaseMethod())) {
            // Process manual UI test cases
            if (request.getTestCaseFormList() == null || request.getTestCaseFormList().getTestCasesList() == null || request.getTestCaseFormList().getTestCasesList().isEmpty()) {
                System.out.println("❌ ERROR: TestCasesList is NULL or EMPTY!");
                return ResponseEntity.badRequest().body("TestCasesList is NULL or EMPTY!");
            }

            for (TestCaseForm tcForm : request.getTestCaseFormList().getTestCasesList()) {
                if (tcForm.getInput() == null || tcForm.getInput().trim().isEmpty()
                        || tcForm.getExpectedOutput() == null || tcForm.getExpectedOutput().trim().isEmpty()) {
                    System.out.println("❌ ERROR: One or more Test Cases are invalid!");
                    return ResponseEntity.badRequest().body("One or more Test Cases are invalid!");
                }
                TestCase tc = new TestCase();
                tc.setInput(tcForm.getInput());
                tc.setExpectedOutput(tcForm.getExpectedOutput());
                tc.setExercise(exercise);
                testCasesFinal.add(tc);
            }
        } else {
            System.out.println("❌ ERROR: Invalid Test Case Method!");
            return ResponseEntity.badRequest().body("Invalid Test Case Method!");
        }

        // Assign test cases to the exercise
        exercise.setTestCases(testCasesFinal);

        // Save Exercise to Database
        exerciseService.saveExercise(exercise);

//        return "redirect:/exercises";
        return ResponseEntity.ok("Exercise & Test Cases Saved!");
    }

    // Show the edit form for a specific exercise
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseService.getExerciseById(id).orElse(null);

        if (exercise == null) {
            return "redirect:/exercises"; // Redirect if the exercise doesn't exist
        }

        // Fetch all programming languages for the dropdown
        List<ProgrammingLanguage> programmingLanguages = programmingLanguageService.getAllProgrammingLanguages();

        model.addAttribute("exercise", exercise); // Add the current exercise to the model
        model.addAttribute("programmingLanguages", programmingLanguages); // Add the languages to the model
        model.addAttribute("content", "exercises/edit");

        return "layout";
    }

    // Handle updating an existing exercise
    @PostMapping("/edit/{id}")
    public String updateExercise(@PathVariable Long id, @ModelAttribute Exercise exercise, Model model) {
        // Optional: Check if the exercise name already exists, if needed
        if (exerciseService.exerciseExists(exercise.getName())) {
            model.addAttribute("error", "Exercise name already exists!");
            model.addAttribute("content", "exercises/edit");
            return "layout";
        }

        // Set the ID of the exercise to ensure it's the right one for updating
        exercise.setId(id);

        // Save the updated exercise
        exerciseService.saveExercise(exercise);

        // Redirect to the exercises list after saving
        return "redirect:/exercises";
    }

    // Delete a specific exercise
    @GetMapping("/delete/{id}")
    public String deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return "redirect:/exercises";
    }

    // Print roles page
    @GetMapping("/print")
    public String print(Model model) {
        List<Exercise> exercises = exerciseService.findAllExercises();
        model.addAttribute("exercises", exercises);
        return "exercises/print";
    }
    // Export exercises to an Excel file
//    @GetMapping("/export")
//    public ResponseEntity<InputStreamResource> exportExercises() {
//        // Fetch all exercises (page size set to max to get all records)
//        List<Exercise> exercises = exerciseService.findAllExercises();
//
//        // Convert exercises to an Excel file
//        ByteArrayInputStream excelFile = exerciseService.exportExercisesToExcel(exercises);
//
//        // Create headers for the response (Content-Disposition to trigger file download)
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Disposition", "attachment; filename=exercises.xlsx");
//
//        // Return the file in the response
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(new InputStreamResource(excelFile));
//    }
//
//    // Import exercises from an Excel file
//    @PostMapping("/import")
//    public String importModules(@RequestParam("file") MultipartFile file) {
//        exerciseService.importExcel(file);
//        return "redirect:/modules";
//    }

}
