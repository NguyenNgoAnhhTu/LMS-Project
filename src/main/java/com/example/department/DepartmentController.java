package com.example.department;

import com.example.exception.*;
import com.example.course.CourseService;
import com.example.location.LocationService;
import com.example.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Modules");
        model.addAttribute("links", "/style.css");
    }

    // Get paginated list of departments
    @GetMapping()
    public String getDepartments(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<Department> departmentsPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the departments
            departmentsPage = departmentService.searchDepartments(searchQuery, page, size);
        } else {
            // If no search query, just get all departments with pagination
            departmentsPage = departmentService.getDepartments(page, size);
        }

        model.addAttribute("departmentsPage", departmentsPage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view
        // add attribute for layout
        model.addAttribute("content","department/list");

        return "layout";
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("locations", locationService.findAll());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("content", "department/create");
        return "layout";
    }

    // Create new department
    @PostMapping("/create")
    public String create(@ModelAttribute Department department,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            departmentService.createDepartment(department);
            redirectAttributes.addFlashAttribute("success", "Department created successfully!");
            return "redirect:/departments";
        } catch (ObjectAlreadyExistsException e) {
            // Add all necessary attributes for the form
            model.addAttribute("error", e.getMessage());
            model.addAttribute("department", department); // Keep the submitted data
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("locations", locationService.findAll());
            model.addAttribute("courses", courseService.getAllCourses());
            model.addAttribute("content", "department/create");
            return "layout";
        }
    }

    // Show edit form for a specific department
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("department", departmentService.getDepartmentById(id));
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("locations", locationService.findAll());
        model.addAttribute("courses", courseService.getAllCourses());

        model.addAttribute("content", "department/edit");
        return "layout";
    }

    // Update existing department
    @PostMapping("/edit/{id}")
    public String updateDepartment(@PathVariable Long id, @ModelAttribute Department department, Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            departmentService.updateDepartment(id, department);
            redirectAttributes.addFlashAttribute("success", "Department updated successfully!");
            return "redirect:/departments";
        } catch (ObjectAlreadyExistsException e) {
            // Add all necessary attributes for the form
            model.addAttribute("error", e.getMessage());
            model.addAttribute("department", department); // Keep the submitted data
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("locations", locationService.findAll());
            model.addAttribute("courses", courseService.getAllCourses());

            model.addAttribute("content", "department/edit");
            return "layout";
        }
    }

    // Delete a department
    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return "redirect:/departments";
    }

    // Print departments page
    @GetMapping("/print")
    public String printDepartments(Model model) {
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);
        return "department/print";
    }

    // Export departments to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportDepartments() {
        // Fetch all departments (page size set to max to get all records)
        List<Department> departments = departmentService.getAllDepartments();

        // Convert departments to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = departmentService.exportDepartmentsToExcel(departments);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=departments.xlsx");
        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import departments from Excel
    @PostMapping("/import")
    public String importDepartments(@RequestParam("file") MultipartFile file) {
        List<Department> departments = departmentService.importExcel(file);
        departmentService.saveAll(departments);  // Save the departments in the database
        return "redirect:/departments";  // Redirect to the departments list page after import
    }
}