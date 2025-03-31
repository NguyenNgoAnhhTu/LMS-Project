package com.example.reports.controller;

import com.example.assessment.model.*;
import com.example.assessment.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/assessment-reports")
public class AssessmentReportController {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private StudentAssessmentAttemptRepository attemptRepository;

    @Autowired
    private InvitedCandidateRepository invitedCandidateRepository;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Assessment Report");
        model.addAttribute("links", "/style.css");
    }

    @GetMapping("/assessment")
    public String assessmentReports(Model model) {
        List<Assessment> assessments = assessmentRepository.findAll();
//        List<StudentAssessmentAttempt> attempts = attemptRepository.findAll();

        model.addAttribute("assessments", assessments);
//        model.addAttribute("attempts", attempts);

        model.addAttribute("content","reports/assessment/assessments");

        return "layout";
    }

//    @GetMapping("/email-list/{course}")
//    public String assessmentEmailList(@PathVariable String course, Model model) {
//        List<Assessment> assessments = assessmentRepository.findByCourse(course);
//        List<String> emailList = invitedCandidateRepository
//                .findByAssessmentIn(assessments)
//                .stream()
//                .map(InvitedCandidate::getEmail)
//                .distinct()
//                .collect(Collectors.toList());
//
//        model.addAttribute("email_list", emailList);
//        return "reports/assessment/email_list";
//    }
//
//    @GetMapping("/detail/{id}")
//    public String assessmentDetail(@PathVariable Long id, Model model) {
//        Assessment assessment = assessmentRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
//        model.addAttribute("assessment", assessment);
//        return "reports/assessment/assessment_details";
//    }
//
//    @GetMapping("/student-report/{title}")
//    public String studentAssessmentReport(@PathVariable String title, Model model) {
//        Assessment assessment = assessmentRepository.findByTitle(title)
//                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with title: " + title));
//        List<StudentAssessmentAttempt> attempts = attemptRepository.findByAssessment(assessment);
//
//        model.addAttribute("assessment", assessment);
//        model.addAttribute("attempts", attempts);
//        return "reports/assessment/student_assessment_report";
//    }
//
//    @GetMapping("/student-answers/{assessmentId}/{attemptId}")
//    public String viewStudentAnswers(
//            @PathVariable Long assessmentId,
//            @PathVariable Long attemptId,
//            @RequestParam(required = false) String email,
//            Model model) {
//
//        Assessment quiz = assessmentRepository.findById(assessmentId)
//                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + assessmentId));
//        StudentAssessmentAttempt attempt = attemptRepository.findById(attemptId)
//                .orElseThrow(() -> new IllegalArgumentException("Attempt not found with id: " + attemptId));
//
//        List<StudentAnswer> studentAnswers = quiz.getQuestions()
//                .stream()
//                .flatMap(q -> q.getStudentAnswers().stream())
//                .collect(Collectors.toList());
//
//        if (email != null) {
//            studentAnswers = studentAnswers.stream()
//                    .filter(a -> a.getAttempt().getEmail().equals(email))
//                    .collect(Collectors.toList());
//        }
//
//        List<Object> correctAnswers = quiz.getQuestions().stream()
//                .map(q -> new Object() {
//                    public Long questionId = q.getId();
//                    public List<AnswerOption> correctOptions = q.getAnswerOptions().stream()
//                            .filter(AnswerOption::isCorrect)
//                            .collect(Collectors.toList());
//                })
//                .collect(Collectors.toList());
//
//        model.addAttribute("quiz", quiz);
//        model.addAttribute("attempt", attempt);
//        model.addAttribute("student_answers", studentAnswers);
//        model.addAttribute("correct_answers", correctAnswers);
//
//        return "reports/assessment/student_answers";
//    }
}

