package com.example.tools;

import com.example.course.CourseService;
import com.example.exception.InputException;
import com.example.quiz.model.Question;
import com.example.quiz.model.Quiz;
import com.example.quiz.service.QuizService;
import com.example.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenerateRandomService {
    //private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHARACTERS = "0123456789";
    private static final Random RANDOM = new Random();

    public static String getRandomString(int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    /*@Autowired
    private QuestionService questionService;*/

    // Hàm lấy ngẫu nhiên danh sách nhỏ các câu hỏi cho 1 bài quiz từ 1 list lớn các câu hỏi
    public List<Question> collectRandomQuestions(List<Question> questionList, int questionsEachQuiz) throws CloneNotSupportedException {
        if (questionsEachQuiz >= questionList.size()) {
            throw new InputException("The number of questions per quiz must not exceed the total number of questions!");
        }

        List<Question> clonedQuestions = new ArrayList<>();
        Collections.shuffle(questionList);
        List<Question> choseQuestions = questionList.subList(0, questionsEachQuiz);

        for (Question question : choseQuestions){
            clonedQuestions.add((Question) question.clone());
        }
        /*for (int i = 0; i < questionList.size(); i++) {
            for (int j = i + 1; j < questionList.size(); j++) {
                if (questionList.get(i).getQuestionText().equals()) {

                }
            }
        }*/

        for (int i = 0; i < clonedQuestions.size(); i++) {
            clonedQuestions.get(i).setQuestionNo(i + 1);
        }

        return clonedQuestions;
    }

    // Hàm add danh sách câu hỏi mới tạo cho 1 quiz trống
    /*public Quiz generateExam(Quiz quiz, List<Question> questionList, int questionsEachQuiz) {
        try {
            quiz.setQuestions(collectRandomQuestions(questionList, questionsEachQuiz));
            return quizService.save(quiz);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }*/

    //Tạo tên ngẫu nhiên
    public String generateRandomName(List<Quiz> quizList) {
        if(quizList == null || quizList.isEmpty()){
            return "Quiz" + getRandomString(6);
        }

        Set<String> existingNames = quizList.stream()
                .map(Quiz::getName)
                .collect(Collectors.toSet());
        String name;
        do {
            name = getRandomString(6);
        } while (existingNames.contains(name) || existingNames.contains("Quiz" + name));
        return "Quiz" + name;
    }


    // Lấy toàn bộ các questions trong 1 course, shuffle lại và chia cho n bài quiz với m questions mỗi bài
    // Lấy ra course -> lấy tất cả quiz của nó -> lấy toàn bộ câu hỏi
    /*public List<Quiz> generateExams(String courseName, int numOfQuizzes, int questionsEachQuiz) {
        if (numOfQuizzes <= 0) {
            throw new InputException("Number of quizs must not be <= 0");
        }

        if (questionsEachQuiz <= 0) {
            throw new InputException("Number of questions must not be <= 0");
        }

        Set<Quiz> quizzes = questionService.showAllQuizzesWithQuestions(courseName);

        List<Quiz> createdQuizzes = new ArrayList<>();

        List<Question> questions = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            questions.addAll(quiz.getQuestions()); // Thêm tất cả câu hỏi từ quiz vào danh sách
        }


        if(questions.isEmpty()){
            throw new NotFoundException("Course has no question!");
        }

        // Tạo quiz
        for (int i = 1; i <= numOfQuizzes; i++) {
            List<Question> shuffledQuestions = collectRandomQuestions(questions, questionsEachQuiz);
            Quiz quiz = new Quiz();
            quiz.setName(generateRandomName(quizzes));
            quiz.setCourse(courseService.findByName(courseName));
            quiz.setDescription("");
            quiz.setCreatedBy(userService.getCurrentUser());
            quiz.setQuestions(new HashSet<>(shuffledQuestions));
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setQuizType(Quiz.QuizType.CLOSE);
            for(Question question : shuffledQuestions){
                question.getQuizzes().add(quiz);
                questionService.save(question);
            }
            quizService.save(quiz);
            createdQuizzes.add(quiz);
        }

        return createdQuizzes;
    }*/


}
