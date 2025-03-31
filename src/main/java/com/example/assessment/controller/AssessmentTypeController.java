package com.example.assessment.controller;

import com.example.assessment.model.AssessmentType;
import com.example.assessment.service.AssessmentTypeService;
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
import java.util.List;

@Controller
@RequestMapping("/ass-types")
public class AssessmentTypeController {

    @Autowired
    private AssessmentTypeService assessmentTypeService;

    // Get paginated list of assessmentTypes
    @GetMapping()
    public String getAssessmentType(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<AssessmentType> assessmentTypePage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the assessmentTypes
            assessmentTypePage = assessmentTypeService.searchAssessmentTypes(searchQuery, page, size);
        } else {
            // If no search query, just get all assessmentTypes with pagination
            assessmentTypePage = assessmentTypeService.getAssessmentTypes(page, size);
        }

        model.addAttribute("AssessmentTypePage", assessmentTypePage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view
        model.addAttribute("content","assessment_type/list");
        return "layout"; // your view template for displaying assessmentTypes
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("assessment_type", new AssessmentType());
        model.addAttribute("content","assessment_type/create");
        return "layout"; // your view template for displaying assessmentTypes
    }

    // Create new assessment_type
    @PostMapping("/create")
    public String createAssessmentType(@ModelAttribute AssessmentType assessment_type, Model model) {
        if (assessmentTypeService.isAssessmentTypeNameExists(assessment_type.getName())) {
            model.addAttribute("error", "AssessmentType name already exists!");
            return "assessment_type/create"; // Ensure this is the correct view name
        }
        assessmentTypeService.createAssessmentType(assessment_type.getName());
        return "redirect:/ass-types"; // Ensure this is the correct view name
    }


    // Show edit form for a specific assessment_type
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("assessment_type", assessmentTypeService.getAssessmentTypeById(id));
        model.addAttribute("content","assessment_type/edit");
        return "layout"; // your view template for displaying assessmentTypes
    }

    // Update existing assessment_type
    @PostMapping("/edit/{id}")
    public String updateAssessmentType(@PathVariable Integer id, @ModelAttribute AssessmentType assessment_type, Model model) {
        if (assessmentTypeService.isAssessmentTypeNameExists(assessment_type.getName())) {
            model.addAttribute("error", "AssessmentType name already exists!");
            return "assessment_type/edit"; // Ensure this is the correct view name
        }
        assessmentTypeService.updateAssessmentType(id, assessment_type.getName());
        return "redirect:/ass-types";
    }

    // Delete a assessment_type
    @GetMapping("/delete/{id}")
    public String deleteAssessmentType(@PathVariable Integer id) {
        assessmentTypeService.deleteAssessmentType(id);
        return "redirect:/ass-types";
    }

    // Export assessmentTypes to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportAssessmentTypes() {
        // Fetch all assessmentTypes (page size set to max to get all records)
        Page<AssessmentType> AssessmentTypePage = assessmentTypeService.getAssessmentTypes(0, Integer.MAX_VALUE);

        // Convert assessmentTypes to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = assessmentTypeService.exportAssessmentTypesToExcel(AssessmentTypePage.getContent());

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=assessmentTypes.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import assessmentTypes from Excel
    @PostMapping("/import")
    public String importAssessmentTypes(@RequestParam("file") MultipartFile file) {
        List<AssessmentType> assessmentTypes = assessmentTypeService.importExcel(file);
        assessmentTypeService.saveAll(assessmentTypes);  // Save the assessmentTypes in the database
        return "redirect:/ass-types";  // Redirect to the assessmentTypes list page after import
    }

    // Print roles page
    @GetMapping("/print")
    public String printRoles(Model model) {
        List<AssessmentType> assessmentTypes = assessmentTypeService.getAllAssessmentTypes();
        model.addAttribute("assessmentTypes", assessmentTypes);
        return "assessment_type/print";
    }
}
