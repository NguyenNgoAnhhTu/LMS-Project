package com.example.quiz.service;


import com.example.course.Course;
import com.example.course.CourseRepository;
import com.example.course.CourseService;
import com.example.exception.InputException;
import com.example.exception.NotFoundException;
import com.example.quiz.model.AnswerOption;
import com.example.quiz.model.Question;
import com.example.quiz.model.Question.QuestionType;
import com.example.quiz.model.Quiz;
import com.example.quiz.model.SchedulerUtil;
import com.example.quiz.repository.AnswerOptionRepository;
import com.example.quiz.repository.AnswerRepository;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.QuizRepository;
import com.example.tools.CovertExcelToJsonService;
import com.example.tools.GenerateRandomService;
import com.example.user.User;
import com.example.user.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.tools.QuizDuplicateChecker.checkDuplicateQuestion;

@Service
public class QuestionService {

    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ScheduleJob scheduleJob;
    @Autowired
    private UserService userService;

    @Autowired
    private GenerateRandomService generateRandomService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;
    @Autowired
    private QuizService quizService;

    @Autowired
    private CovertExcelToJsonService covertExcelToJsonService;

    public Optional<Question> findById(Long id) {
        return questionRepository.findById(id);
    }

    public Question save(Question question) {
        return questionRepository.save(question);
    }

    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    public Page<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Page<Question> search(String searchQuery, Pageable pageable) {
        return questionRepository.search(searchQuery, pageable);
    }

    public List<Question> findByQuiz(Quiz quiz) {
        return questionRepository.findByQuizzes_Id(quiz.getId());
    }

    public List<Question> findByQuizName(String name) {
        return questionRepository.findByQuizzes_Name(name);
    }


    private String getCellValueAsString(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }


    //Find list quiz and question form course
    public Set<Quiz> showAllQuizzesWithQuestions(String courseName) {
        Set<Quiz> quizzes = quizRepository.findQuizByCourseName(courseName);

        if (quizzes == null || quizzes.isEmpty()) {
            throw new NotFoundException("Quiz not found");
        }

        for (Quiz quiz : quizzes) {
            List<Question> questions = questionRepository.findByQuizzes_Name(quiz.getName());

            quiz.setQuestions((questions != null && !questions.isEmpty())
                    ? new HashSet<>(questions)
                    : Collections.emptySet());
        }

        return quizzes;
    }

    public List<Question> findQuestionsByQuizId(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quiz not found!"));
        return new ArrayList<>(quiz.getQuestions());
    }

    @Transactional
    public void importExcel(MultipartFile _file_, String courseName) throws ObjectAlreadyExistsException {
        try (Workbook workbook = new XSSFWorkbook(_file_.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Question> questions = new ArrayList<>();
            List<String> optionLabelList = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
            List<Course> courses = courseService.getAllCourses();
            List<Quiz> quizzes = new ArrayList<>();
            for (Course course : courses) {
                if (course.getQuizzes() != null && !course.getQuizzes().isEmpty()) {
                    quizzes.addAll(course.getQuizzes());
                }
            }
            //Set<Quiz> quizzes = quizRepository.findQuizByCourseName(courseName);
            List<String> existingQuestions = new ArrayList<>();
            int numDupQues = 0;
            double minNumOfDupQues = (1.0 / 3) * (rowCount - 1);

            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    existingQuestions.addAll(quiz.getQuestions().stream().map(Question::getQuestionText).toList());
                }
            }

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                // QUESTION ------------------------------------------
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Extract cells
                    Cell questionTextCell = row.getCell(3);
                    Cell questionTypeCell = row.getCell(4);
                    Cell answerCell = row.getCell(5);
                    Cell optionA = row.getCell(6);
                    Cell optionB = row.getCell(7);
                    Cell optionC = row.getCell(8);
                    Cell optionD = row.getCell(9);
                    Cell optionE = row.getCell(10);
                    Cell optionF = row.getCell(11);
                    Cell optionG = row.getCell(12);
                    Cell optionH = row.getCell(13);
                    Cell optionI = row.getCell(14);
                    Cell optionJ = row.getCell(15);

                    String questionText = questionTextCell != null ? getCellValueAsString(questionTextCell).trim() : null;
                    // CHECK DUPLICATE QUESTION
                    if (questionText != null) {
                        if (checkDuplicateQuestion(existingQuestions, questionText)) {
                            numDupQues++;
                            if ((double) numDupQues >= Math.ceil(minNumOfDupQues)) {
                                throw new ObjectAlreadyExistsException("Can not import file! More than 1/3 of the questions in the file are duplicated!");
                            }
                        }
                    }

                    String questionTypeString = questionTypeCell != null ? getCellValueAsString(questionTypeCell).trim() : null;
                    List<Cell> optionCellList = List.of(optionA, optionB, optionC,
                            optionD, optionE, optionF,
                            optionG, optionH, optionI,
                            optionJ);

                    List<AnswerOption> answerOptionListTemp = new ArrayList<>();

                    // Tạo các obj answer option
                    for (int y = 0; y < optionCellList.size(); y++) {
                        if (optionCellList.get(y) != null && !getCellValueAsString(optionCellList.get(y)).isEmpty()) {
                            AnswerOption answerOption = new AnswerOption();
                            answerOption.setOptionLabel(optionLabelList.get(y));
                            answerOption.setOptionText(getCellValueAsString(optionCellList.get(y)));

                            if (Arrays.stream(getCellValueAsString(answerCell).split(", "))
                                    .toList()
                                    .contains(optionLabelList.get(y))) {
                                answerOption.setIsCorrect(true);
                            } else {
                                answerOption.setIsCorrect(false);
                            }

                            answerOptionListTemp.add(answerOption);
                        }
                    }

                    // Tạo question
                    if (questionText != null && questionTypeString != null) {
                        QuestionType questionType = setTypeBaseOnStringInput(questionTypeString);

                        Question question = new Question();

                        question.setQuestionNo(i);
                        question.setQuestionText(questionText);
                        question.setQuestionType(questionType);

                        for (AnswerOption answerOption : answerOptionListTemp) {
                            question.addAnswerOption(answerOption);
                        }

                        questions.add(question);
                    }
                }
                //----------------------------------------------------------
            }
            // Tạo quiz

            Quiz quiz = new Quiz();

            String originalFilenameName = _file_.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizzes, fileName);
            if (numberOfDuplicateNames != 0) {
                int addPara = numberOfDuplicateNames + 1;
                quiz.setName(fileName + " (" + addPara + ")");
            } else {
                quiz.setName(fileName);
            }

            quiz.setDescription("This is an auto-created quiz!");
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            User user = userService.getCurrentUser();
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setCreatedBy(user);
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : questions) {
                quiz.addQuestion(question);
            }

            Course course = courseService.findByName(courseName);
            course.addQuiz(quiz);

            Course savedCourse = courseService.save(course);
            List<Quiz> newestQuizzes = savedCourse.getQuizzes();
            Quiz savedQuiz = newestQuizzes.get(newestQuizzes.size() - 1);

            try {
                SchedulerUtil.startScheduler();

                Scheduler scheduler = SchedulerUtil.getScheduler();
                scheduleJob.scheduleQuizOpenJob(savedQuiz.getId(), savedQuiz.getStartTime());
                scheduleJob.scheduleQuizCloseJob(savedQuiz.getId(), savedQuiz.getEndTime());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (IOException _e_) {
            throw new RuntimeException("Error importing questions from Excel", _e_);
        } catch (ObjectAlreadyExistsException e) {
            throw new ObjectAlreadyExistsException(e.getMessage());
        }
    }


    public QuestionType setTypeBaseOnStringInput(String questionTypeString) {
        if (questionTypeString.equalsIgnoreCase("Multiple Choice")) {
            return QuestionType.MCQ;
        } else if (questionTypeString.equalsIgnoreCase("Single Choice")) { // Tùy vào type trong excel
            return QuestionType.SCQ;
        } else {
            return QuestionType.TEXT;
        }
    }


    // GENERATE EXAM
    @Transactional
    public List<Quiz> generateQuizzes(String courseName, int numOfQuizzes, int questionsEachQuiz) throws CloneNotSupportedException {
        if (numOfQuizzes <= 0) {
            throw new InputException("Number of quizs must not be <= 0");
        }

        if (questionsEachQuiz <= 0) {
            throw new InputException("Number of questions must not be <= 0");
        }

        // CHECK TRÙNG NAME
        List<Course> courses = courseService.getAllCourses();
        List<Quiz> quizList = new ArrayList<>();
        for (Course course : courses) {
            if (course.getQuizzes() != null && !course.getQuizzes().isEmpty()) {
                quizList.addAll(course.getQuizzes());
            }
        }


        List<Quiz> createdQuizzes = new ArrayList<>();

        Set<Quiz> quizzes = quizRepository.findQuizByCourseName(courseName);

        if (quizzes == null || quizzes.isEmpty()) {
            throw new NotFoundException("No question found in this course!");
        }

        List<Question> questions = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                questions.addAll(quiz.getQuestions()); // Thêm tất cả câu hỏi từ quiz vào danh sách
            }
        }

        if (questions.isEmpty()) {
            throw new NotFoundException("Course has no question!");
        }

        // Tạo quiz
        for (int i = 0; i < numOfQuizzes; i++) {
            List<Question> shuffledQuestions = generateRandomService.collectRandomQuestions(questions, questionsEachQuiz);
            Quiz quiz = new Quiz();
            quiz.setName(generateRandomService.generateRandomName(quizList));
            //quiz.setCourse(courseService.findByName(courseName));
            quiz.setDescription("This is an auto-created quiz!");
            quiz.setCreatedBy(userService.getCurrentUser());
            //quiz.setQuestions(new HashSet<>(shuffledQuestions));
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : shuffledQuestions) {
                quiz.addQuestion(question);
            }

            Course course = courseService.findByName(courseName);
            course.addQuiz(quiz);

            Course savedCourse = courseService.save(course);
            List<Quiz> newestQuizzes = savedCourse.getQuizzes();
            Quiz savedQuiz = newestQuizzes.get(newestQuizzes.size() - 1);

            try {
                SchedulerUtil.startScheduler();

                Scheduler scheduler = SchedulerUtil.getScheduler();
                scheduleJob.scheduleQuizOpenJob(savedQuiz.getId(), savedQuiz.getStartTime());
                scheduleJob.scheduleQuizCloseJob(savedQuiz.getId(), savedQuiz.getEndTime());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

            createdQuizzes.add(savedQuiz);
        }

        return createdQuizzes;
    }

    public int countDuplicateName(List<Quiz> quizList, String name) {
        List<String> existingNames = quizList.stream()
                .map(Quiz::getName)
                .toList();

        int count = 0;
        for (String existingName : existingNames) {
            if (existingName.contains(name)) {
                count++;
            }
        }
        return count;
    }

    @Transactional
    public Map<String, Object> reviewQuiz(MultipartFile _file_, String courseName) {
        try (Workbook workbook = new XSSFWorkbook(_file_.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            List<Question> questions = new ArrayList<>();
            List<String> optionLabelList = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
            List<String> correctAnswers = new ArrayList<>();

            //List<Course> courses = courseService.getAllCourses();
            List<Quiz> quizzes = quizService.findAll();
            //Set<Quiz> quizzes = quizRepository.findQuizByCourseName(courseName);
            List<String> existingQuestions = new ArrayList<>();
            //int numDupQues = 0;
            //double minNumOfDupQues = (1.0 / 3) * (rowCount - 1);
            List<String> dupQueNos = new ArrayList<>();

            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    existingQuestions.addAll(quiz.getQuestions().stream().map(Question::getQuestionText).toList());
                }
            }

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                // QUESTION ------------------------------------------
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Extract cells
                    Cell questionNoCell = row.getCell(0);
                    Cell questionTextCell = row.getCell(3);
                    Cell questionTypeCell = row.getCell(4);
                    Cell answerCell = row.getCell(5);
                    Cell optionA = row.getCell(6);
                    Cell optionB = row.getCell(7);
                    Cell optionC = row.getCell(8);
                    Cell optionD = row.getCell(9);
                    Cell optionE = row.getCell(10);
                    Cell optionF = row.getCell(11);
                    Cell optionG = row.getCell(12);
                    Cell optionH = row.getCell(13);
                    Cell optionI = row.getCell(14);
                    Cell optionJ = row.getCell(15);

                    String questionText = questionTextCell != null ? getCellValueAsString(questionTextCell).trim() : null;
                    String questionNo = questionNoCell != null ? getCellValueAsString(questionNoCell).trim() : null;

                    // CHECK DUPLICATE QUESTION
                    if (questionText != null) {
                        if (checkDuplicateQuestion(existingQuestions, questionText)) {
                            dupQueNos.add(questionNo);
                        }
                    }

                    correctAnswers.add(getCellValueAsString(answerCell));
                    String questionTypeString = questionTypeCell != null ? getCellValueAsString(questionTypeCell).trim() : null;
                    List<Cell> optionCellList = List.of(optionA, optionB, optionC,
                            optionD, optionE, optionF,
                            optionG, optionH, optionI,
                            optionJ);

                    List<AnswerOption> answerOptionListTemp = new ArrayList<>();

                    // Tạo các obj answer option
                    for (int y = 0; y < optionCellList.size(); y++) {
                        if (optionCellList.get(y) != null && !getCellValueAsString(optionCellList.get(y)).isEmpty()) {
                            AnswerOption answerOption = new AnswerOption();
                            answerOption.setOptionLabel(optionLabelList.get(y));
                            answerOption.setOptionText(getCellValueAsString(optionCellList.get(y)));

                            if (Arrays.stream(getCellValueAsString(answerCell).split(", "))
                                    .toList()
                                    .contains(optionLabelList.get(y))) {
                                answerOption.setIsCorrect(true);
                            } else {
                                answerOption.setIsCorrect(false);
                            }

                            answerOptionListTemp.add(answerOption);
                        }
                    }

                    // Tạo question
                    if (questionText != null && questionTypeString != null) {
                        QuestionType questionType = setTypeBaseOnStringInput(questionTypeString);

                        Question question = new Question();

                        question.setQuestionNo(i);
                        question.setQuestionText(questionText);
                        question.setQuestionType(questionType);

                        for (AnswerOption answerOption : answerOptionListTemp) {
                            question.addAnswerOption(answerOption);
                        }

                        questions.add(question);
                    }
                }
                //----------------------------------------------------------
            }

            /*String originalFilenameName = _file_.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizzes, fileName);
            if (numberOfDuplicateNames != 0) {
                int addPara = numberOfDuplicateNames + 1;
                fileName = fileName + " (" + addPara + ")";
            }*/
            // Tạo quiz
            /*Quiz quiz = new Quiz();

            String originalFilenameName = _file_.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizzes, fileName);
            if (numberOfDuplicateNames != 0) {
                int addPara = numberOfDuplicateNames + 1;
                quiz.setName(fileName + " (" + addPara + ")");
            } else {
                quiz.setName(fileName);
            }

            quiz.setDescription("This is an auto-created quiz!");
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            User user = userService.getCurrentUser();
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setCreatedBy(user);
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : questions) {
                quiz.addQuestion(question);
            }*/

            //String errorDupQues = "";
            /*if((double) numDupQues >= Math.ceil(minNumOfDupQues)){
                errorDupQues = "Can not create quiz! More than 1/3 of the questions in the file are duplicated!";
            }*/

            //Đặt tên quiz
            String originalFilenameName = _file_.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizService.findAll(), fileName);
            if (numberOfDuplicateNames != 0) {
                int addPara = numberOfDuplicateNames + 1;
                fileName = fileName + " (" + addPara + ")";
            }

            /*Map<Integer, List<Integer>> duplicatedRows = checkDuplicateInExcel(_file_);
            List<String> warnings = new ArrayList<>();
            for (Question question : questions){
                String warning = null;
                for (Map.Entry<Integer, List<Integer>> entry : duplicatedRows.entrySet()){
                    if (entry.getKey() == question.getQuestionNo()){
                        warning = "This question can be duplicated with question " + entry.getValue();
                    } else if (entry.getValue().contains(question.getQuestionNo())){
                        warning = "This question can be duplicated with question " + entry.getKey();
                    }
                }
                warnings.add(warning);
            }*/
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("Questions", questions);
            //reviewData.put("Correct", correctAnswers);
            reviewData.put("fileName", fileName);
            reviewData.put("courseName", courseName);
            reviewData.put("DuplicateQuestionNos", dupQueNos);

            return reviewData;
        } catch (IOException e) {
            throw new RuntimeException("Error importing questions from Excel", e);
        }
    }

    public Question cloneQuestion(Long questionId, Quiz targetQuiz) {
        Question originalQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // Đếm số lượng câu hỏi hiện có trong targetQuiz để xác định questionNo mới
        int newQuestionNo = questionRepository.countByQuiz(targetQuiz) + 1;

        // Tạo bản sao của câu hỏi
        Question clonedQuestion = new Question();

        clonedQuestion.setQuestionNo(newQuestionNo); // Gán số thứ tự mới
        clonedQuestion.setQuestionText(originalQuestion.getQuestionText());
        clonedQuestion.setQuestionType(originalQuestion.getQuestionType());
        clonedQuestion.setPoints(originalQuestion.getPoints());
        clonedQuestion.setQuizzes(targetQuiz); // Gán vào quiz mới

        // Tạo bản sao của AnswerOption (nếu có)
        List<AnswerOption> clonedOptions = new ArrayList<>();
        for (AnswerOption option : originalQuestion.getAnswerOptions()) {
            AnswerOption clonedOption = new AnswerOption();
            clonedOption.setOptionText(option.getOptionText());
            clonedOption.setIsCorrect(option.getIsCorrect());
            clonedOption.setQuestion(clonedQuestion);
            clonedOption.setOptionLabel(option.getOptionLabel());

            clonedOptions.add(clonedOption);
        }
        clonedQuestion.setAnswerOptions(clonedOptions);

        return questionRepository.save(clonedQuestion);
    }


    @Transactional
    public void importFileFromReview(List<Question> questions, String fileName, String courseName) {
        try {
            Quiz quiz = new Quiz();

            quiz.setName(fileName);
            quiz.setDescription("This is an auto-created quiz!");
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            User user = userService.getCurrentUser();
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setCreatedBy(user);
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : questions) {
                quiz.addQuestion(question);
            }

            Course course = courseService.findByName(courseName);
            course.addQuiz(quiz);

            Course savedCourse = courseService.save(course);
            List<Quiz> newestQuizzes = savedCourse.getQuizzes();
            Quiz savedQuiz = newestQuizzes.get(newestQuizzes.size() - 1);

            try {
                SchedulerUtil.startScheduler();

                Scheduler scheduler = SchedulerUtil.getScheduler();
                scheduleJob.scheduleQuizOpenJob(savedQuiz.getId(), savedQuiz.getStartTime());
                scheduleJob.scheduleQuizCloseJob(savedQuiz.getId(), savedQuiz.getEndTime());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // IMPORT JSON
    @Transactional
    public void importJson(MultipartFile file, String courseName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = file.getInputStream();
            //byte[] jsonData = inputStream.readAllBytes();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            String totalQuestions = rootNode.get("total_questions").asText();
            JsonNode questionNode = rootNode.get("questions");


            List<Course> courses = courseService.getAllCourses();
            List<Quiz> quizzes = new ArrayList<>();
            for (Course course : courses) {
                if (course.getQuizzes() != null && !course.getQuizzes().isEmpty()) {
                    quizzes.addAll(course.getQuizzes());
                }
            }

            List<String> existingQuestions = new ArrayList<>();
            int numDupQues = 0;
            double minNumOfDupQues = (1.0 / 3) * (Double.parseDouble(totalQuestions));

            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    existingQuestions.addAll(quiz.getQuestions().stream().map(Question::getQuestionText).toList());
                }
            }

            List<Question> questions = objectMapper.convertValue(restoreJsonNode(questionNode), new TypeReference<List<Question>>() {
            });
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                question.setQuestionNo(i + 1);

                if (question.getQuestionText() != null) {
                    if (checkDuplicateQuestion(existingQuestions, question.getQuestionText())) {
                        numDupQues++;
                        if ((double) numDupQues >= Math.ceil(minNumOfDupQues)) {
                            throw new ObjectAlreadyExistsException("Can not import file! More than 1/3 of the questions in the file are duplicated!");
                        }
                    }
                }

                List<AnswerOption> answerOptions = question.getAnswerOptions();
                for (int j = 0; j < answerOptions.size(); j++) {
                    answerOptions.get(j).setOptionLabel(String.valueOf((char) ('A' + j)));
                }
            }

            Quiz quiz = new Quiz();

            String originalFilenameName = file.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizzes, fileName);
            if (numberOfDuplicateNames != 0) {
                int addPara = numberOfDuplicateNames + 1;
                quiz.setName(fileName + " (" + addPara + ")");
            } else {
                quiz.setName(fileName);
            }

            quiz.setDescription("This is an auto-created quiz!");
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            User user = userService.getCurrentUser();
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setCreatedBy(user);
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : questions) {
                quiz.addQuestion(question);
            }

            Course course = courseService.findByName(courseName);
            course.addQuiz(quiz);

            Course savedCourse = courseService.save(course);
            List<Quiz> newestQuizzes = savedCourse.getQuizzes();
            Quiz savedQuiz = newestQuizzes.get(newestQuizzes.size() - 1);

            try {
                SchedulerUtil.startScheduler();

                Scheduler scheduler = SchedulerUtil.getScheduler();
                scheduleJob.scheduleQuizOpenJob(savedQuiz.getId(), savedQuiz.getStartTime());
                scheduleJob.scheduleQuizCloseJob(savedQuiz.getId(), savedQuiz.getEndTime());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode restoreJsonNode(JsonNode jsonNode) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            objectNode.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isTextual()) { // Nếu là chuỗi thì xử lý
                    objectNode.put(entry.getKey(), restoreString(value.asText()));
                } else if (value.isObject() || value.isArray()) { // Nếu là object hoặc array thì đệ quy xử lý tiếp
                    objectNode.set(entry.getKey(), restoreJsonNode(value));
                }
            });
            return objectNode;
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (JsonNode element : jsonNode) {
                arrayNode.add(restoreJsonNode(element));
            }
            return arrayNode;
        }
        return jsonNode;
    }

    private String restoreString(String formattedString) {
        String restoredString = formattedString.replaceAll("<br>", "\n")
                .replace("&nbsp;&nbsp;&nbsp;&nbsp;", "    ");
        return StringEscapeUtils.unescapeHtml4(restoredString);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow();
        answerRepository.deleteAllByQuestion(question);
        answerOptionRepository.deleteAllByQuestion(question);
        questionRepository.delete(question);
    }

    @Transactional
    public void deleteAllQuestions(Long quizId) {
        List<Question> questions = questionRepository.findByQuizzes_Id(quizId);
        for (Question question : questions) {
            deleteQuestion(question.getId());
        }
    }

    /*public static void main(String[] args) throws IOException {
        Map<Integer, String> map = new LinkedHashMap<>();
        System.out.println("Độ tương đồng: " + similarity);
    }*/

    @Transactional
    public Map<String, Object> reviewFileJson(MultipartFile file, String courseName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = file.getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            String totalQuestions = rootNode.get("total_questions").asText();
            JsonNode questionNode = rootNode.get("questions");
            //String dataJson = rootNode.toString();

            List<Course> courses = courseService.getAllCourses();
            List<Quiz> quizzes = new ArrayList<>();
            for (Course course : courses) {
                if (course.getQuizzes() != null && !course.getQuizzes().isEmpty()) {
                    quizzes.addAll(course.getQuizzes());
                }
            }

            List<String> existingQuestions = new ArrayList<>();
            /*int numDupQues = 0;
            double minNumOfDupQues = (1.0 / 3) * (Double.parseDouble(totalQuestions));*/
            List<String> dupQueNos = new ArrayList<>();

            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    existingQuestions.addAll(quiz.getQuestions().stream().map(Question::getQuestionText).toList());
                }
            }

            List<Question> questions = objectMapper.convertValue(restoreJsonNode(questionNode), new TypeReference<List<Question>>() {
            });
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                question.setQuestionNo(i + 1);

                if (question.getQuestionText() != null) {
                    if (checkDuplicateQuestion(existingQuestions, question.getQuestionText())) {
                        dupQueNos.add(String.valueOf(i + 1));
                        /*if ((double) numDupQues >= Math.ceil(minNumOfDupQues)) {
                            throw new ObjectAlreadyExistsException("Can not import file! More than 1/3 of the questions in the file are duplicated!");
                        }*/
                    }
                }

                List<AnswerOption> answerOptions = question.getAnswerOptions();
                for (int j = 0; j < answerOptions.size(); j++) {
                    answerOptions.get(j).setOptionLabel(String.valueOf((char) ('A' + j)));
                }
            }

            //Quiz quiz = new Quiz();

            String originalFilenameName = file.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizzes, fileName);
            if (numberOfDuplicateNames != 0) {
                int addPara = numberOfDuplicateNames + 1;
                fileName = fileName + " (" + addPara + ")";
            }

            /*quiz.setDescription("This is an auto-created quiz!");
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            User user = userService.getCurrentUser();
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setCreatedBy(user);
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : questions) {
                quiz.addQuestion(question);
            }

            Course course = courseService.findByName(courseName);
            course.addQuiz(quiz);

            Course savedCourse = courseService.save(course);
            List<Quiz> newestQuizzes = savedCourse.getQuizzes();
            Quiz savedQuiz = newestQuizzes.get(newestQuizzes.size() - 1);

            try {
                SchedulerUtil.startScheduler();

                Scheduler scheduler = SchedulerUtil.getScheduler();
                scheduleJob.scheduleQuizOpenJob(savedQuiz.getId(), savedQuiz.getStartTime());
                scheduleJob.scheduleQuizCloseJob(savedQuiz.getId(), savedQuiz.getEndTime());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }*/

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("Questions", questions);
            //reviewData.put("dataJson", dataJson);
            reviewData.put("fileName", fileName);
            reviewData.put("courseName", courseName);
            reviewData.put("DuplicateQuestionNos", dupQueNos);

            return reviewData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TEST: IMPORT EXCEL BẰNG CÁCH TÁCH HÀM PROCESS EXCEL VỚI HÀM CREATE QUIZ
    @Transactional
    public void importExcelTEST(MultipartFile file, String courseName) throws ObjectAlreadyExistsException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Object> objectQuestions = covertExcelToJsonService.processSheetForImportExcel(sheet);
            List<Question> questions = new ArrayList<>();
            List<Quiz> quizzes = quizRepository.findAll();
            List<String> existingQuestions = new ArrayList<>();

            int numDupQues = 0;
            double minNumOfDupQues = (1.0 / 3) * (objectQuestions.size());
            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    existingQuestions.addAll(quiz.getQuestions().stream().map(Question::getQuestionText).toList());
                }
            }

            for (int i = 0; i < objectQuestions.size(); i++) {
                Map<String, Object> questionData = (Map<String, Object>) objectQuestions.get(i);
                String questionText = (String) questionData.get("questionText");

                if (questionText != null) {
                    if (checkDuplicateQuestion(existingQuestions, questionText)) {
                        numDupQues++;
                        if ((double) numDupQues >= Math.ceil(minNumOfDupQues)) {
                            throw new ObjectAlreadyExistsException("Can not import file! More than 1/3 of the questions in the file are duplicated!");
                        }
                    }
                }

                String questionTypeString = (String) questionData.get("questionType");
                List<Map<String, Object>> answerOptionsMapList = (List<Map<String, Object>>) questionData.get("answerOptions");
                List<AnswerOption> answerOptionEachQuestion = new ArrayList<>();

                for (int j = 0; j < answerOptionsMapList.size(); j++) {
                    String optionText = (String) answerOptionsMapList.get(j).get("optionText");
                    boolean isCorrect = (boolean) answerOptionsMapList.get(j).get("isCorrect");

                    AnswerOption answerOption = new AnswerOption();
                    answerOption.setOptionLabel(String.valueOf((char) (j + 'A')));
                    answerOption.setOptionText(optionText);
                    answerOption.setIsCorrect(isCorrect);

                    answerOptionEachQuestion.add(answerOption);
                }

                if (questionText != null && questionTypeString != null) {
                    QuestionType questionType = setTypeBaseOnStringInput(questionTypeString);
                    Question question = new Question();

                    question.setQuestionNo(i + 1);
                    question.setQuestionText(questionText);
                    question.setQuestionType(questionType);

                    for (AnswerOption answerOption : answerOptionEachQuestion) {
                        question.addAnswerOption(answerOption);
                    }

                    questions.add(question);
                }
            }

            Quiz quiz = new Quiz();

            String originalFilenameName = file.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            if (quizzes != null && !quizzes.isEmpty()) {
                int numberOfDuplicateNames = countDuplicateName(quizzes, fileName);
                if (numberOfDuplicateNames != 0) {
                    int addPara = numberOfDuplicateNames + 1;
                    quiz.setName(fileName + " (" + addPara + ")");
                } else {
                    quiz.setName(fileName);
                }
            } else {
                quiz.setName(fileName);
            }
            quiz.setDescription("This is an auto-created quiz!");
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            User user = userService.getCurrentUser();
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setCreatedBy(user);
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : questions) {
                quiz.addQuestion(question);
            }

            Course course = courseService.findByName(courseName);
            course.addQuiz(quiz);

            Course savedCourse = courseService.save(course);
            List<Quiz> newestQuizzes = savedCourse.getQuizzes();
            Quiz savedQuiz = newestQuizzes.get(newestQuizzes.size() - 1);

            try {
                SchedulerUtil.startScheduler();

                Scheduler scheduler = SchedulerUtil.getScheduler();
                scheduleJob.scheduleQuizOpenJob(savedQuiz.getId(), savedQuiz.getStartTime());
                scheduleJob.scheduleQuizCloseJob(savedQuiz.getId(), savedQuiz.getEndTime());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        } catch (IOException _e_) {
            throw new RuntimeException("Error importing questions from Excel", _e_);
        } catch (ObjectAlreadyExistsException e) {
            throw new ObjectAlreadyExistsException(e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> reviewQuizTEST(MultipartFile file, String courseName) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Object> objectQuestions = covertExcelToJsonService.processSheetForImportExcel(sheet);
            List<Question> questions = new ArrayList<>();
            List<Quiz> quizzes = quizRepository.findAll();
            List<String> existingQuestions = new ArrayList<>();
            List<String> dupQueNos = new ArrayList<>();

            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    existingQuestions.addAll(quiz.getQuestions().stream().map(Question::getQuestionText).toList());
                }
            }

            for (int i = 0; i < objectQuestions.size(); i++) {
                Map<String, Object> questionData = (Map<String, Object>) objectQuestions.get(i);
                String questionText = (String) questionData.get("questionText");

                if (questionText != null) {
                    if (checkDuplicateQuestion(existingQuestions, questionText)) {
                        dupQueNos.add(String.valueOf(i + 1));
                    }
                }

                String questionTypeString = (String) questionData.get("questionType");
                List<Map<String, Object>> answerOptionsMapList = (List<Map<String, Object>>) questionData.get("answerOptions");
                List<AnswerOption> answerOptionEachQuestion = new ArrayList<>();

                for (int j = 0; j < answerOptionsMapList.size(); j++) {
                    String optionText = (String) answerOptionsMapList.get(j).get("optionText");
                    boolean isCorrect = (boolean) answerOptionsMapList.get(j).get("isCorrect");

                    AnswerOption answerOption = new AnswerOption();
                    answerOption.setOptionLabel(String.valueOf((char) (j + 'A')));
                    answerOption.setOptionText(optionText);
                    answerOption.setIsCorrect(isCorrect);

                    answerOptionEachQuestion.add(answerOption);
                }

                if (questionText != null && questionTypeString != null) {
                    QuestionType questionType = setTypeBaseOnStringInput(questionTypeString);
                    Question question = new Question();

                    question.setQuestionNo(i + 1);
                    question.setQuestionText(questionText);
                    question.setQuestionType(questionType);

                    for (AnswerOption answerOption : answerOptionEachQuestion) {
                        question.addAnswerOption(answerOption);
                    }

                    questions.add(question);
                }
            }

            //Đặt tên quiz
            String originalFilenameName = file.getOriginalFilename();
            String fileName = "";
            if (originalFilenameName != null && originalFilenameName.contains(".")) {
                fileName = originalFilenameName.substring(0, originalFilenameName.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizService.findAll(), fileName);
            if (numberOfDuplicateNames != 0) {
                int addPara = numberOfDuplicateNames + 1;
                fileName = fileName + " (" + addPara + ")";
            }

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("Questions", questions);
            //reviewData.put("Correct", correctAnswers);
            reviewData.put("fileName", fileName);
            reviewData.put("courseName", courseName);
            reviewData.put("DuplicateQuestionNos", dupQueNos);

            return reviewData;
        } catch (IOException e) {
            throw new RuntimeException("Error importing questions from Excel", e);
        }
    }

    @Transactional
    public void importWord(MultipartFile file, String courseName) {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {

            List<Question> questions = new ArrayList<>();
            List<String> optionLabelList = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I");

            for (XWPFTable table : document.getTables()) {
                List<XWPFTableRow> rows = table.getRows();
                //     System.out.println("Found table with " + rows.size() + " rows");

                for (int i = 1; i < rows.size(); i++) {
                    List<XWPFTableCell> cells = rows.get(i).getTableCells();
                    if (cells.size() < 3) {
                        //        System.out.println("Skipping row #" + i + ": Not enough columns (less than 3)");
                        continue;
                    }

                    try {
                        String questionNoStr = cells.get(0).getText().trim();
                        String correctAnswer = cells.get(2).getText().trim();

                        XWPFTableCell contentCell = cells.get(1);
                        StringBuilder contentBuilder = new StringBuilder();
                        List<String> paragraphsText = new ArrayList<>();
                        int optionCounter = 0;

                        for (XWPFParagraph paragraph : contentCell.getParagraphs()) {
                            String paragraphText = paragraph.getText().trim();
                            if (paragraphText.isEmpty()) {
                                continue;
                            }

                            if (paragraph.getNumIlvl() != null) {
                                String numFmt = paragraph.getNumFmt();
                                if (numFmt != null && numFmt.equals("upperLetter") && optionCounter < optionLabelList.size()) {
                                    paragraphText = optionLabelList.get(optionCounter) + ". " + paragraphText;
                                    optionCounter++;
                                }
                            }

                            paragraphsText.add(paragraphText);
                            contentBuilder.append(paragraphText).append("\n");
                        }

                        String content = contentBuilder.toString().trim();
                        //System.out.println("Processing row #" + i + ":");
                        //System.out.println("Content: " + content);

                        Pattern pattern = Pattern.compile("A\\s*\\.");
                        Matcher matcher = pattern.matcher(content);

                        if (!matcher.find()) {
                            //System.out.println("Skipping row #" + i + ": No answer options found (no 'A.' detected)");
                            continue;
                        }

                        int startAnswers = matcher.start();
                        String questionText = content.substring(0, startAnswers).trim();
                        String optionsText = content.substring(startAnswers).trim();

                        if (questionText.isEmpty()) {
                            // System.out.println("Skipping row #" + i + ": No valid question text found");
                            continue;
                        }

                        // Tách các đáp án bằng regex
                        //  Pattern optionPattern = Pattern.compile("([A-I])\\s*\\.\\s*([^A-I.][^A-I]*?)(?=(?:[A-I]\\s*\\.|$))");
                        Pattern optionPattern = Pattern.compile("([A-I])\\s*\\.\\s*(.*?)(?=(?:[A-I]\\s*\\.|$))", Pattern.DOTALL);
                        Matcher optionMatcher = optionPattern.matcher(optionsText);


                        List<String> processedOptions = new ArrayList<>();

                        while (optionMatcher.find()) {
                            String optionText = optionMatcher.group(2).trim(); // Lấy nội dung đáp án
                            if (!optionText.isEmpty()) {
                                if (optionText.length() > 255) {
                                    optionText = optionText.substring(0, 252) + "...";
                                }
                                processedOptions.add(optionText);
                                //System.out.println("Found option: " + optionText);
                            }
                        }

                        if (processedOptions.size() < 2) {
                            //System.out.println("Skipping row #" + i + ": Insufficient options (less than 2)");
                            continue;
                        }

                        // Tạo câu hỏi
                        Question question = new Question();
                        question.setQuestionNo(i);
                        question.setQuestionText(questionText);
                        question.setQuestionType(QuestionType.MCQ);
                        question.setPoints(10);

                        // Parse đáp án đúng
                        Set<String> correctAnswers = parseCorrectAnswers(correctAnswer);

                        // Tạo các đáp án
                        for (int j = 0; j < processedOptions.size() && j < optionLabelList.size(); j++) {
                            AnswerOption option = new AnswerOption();
                            option.setOptionLabel(optionLabelList.get(j));
                            option.setOptionText(processedOptions.get(j));
                            option.setIsCorrect(correctAnswers.contains(optionLabelList.get(j)));
                            question.addAnswerOption(option);
                        }

                        questions.add(question);
                        // System.out.println("Successfully added question #" + i);

                    } catch (Exception e) {
                        //System.out.println("Error processing row #" + i + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                break; // Chỉ xử lý bảng đầu tiên để tránh lặp lại
            }

            if (questions.isEmpty()) {
                throw new RuntimeException("No valid questions found in the Word file");
            }

            // Tạo quiz và lưu
            Quiz quiz = new Quiz();
            try {
                Set<Quiz> quizzes = quizRepository.findQuizByCourseName(courseName);
                quiz.setName(generateRandomService.generateRandomName((List<Quiz>) quizzes));
            } catch (Exception e) {
                quiz.setName(generateRandomService.generateRandomName(null));
            }

            quiz.setDescription("This is an auto-created quiz from Word");
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setStartTime(LocalDateTime.now().plusSeconds(1));
            quiz.setEndTime(LocalDateTime.now().plusSeconds(2));
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setCreatedBy(userService.getCurrentUser());
            quiz.setQuizType(Quiz.QuizType.CLOSE);

            for (Question question : questions) {
                quiz.addQuestion(question);
            }

            Course course = courseService.findByName(courseName);
            course.addQuiz(quiz);
            Course savedCourse = courseService.save(course);
            List<Quiz> newestQuizzes = savedCourse.getQuizzes();
            Quiz savedQuiz = newestQuizzes.get(newestQuizzes.size() - 1);

            try {
                SchedulerUtil.startScheduler();
                Scheduler scheduler = SchedulerUtil.getScheduler();
                scheduleJob.scheduleQuizOpenJob(savedQuiz.getId(), savedQuiz.getStartTime());
                scheduleJob.scheduleQuizCloseJob(savedQuiz.getId(), savedQuiz.getEndTime());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error importing questions from Word", e);
        }
    }

    // Phương thức hỗ trợ
    private Set<String> parseCorrectAnswers(String correctAnswer) {
        Set<String> correctAnswers = new HashSet<>();
        if (correctAnswer != null && !correctAnswer.isEmpty()) {
            String[] answers = correctAnswer.split(",");
            for (String answer : answers) {
                correctAnswers.add(answer.trim().toUpperCase());
            }
        }
        return correctAnswers;
    }

    @Transactional
    public Map<String, Object>  reviewImportWord(MultipartFile file, String courseName) {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            List<Question> questions = new ArrayList<>();
            List<String> optionLabelList = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I");
            List<String> existingQuestions = new ArrayList<>();
            List<String> dupQueNos = new ArrayList<>();
            List<String> correctAnswersList = new ArrayList<>(); // Danh sách để lưu đáp án đúng cho từng câu hỏi

            // Lấy danh sách câu hỏi hiện có để kiểm tra trùng
            List<Quiz> quizzes = quizRepository.findAll();
            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    existingQuestions.addAll(quiz.getQuestions().stream()
                            .map(Question::getQuestionText)
                            .toList());
                }
            }
            for (XWPFTable table : document.getTables()) {
                List<XWPFTableRow> rows = table.getRows();

                for (int i = 1; i < rows.size(); i++) {
                    List<XWPFTableCell> cells = rows.get(i).getTableCells();
                    if (cells.size() < 3) {
                        continue;
                    }
                    try {
                        String questionNoStr = cells.get(0).getText().trim();
                        String correctAnswer = cells.get(2).getText().trim();
                        XWPFTableCell contentCell = cells.get(1);
                        StringBuilder contentBuilder = new StringBuilder();
                        int optionCounter = 0;
                        for (XWPFParagraph paragraph : contentCell.getParagraphs()) {
                            String paragraphText = paragraph.getText().trim();
                            if (paragraphText.isEmpty()) {
                                continue;
                            }
                            if (paragraph.getNumIlvl() != null) {
                                String numFmt = paragraph.getNumFmt();
                                if (numFmt != null && numFmt.equals("upperLetter") && optionCounter < optionLabelList.size()) {
                                    paragraphText = optionLabelList.get(optionCounter) + ". " + paragraphText;
                                    optionCounter++;
                                }
                            }
                            contentBuilder.append(paragraphText).append("\n");
                        }
                        String content = contentBuilder.toString().trim();
                        Pattern pattern = Pattern.compile("A\\s*\\.");
                        Matcher matcher = pattern.matcher(content);
                        if (!matcher.find()) {
                            continue;
                        }
                        int startAnswers = matcher.start();
                        String questionText = content.substring(0, startAnswers).trim();
                        String optionsText = content.substring(startAnswers).trim();
                        if (questionText.isEmpty()) {
                            continue;
                        }
                        // Kiểm tra trùng lặp
                        if (checkDuplicateQuestion(existingQuestions, questionText)) {
                            dupQueNos.add(questionNoStr);
                        }
                        Pattern optionPattern = Pattern.compile("([A-I])\\s*\\.\\s*(.*?)(?=(?:[A-I]\\s*\\.|$))", Pattern.DOTALL);
                        Matcher optionMatcher = optionPattern.matcher(optionsText);
                        List<String> processedOptions = new ArrayList<>();
                        while (optionMatcher.find()) {
                            String optionText = optionMatcher.group(2).trim();
                            if (!optionText.isEmpty()) {
                                if (optionText.length() > 255) {
                                    optionText = optionText.substring(0, 252) + "...";
                                }
                                processedOptions.add(optionText);
                            }
                        }
                        if (processedOptions.size() < 2) {
                            continue;
                        }
                        // Tạo câu hỏi để review
                        Question question = new Question();
                        question.setQuestionNo(i);
                        question.setQuestionText(questionText);
                        question.setQuestionType(QuestionType.MCQ);
                        question.setPoints(10);
                        Set<String> correctAnswers = parseCorrectAnswers(correctAnswer);
                        StringBuilder correctAnswerText = new StringBuilder();
                        for (int j = 0; j < processedOptions.size() && j < optionLabelList.size(); j++) {
                            AnswerOption option = new AnswerOption();
                            String optionLabel = optionLabelList.get(j);
                            option.setOptionLabel(optionLabel);
                            option.setOptionText(processedOptions.get(j));
                            boolean isCorrect = correctAnswers.contains(optionLabel);
                            option.setIsCorrect(isCorrect);
                            question.addAnswerOption(option);
                            // Thêm nhãn đáp án đúng vào chuỗi
                            if (isCorrect) {
                                if (correctAnswerText.length() > 0) {
                                    correctAnswerText.append(", ");
                                }
                                correctAnswerText.append(optionLabel);
                            }
                        }
                        questions.add(question);
                        correctAnswersList.add(correctAnswerText.toString()); // Lưu đáp án đúng
                    } catch (Exception e) {
                        continue;
                    }
                }
                break; // Chỉ xử lý bảng đầu tiên
            }
            if (questions.isEmpty()) {
                throw new RuntimeException("No valid questions found in the Word file");
            }
            // Tạo tên quiz
            String originalFilename = file.getOriginalFilename();
            String fileName = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            }
            int numberOfDuplicateNames = countDuplicateName(quizService.findAll(), fileName);
            if (numberOfDuplicateNames != 0) {
                fileName = fileName + " (" + (numberOfDuplicateNames + 1) + ")";
            }
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("Questions", questions);
            reviewData.put("fileName", fileName);
            reviewData.put("courseName", courseName);
            reviewData.put("DuplicateQuestionNos", dupQueNos);
            reviewData.put("CorrectAnswers", correctAnswersList); // Thêm danh sách đáp án đúng
            return reviewData;
        } catch (IOException e) {
            throw new RuntimeException("Error reviewing questions from Word", e);
        }
    }

    private boolean checkDuplicateQuestion(List<String> existingQuestions, String questionText) {
        return existingQuestions.contains(questionText.trim());
    }

    public Set<Question> jsonToQuestionSet(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String questionsJson = objectMapper.readTree(json).get("questions").toString();
            // Deserialize "questions" thành danh sách Question
            Set<Question> questions = objectMapper.readValue(questionsJson, new TypeReference<Set<Question>>() {});
            return questions;
        } catch (Exception e) {
            return null;
        }
    }

    public long count() {
        return questionRepository.count();
    }

//    @Transactional
//    public List<Question> findQuestionsByAssessmentId(Long assessmentId) {
//        List<AssessmentQuestion> assessmentQuestions =
//                assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndex(assessmentId);
//
//        // Chuyển đổi danh sách AssessmentQuestion -> danh sách Question
//        return assessmentQuestions.stream()
//                .map(AssessmentQuestion::getQuestion)
//                .collect(Collectors.toList());
//    }

}
