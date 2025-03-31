package com.example.code_judgement.java_judge;

import com.example.code_judgement.CodeExecutionService;
import com.example.code_judgement.ExecutionResponse;
import com.example.exercise.Exercise;
import com.example.exercise.ExerciseService;
import com.example.testcase.TestCase;
import com.example.testcase.TestCaseResult;
import com.example.testcase.TestCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/judgement/java")
@RequiredArgsConstructor
public class JavaJudgementController {
    private final JavaJudgementService javaJudgementService;
    private final ExerciseService exerciseService;
    private final CodeExecutionService codeExecutionService;
    // Nhận request chạy code (đã được forward từ CodeJudgementController)
    @PostMapping("/run-code")
    public String runCode(@RequestParam("exerciseId") Long exerciseId,
                          @RequestParam("language") String language,
                          @RequestParam("code") String code,
                          Model model) {
        // Lấy bài tập và test cases
        Exercise exercise = exerciseService.getExerciseById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exercise ID"));
        List<TestCase> testCases = exercise.getTestCases();

        if (testCases == null || testCases.isEmpty()) {
            model.addAttribute("output", "No test cases defined for this exercise.");
            model.addAttribute("exercise", exercise);
            model.addAttribute("code", code);
            return "judgement/code_space";
        }
        try{
            ExecutionResponse response = codeExecutionService.executeCodeOptimized(code,testCases);
            // Đưa kết quả vào model để hiển thị trong view
            model.addAttribute("exercise", exercise);
            model.addAttribute("code", code);
            model.addAttribute("passed", response.getPassed());
            model.addAttribute("total", response.getTotal());
            model.addAttribute("testResults", response.getTestCasesResults());
            model.addAttribute("output", response.getPassed() + "/" + response.getTotal() + " test cases passed.");

            return "judgement/code_space";
        }
        catch (Exception e){
            model.addAttribute("output", e.getMessage());
            model.addAttribute("exercise", exercise);
            model.addAttribute("code", code);
            model.addAttribute("language", language);
            return "judgement/code_space";
        }


        //        int passed = 0;
//
//        // Với mỗi test case, thực thi code bằng service chuyên cho Java
//        for (TestCase testCase : testCases) {
//            String userOutput = javaJudgementService.executeCode(code, testCase.getInput());
//            boolean isCorrect = userOutput.trim().equals(testCase.getExpectedOutput().trim());
//            if (isCorrect) {
//                passed++;
//            }
//            testResults.add(new TestCaseResult(testCase, userOutput, isCorrect));
//        }


    }

    @PostMapping("/run-custom-code")
    public String runCustomCode(@RequestParam("exerciseId") Long exerciseId,
                                @RequestParam("code") String code,
                                @RequestParam("customInput") String customInput,
                                Model model) {
        // Lấy bài tập
        Exercise exercise = exerciseService.getExerciseById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exercise ID"));

        // Thực thi mã nguồn với custom input
        String userOutput = codeExecutionService.runWithCusTomInput(code, customInput);


        // Đưa kết quả vào model để hiển thị
        model.addAttribute("exercise", exercise);
        model.addAttribute("code", code);
        model.addAttribute("customOutput", userOutput);
        model.addAttribute("output", "");

        return "judgement/code_space";
    }
}
