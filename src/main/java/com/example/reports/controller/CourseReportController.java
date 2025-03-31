package com.example.reports.controller;

import com.example.course.tag.TagService;
import com.example.reports.service.CourseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/course-reports")
public class CourseReportController {

    @Autowired
    private CourseReportService reportService;

    @Autowired
    private TagService tagService;

    @GetMapping("/overview")
    public String courseOverviewReport(Model model) {
        model.addAttribute("courses", reportService.getAllCourses()); // Add courses to the model
        // add attribute for layout
        model.addAttribute("content","reports/course/courses");

        return "layout";
    }

//    @GetMapping("/course-overview")
//    public String getTotalCourses(Model model) {
//        List<Department> departments = departmentService.getAllDepartments(); // Fetch all departments
//        Map<String, Long> departmentCourseCounts = new HashMap<>();
//
//        // Iterate through each department to calculate the total courses
//        for (Department department : departments) {
//            long totalCourses = department.getCourses() != null ? department.getCourses().size() : 0;
//            departmentCourseCounts.put(department.getName(), totalCourses);
//        }
//
//        model.addAttribute("departmentCourseCounts", departmentCourseCounts);
//        return "report/department/total-courses";
//    }


//    @GetMapping("/completion")
//    public String courseCompletionReport(Model model) {
//        model.addAttribute("courseProgress", reportService.getCourseCompletionData());
//        return "reports/course/course_completion_report";
//    }
//
//
//    @GetMapping("/sessions")
//    public String sessionOverviewReport(Model model) {
//        model.addAttribute("sessions", reportService.getAllSessions());
//        return "reports/course/session_overview_report";
//    }
//
//
//    @GetMapping("/material-usage")
//    public String materialUsageReport(Model model) {
//        model.addAttribute("completions", reportService.getMaterialUsageData());
//        return "reports/course/material_usage_report";
//    }
//
//
//    @GetMapping("/material-type-distribution")
//    public String materialTypeDistributionReport(Model model) {
//        model.addAttribute("materials", reportService.getMaterialTypeDistribution());
//        return "reports/course/material_type_distribution_report";
//    }

    @GetMapping("/tags")
    public String tagReport(Model model) {
        model.addAttribute("tags", tagService.getAllTags());
        return "reports/course/tag";
    }

//    @GetMapping("/instructor-performance")
//    public String instructorPerformanceReport(Model model) {
//        model.addAttribute("instructors", reportService.getInstructorPerformanceData());
//        return "reports/assessment/instructor_performance_report";
//    }
}



