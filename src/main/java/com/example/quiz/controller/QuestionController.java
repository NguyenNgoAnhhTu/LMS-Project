package com.example.quiz.controller;

import com.example.exception.NotFoundException;

import com.example.quiz.Request.TransferAllQuestionsDTO;
import com.example.quiz.Request.TransferQuestionDTO;
import com.example.quiz.model.Question;
import com.example.quiz.model.Quiz;
import com.example.quiz.service.QuestionService;
import com.example.quiz.service.QuizService;
import com.example.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("questions")
public class QuestionController {

    @Autowired
    private UserService userService;
    @Autowired
    private QuizService quizService;
    @Autowired
    private QuestionService questionService;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Question");
        model.addAttribute("links", "/style.css");
    }

    @GetMapping()
    public String getQuestions(Model model,
                               @RequestParam(value = "searchQuery", required = false) String searchQuery,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize)  {
        Page<Question> questionsPage;
        Pageable pageable = PageRequest.of(page, pageSize);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            questionsPage = questionService.search(searchQuery, pageable);
        } else {
            questionsPage = questionService.findAll(pageable);
        }

        model.addAttribute("questionsPage", questionsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionsPage.getTotalPages());
        model.addAttribute("totalItems", questionsPage.getTotalElements());
        model.addAttribute("searchQuery", searchQuery);

        model.addAttribute("content","questions/list");
        return "layout";
    }

    @GetMapping("/question-quiz/{name}")
    public String questionQuiz(Model model , @PathVariable String name){
        try {
            Question question = (Question) questionService.findByQuizName(name);
            model.addAttribute("quiz", quizService.findAll());
            model.addAttribute("question", questionService.findAll());
            model.addAttribute("selectedQuestion", question); // Fetch all users for the dropdown
            model.addAttribute("content", "quizes/detail");
        }catch(Exception e){
            return null;
        }
        return "layout";

    }

    @GetMapping("/create")
    public String showCreateForm(Model model) { // Lấy quizId từ URL nếu có

        Question.QuestionType[] questionTypes = Question.QuestionType.values();
        Question question = new Question();

        model.addAttribute("questionType", questionTypes);
        model.addAttribute("question", question);
        model.addAttribute("content", "quizes/detail");

        return "layout";
    }

//    @PostMapping("/create")
//    public String createQuestion(@ModelAttribute QuestionRequestDTO question,
//                                 @PathVariable("quizId") Long quizId) {
//        quizService.createQuestion(quizId, question);
//        return "redirect:/quizes/detail/" + quizId;
//    }



    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Question question = questionService.findById(id).orElse(null);
        List<Quiz> quiz = quizService.findAll();
        model.addAttribute("quiz", quiz);
        model.addAttribute("users", userService.getAllUsers()); // Fetch all users for the dropdown

        model.addAttribute("content", "question/edit");
        return "layout";
    }
    @GetMapping("/delete/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        questionService.deleteById(id);
        return "redirect:/questions";
    }

    @GetMapping("/showQuestion/{courseName}")
    public String showQuestion(Model _model_, @PathVariable String courseName) {
        try {
            Set<Quiz> quizzes = questionService.showAllQuizzesWithQuestions(courseName);

            if (quizzes == null || quizzes.isEmpty()) {
                return "errorPage";
            }
            _model_.addAttribute("quizzes", quizzes);
            _model_.addAttribute("content", "quizes/detail");
        } catch (NotFoundException e) {
            _model_.addAttribute("error", e.getMessage());
            return "errorPage";
        } catch (Exception e) {
            return "errorPage";
        }
        return "layout";
    }

    @Transactional
    @PostMapping("/transfer")
    public String transferQuesnsftion(@ModelAttribute TransferQuestionDTO transferDto) {
        System.out.println("Received transfer request for question ID: " + transferDto.getQuestionId());
        System.out.println("Target quiz ID: " + transferDto.getTargetQuizId());

        // Lấy quiz từ Optional hoặc ném lỗi nếu không tồn tại
        Quiz targetQuiz = quizService.findById(transferDto.getTargetQuizId())
                .orElseThrow(() -> new NotFoundException("Target quiz not found with ID: " + transferDto.getTargetQuizId()));

        questionService.cloneQuestion(transferDto.getQuestionId(), targetQuiz);

        return "redirect:/quizes/detail/" + transferDto.getTargetQuizId();

    }

    @Transactional
    @PostMapping("/transfer-all")
    public String transferAllQuestions(@ModelAttribute TransferAllQuestionsDTO transferDto) {
        System.out.println("Received transfer request for quiz ID: " + transferDto.getSourceQuizId());
        System.out.println("Target quiz ID: " + transferDto.getTargetQuizId());

        // Lấy source và target quiz từ Optional hoặc ném lỗi nếu không tồn tại
        Quiz sourceQuiz = quizService.findById(transferDto.getSourceQuizId())
                .orElseThrow(() -> new NotFoundException("Source quiz not found with ID: " + transferDto.getSourceQuizId()));
        Quiz targetQuiz = quizService.findById(transferDto.getTargetQuizId())
                .orElseThrow(() -> new NotFoundException("Target quiz not found with ID: " + transferDto.getTargetQuizId()));

        // Lấy tất cả câu hỏi từ quiz nguồn
        List<Question> questions = questionService.findByQuiz(sourceQuiz);
        for (Question question : questions) {
            questionService.cloneQuestion(question.getId(), targetQuiz);
        }

        return "redirect:/quizes/detail/" + transferDto.getTargetQuizId();
    }


}

