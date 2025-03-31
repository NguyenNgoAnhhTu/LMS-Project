package com.example.tools;

import com.example.course.CourseService;
import com.example.quiz.service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/tools")
public class ToolController {
    private final CovertExcelToJsonService covertExcelToJsonService;
    private final ConvertTxtToJsonService convertTxtToJsonService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuestionService questionService;

    public ToolController(CovertExcelToJsonService covertExcelToJsonService, ConvertTxtToJsonService convertTxtToJsonService) {
        this.covertExcelToJsonService = covertExcelToJsonService;
        this.convertTxtToJsonService = convertTxtToJsonService;
    }

    @GetMapping()
    public String tools(Model model) {
        model.addAttribute("content", "tools/convert_excel");
        return "layout";
    }

    @GetMapping("/convert_txt")
    public String convertTxt() {
        return "tools/convert_txt";
    }

    @PostMapping("/upload_excel")
    public String handleFileUpload(
            @RequestParam("excelFile") List<MultipartFile> files,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (files == null || files.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No files uploaded.");
            return "redirect:/tools/covert_excel";
        }

        try {
            Map<String, Object> rawJsonData = covertExcelToJsonService.convertExcelFilesToJson(files);

            Map<String, String> errorMap = (Map<String, String>) rawJsonData.get("errors");
            rawJsonData.remove("errors");

            Map<String, String> jsonData = new HashMap<>();
            ObjectMapper objectMapper = new ObjectMapper();

            for (Map.Entry<String, Object> entry : rawJsonData.entrySet()) {
                String fileName = entry.getKey();
                Object fileJsonObj = entry.getValue();

                // Convert Map -> JSON String
                String jsonString = objectMapper.writeValueAsString(fileJsonObj);

                // Lưu vào base64Map
                jsonData.put(fileName, jsonString);
            }
            String jsonString = objectMapper.writeValueAsString(jsonData);

            model.addAttribute("jsonData", jsonString);
            model.addAttribute("errorMap", errorMap);

            return "tools/excel_to_json_view";
        } catch (IOException e) {
            model.addAttribute("error", "Error occurred while processing the file: " + e.getMessage());
            return "redirect:/tools/covert_excel";
        }
    }

    @PostMapping("/upload_txt")
    public String handleTxtContentUpload(
            @RequestParam Map<String, String> txtFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (txtFile == null || txtFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No content provided.");
            return "redirect:/tools/convert_txt";
        }
        txtFile.remove("_csrf");

        // Map fileName -> raw text content
        Map<String, String> fileContents = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(1);

        try {
            txtFile.forEach((key, value) -> {
                if (!value.trim().isEmpty()) {
                    String fileName = convertTxtToJsonService.extractFileName(value);
                    if (fileName == null) {
                        fileName = String.format("file_%02d", counter.getAndIncrement());
                    }
                    fileContents.put(fileName, value);
                }
            });

            if (fileContents.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "All fields are empty.");
                return "redirect:/tools/convert_txt";
            }

            Map<String, Object> allFilesJson = convertTxtToJsonService.convertTxtFilesToJson(fileContents);
            Map<String, String> errorMap = (Map<String, String>) allFilesJson.get("errors");
            allFilesJson.remove("errors");

            Map<String, String> jsonData = new HashMap<>();

            ObjectMapper objectMapper = new ObjectMapper();


            for (Map.Entry<String, Object> entry : allFilesJson.entrySet()) {
                String fileName = entry.getKey();
                Object fileJsonObj = entry.getValue();

                // Convert Map -> JSON String
                String jsonString = objectMapper.writeValueAsString(fileJsonObj);

                // Convert JSON String -> byte (UTF-8)
                byte[] jsonBytes = jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                // Encode -> base64
                String base64Json = java.util.Base64.getEncoder().encodeToString(jsonBytes);

                // Lưu vào base64Map
                jsonData.put(fileName, base64Json);
            }
            String jsonString = objectMapper.writeValueAsString(jsonData);

            model.addAttribute("jsonData", jsonString);
            model.addAttribute("errorMap", errorMap);

            return "tools/txt_to_json_view";
        } catch (Exception e) {
            model.addAttribute("error", "Error occurred while processing the file: " + e.getMessage());
            return "redirect:/tools/convert_txt";
        }
    }

    //Generate exam
    @GetMapping("/generate_exam")
    public String showGenerateExamPage(Model model) {
        model.addAttribute("Courses", courseService.getAllCourses());
        model.addAttribute("content", "tools/generate_exam");
        return "layout";
    }

    @PostMapping("/generate_exams")
    public String generateExams(@RequestParam String courseName,
                                @RequestParam int numOfQuizzes,
                                @RequestParam int questionsEachQuiz,
                                Model model){
        model.addAttribute("content", "tools/generate_exam");
        model.addAttribute("Courses", courseService.getAllCourses());
        model.addAttribute("numOfQuizzes", numOfQuizzes);
        model.addAttribute("questionsEachQuiz", questionsEachQuiz);
        model.addAttribute("courseName", courseName);
        try{
            //List<Quiz> exams = generateExamService.generateExams(testCourse().get(1), numOfQuizs, questionsEachQuiz);
            model.addAttribute("Quizzes", questionService.generateQuizzes(courseName, numOfQuizzes, questionsEachQuiz));
            model.addAttribute("Count", numOfQuizzes);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "layout";
    }


}
