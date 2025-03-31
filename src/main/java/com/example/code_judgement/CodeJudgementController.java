package com.example.code_judgement;

import com.example.exercise.Exercise;
import com.example.exercise.ExerciseService;
import com.example.testcase.TestCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/judgement")
@RequiredArgsConstructor
public class CodeJudgementController {
    private final ExerciseService exerciseService;

    // Hiển thị giao diện code space cho một bài tập
    @GetMapping("/code_space/{id}")
    public String showExercisePlayground(@PathVariable Long id,
                                         Model model) {
        Exercise exercise = exerciseService.getExerciseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exercise ID"));
        model.addAttribute("exercise", exercise);
        model.addAttribute("code", exercise.getSetup() );
        model.addAttribute("output", "");
        return "judgement/code_space";
    }

    // Nhận request chạy code, sau đó chuyển tiếp đến controller xử lý code tương ứng với ngôn ngữ
    @PostMapping("/run-code")
    public String runCode(@RequestParam("exerciseId") Long exerciseId,
                          @RequestParam("code") String code,
                          Model model) {
        // Lấy bài tập và test cases tương ứng
        Exercise exercise = exerciseService.getExerciseById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exercise ID"));
        List<TestCase> testCases = exercise.getTestCases();

        if (testCases == null || testCases.isEmpty()) {
            model.addAttribute("output", "No test cases defined for this exercise.");
            model.addAttribute("exercise", exercise);
            model.addAttribute("code", code);
            return "judgement/code_space";
        }

        // Xây dựng đường dẫn chuyển tiếp dựa trên ngôn ngữ (ví dụ: "java" -> "/judgement/java_judge/run-code")
        String targetPath = "/judgement/" + exercise.getLanguage().getLanguage().toLowerCase() + "/run-code";

        return "forward:" + targetPath;
    }

    @PostMapping("/run-custom-code")
    public String runCustomCode(@RequestParam("exerciseId") Long exerciseId,
                                @RequestParam("code") String code,
                                @RequestParam("customInput") String customInput,
                                Model model) {
        // Lấy bài tập
        Exercise exercise = exerciseService.getExerciseById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exercise ID"));

        // Thực thi mã nguồn với custom input dựa trên ngôn ngữ
       if(customInput == null) {
           model.addAttribute("customOutput", "No custom input defined for this exercise.");
           model.addAttribute("exercise", exercise);
           model.addAttribute("code", code);
           return "judgement/code_space";
       }

        // Đưa kết quả vào model để hiển thị
        model.addAttribute("exercise", exercise);
        model.addAttribute("code", code);
        model.addAttribute("customInput", customInput);
        model.addAttribute("customOutput", "");

        String targetPath = "/judgement/" + exercise.getLanguage().getLanguage().toLowerCase() + "/run-custom-code";
        return "forward:" + targetPath;
    }
}
