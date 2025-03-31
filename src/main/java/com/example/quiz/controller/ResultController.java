package com.example.quiz.controller;

import com.example.quiz.model.Result;
import com.example.quiz.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/results")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping
    public String list(Model model) {
        List<Result> results = resultService.findAll();
        model.addAttribute("results", results);
        model.addAttribute("content", "quizes/results");
        return "layout";
    }

    @GetMapping("/result-detail/{id}")
    public String viewDetail(@PathVariable("id") Long id, Model model) {
        Result result = resultService.findById(id).orElse(null);

        if (result == null) {
            return "redirect:/quizes/results";
        }

        model.addAttribute("result", result);
        model.addAttribute("content", "quizes/result-detail");
        return "layout";
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("result", new Result());
        model.addAttribute("content", "quizes/result-create");
        return "layout";
    }

    @PostMapping("/create")
    public String submitResult(@ModelAttribute Result result) {
        resultService.createResult(result);
        return "redirect:/quizes/results";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Result result = resultService.findById(id).orElse(null);
        model.addAttribute("result", result);
        model.addAttribute("content", "quizes/result-edit");
        return "layout";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute Result result) {
        resultService.update(id, result);
        return "redirect:/quizes/results";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        resultService.deleteById(id);
        return "redirect:/quizes/results";
    }
}
