package com.example.course;

import com.example.course.material.CourseMaterialDTO;
import com.example.course.material.CourseMaterialService;
import com.example.course.section.Section;
import com.example.course.section.SectionService;
import com.example.user.UserService;
import org.checkerframework.checker.units.qual.A;
import org.springdoc.core.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseMaterialService courseMaterialService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Course Management");
        model.addAttribute("links", "/style.css");
    }

    // Get paginated list of courses
    @GetMapping()
    public String getCourses(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<Course> coursesPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the courses
            coursesPage = courseService.searchCourses(searchQuery, page, size);
        } else {
            // If no search query, just get all courses with pagination
            coursesPage = courseService.getCourses(page, size);
        }

        model.addAttribute("coursesPage", coursesPage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view
        return "course/list";  // Your view template for displaying courses
    }



    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("instructors", userService.getAllInstructor()); // Pass list of instructor
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("courseMaterial", courseMaterialService.findAll());
        return "course/create"; // Points to create.html
    }

    // Create new course
    @PostMapping("/create")
    public String createCourse(@ModelAttribute Course course, Model model) {
        // You can add validation here if needed
        courseService.createCourse(course);
        model.addAttribute("success", "Course created successfully!");
        return "redirect:/courses"; // Redirect to courses list
    }

    // Show edit form for a specific course
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        model.addAttribute("sections", sectionService.getAllSections());

        List<Section> sectionList = sectionService.getSectionsByCourse(id);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Section section : sectionList) {
            Map<String, Object> map = new HashMap<>();
            map.put("sectionId", section.getId());
            map.put("sectionName", section.getName());
            map.put("courseMaterials", section.getCourseMaterials().stream().map(CourseMaterialDTO::toDTO).toList());
            response.add(map);
        }
        model.addAttribute("response", response);
        //model.addAttribute("material", "http://localhost/material/DATH.pdf");
        //model.addAttribute("content", "course/edit");
        return "course/edit";
    }

    // Update existing course
    @PostMapping("/edit/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCourse(
            @PathVariable Long id,
            @ModelAttribute Course course
    ) {
       try {
           // Xử lý logic cập nhật
           return ResponseEntity.ok(courseService.updateCourse(id, course));
       }catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
       }
    }


    // Delete a course
    @GetMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/courses";
    }

    // Print courses page
    @GetMapping("/print")
    public String printCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "course/print";
    }

    // Export courses to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportCourses() {
        // Fetch all courses (page size set to max to get all records)
        List<Course> courses = courseService.getAllCourses();

        // Convert courses to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = courseService.exportCoursesToExcel(courses);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=courses.xlsx");
        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import courses from Excel
    @PostMapping("/import")
    public String importCourses(@RequestParam("file") MultipartFile file) {
        List<Course> courses = courseService.importExcel(file);
        courseService.saveAll(courses);  // Save the courses in the database
        return "redirect:/courses";  // Redirect to the courses list page after import
    }

    @GetMapping("/material")
    public String showMaterials(Model model) {
        //model.addAttribute("material", "/templates/material/Introduction.to.Java.Programming.9th.Edition-(Full-Edition).pdf");
        model.addAttribute("material", "http://localhost/material/Software%20Engineering%2C%2010th%20Edition%20%28%20PDFDrive%20%29.pdf");

        model.addAttribute("content", "course/material");
        return "layout";
    }


}

