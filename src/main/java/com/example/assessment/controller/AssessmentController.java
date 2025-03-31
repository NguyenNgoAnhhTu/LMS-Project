package com.example.assessment.controller;


import com.example.course.CourseService;
import com.example.exercise.Exercise;
import com.example.assessment.model.Assessment;
import com.example.assessment.service.AssessmentService;
import com.example.assessment.service.AssessmentTypeService;
import com.example.assessment.service.StudentAssessmentAttemptService;
import com.example.assessment.service.InvitedCandidateService;

import com.example.exercise.ExerciseService;
import com.example.quiz.service.QuestionService;
import com.example.quiz.service.QuizService;
import com.example.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/assessments")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AssessmentTypeService assessmentTypeService;

    @Autowired
    private StudentAssessmentAttemptService assessmentAttemptService;

    @Autowired
    private InvitedCandidateService candidateService;

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Assessments");
        model.addAttribute("links", "/style.css");
    }

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "searchQuery", required = false) String searchQuery,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Page<Assessment> assessments;
        Pageable pageable = PageRequest.of(page, pageSize);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            assessments = assessmentService.search(searchQuery, pageable);
        } else {
            assessments = assessmentService.findAll(pageable);
        }

        model.addAttribute("assessments", assessments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", assessments.getTotalPages());
        model.addAttribute("totalItems", assessments.getTotalElements());
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("content", "assessments/list");

        return "layout";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("assessment", new Assessment());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("exercises", exerciseService.findAllExercises());
        model.addAttribute("questions", questionService.findAll());

        model.addAttribute("assessmentTypes", assessmentTypeService.getAllAssessmentTypes());
        // Add all users to the model for selection in the form
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", userService.getCurrentUser());

        model.addAttribute("content", "assessments/create");

        return "layout";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Assessment assessment) {
        assessmentService.save(assessment);
        return "redirect:/assessments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        // Retrieve the assessment by its ID
        Assessment assessment = assessmentService.findById(id).orElse(null);

        // If the assessment is not found, redirect to assessments or handle the error
        if (assessment == null) {
            return "redirect:/assessments";
        }

        // Add the assessment to the model
        model.addAttribute("assessment", assessment);

        // Add quizzes, questions, and related data
        model.addAttribute("allQuizzes", quizService.findAll());
        model.addAttribute("questions", questionService.findAll());
        model.addAttribute("selectedQuiz", quizService.findById(id));
        model.addAttribute("totalQuestionsSelectedQuiz", assessment.getQuestions().size());

        // Add selected exercises
        List<Long> selectedExerciseIds = assessment.getExercises()
                .stream()
                .map(Exercise::getId)
                .collect(Collectors.toList());
        model.addAttribute("selectedExercises", selectedExerciseIds);

        // Add other attributes
        model.addAttribute("exercises", exerciseService.findAllExercises());

        // Add other necessary attributes
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("assessmentTypes", assessmentTypeService.getAllAssessmentTypes());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", userService.getCurrentUser());

        // Set the content for the edit view
        model.addAttribute("content", "assessments/edit");

        return "layout";
    }


    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable("id") Long id, Model model) {
        // Retrieve the assessment by its ID
        Assessment assessment = assessmentService.findById(id).orElse(null);

        // If the assessment is not found, handle the error
        if (assessment == null) {
            // Redirect to the assessments list or handle the error differently
            return "redirect:/assessments";
        }

        // Add the retrieved assessment to the model
        model.addAttribute("assessment", assessment);

        // Add related data for display
        model.addAttribute("exercises", assessment.getExercises()); // Get exercises related to this assessment
        model.addAttribute("questions", assessment.getQuestions()); // Get questions related to this assessment
       model.addAttribute("registeredAttempts", assessmentAttemptService.findByAssessmentId(id)); // Fetch attempts for this assessment
        model.addAttribute("invitedCandidates", candidateService.findByAssessmentId(id)); // Fetch invited candidates

        // Add the current user (if needed for permissions or tracking)
        model.addAttribute("currentUser", userService.getCurrentUser());

        // Set the content for the detail view
        model.addAttribute("content", "assessments/detail");

        return "layout";
    }


    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute Assessment assessment) {
        assessment.setId(id);
        assessmentService.save(assessment);
        return "redirect:/assessments";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String delete(@PathVariable("id") Long id) {
        assessmentService.deleteById(id);
        return "redirect:/assessments";
    }

    @GetMapping("/print")
    public String print(Model model) {
        List<Assessment> assessments = assessmentService.findAll();
        model.addAttribute("assessments", assessments);
        return "assessments/print";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        List<Assessment> assessments = assessmentService.findAll();
        ByteArrayInputStream excelFile = assessmentService.exportToExcel(assessments);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=assessments.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        assessmentService.importExcel(file);
        return "redirect:/assessments";
    }
}