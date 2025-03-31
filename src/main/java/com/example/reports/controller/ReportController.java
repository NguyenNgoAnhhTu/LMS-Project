package com.example.reports.controller;


import com.example.reports.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService dashboardService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Quizes");
        model.addAttribute("links", "/style.css");
    }

    @GetMapping
    public String showDashboard(Model model) {
        model.addAttribute("totalDepartments", dashboardService.getTotalDepartments());
        model.addAttribute("totalCourses", dashboardService.getTotalCourses());
        // Add more attributes for other reports.

        // add attribute for layout
        model.addAttribute("content","reports/dashboard");

        return "layout";
    }
}

