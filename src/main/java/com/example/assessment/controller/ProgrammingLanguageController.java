package com.example.assessment.controller;



import com.example.assessment.model.ProgrammingLanguage;
import com.example.assessment.service.ProgrammingLanguageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/programming_languages")
public class ProgrammingLanguageController {

    private final ProgrammingLanguageService programmingLanguageService;

    public ProgrammingLanguageController(ProgrammingLanguageService programmingLanguageService) {
        this.programmingLanguageService = programmingLanguageService;
    }

    @GetMapping
    public String listProgrammingLanguages(Model model,
                                @RequestParam(value = "searchQuery", required = false) String searchQuery,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Page<ProgrammingLanguage> programmingLanguages;
        Pageable pageable = PageRequest.of(page, pageSize);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            programmingLanguages = programmingLanguageService.searchProgrammingLanguages(searchQuery, page, pageSize);
        } else {
            programmingLanguages = programmingLanguageService.getProgrammingLanguages(page, pageSize);
        }

        model.addAttribute("programmingLanguages", programmingLanguages.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", programmingLanguages.getTotalPages());
        model.addAttribute("totalItems", programmingLanguages.getTotalElements());
        model.addAttribute("searchQuery", searchQuery);
        // add attribute for layout
        model.addAttribute("content","programmingLanguage/list");
        return "layout";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("programmingLanguage", new ProgrammingLanguage());
        model.addAttribute("programmingLanguages", programmingLanguageService.getAllProgrammingLanguages());
        model.addAttribute("content", "programmingLanguage/create");
        return "layout";
    }

    // Create new programmingLanguage
    @PostMapping("/create")
    public String createProgrammingLanguage(@ModelAttribute ProgrammingLanguage programmingLanguage) {
        programmingLanguageService.saveProgrammingLanguage(programmingLanguage);
        return "redirect:/programming_languages";
    }

    // Show edit form for a specific programmingLanguage
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("programmingLanguage", programmingLanguageService.getProgrammingLanguageById(id));
        model.addAttribute("content", "programmingLanguage/edit");
        return "layout";
    }

    // Update existing programmingLanguage
    @PostMapping("/edit/{id}")
    public String updateProgrammingLanguage(@PathVariable("id") Integer id, @ModelAttribute ProgrammingLanguage programmingLanguage, Model model) {
        programmingLanguage.setId(Long.valueOf(id)); // Ensure the id is set correctly
        programmingLanguageService.saveProgrammingLanguage(programmingLanguage); // Save the programmingLanguage
        return "redirect:/programming_languages";
    }

    // Delete a programmingLanguage
    @GetMapping("/delete/{id}")
    public String deleteProgrammingLanguage(@PathVariable Integer id) {
        programmingLanguageService.deleteProgrammingLanguage(id);
        return "redirect:/programming_languages";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportProgrammingLanguages() {
        // Fetch all roles (page size set to max to get all records)
        List<ProgrammingLanguage> programmingLanguages = programmingLanguageService.getAllProgrammingLanguages();

        // Convert roles to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = programmingLanguageService.exportProgrammingLanguagesToExcel(programmingLanguages);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=programmingLanguages.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    @PostMapping("/import")
    public String importProgrammingLanguages(@RequestParam("file") MultipartFile file) {
        programmingLanguageService.importExcel(file);
        return "redirect:/programming_languages";
    }


    // Print roles page
    @GetMapping("/print")
    public String printProgrammingLanguages(Model model) {
        List<ProgrammingLanguage> programmingLanguages = programmingLanguageService.getAllProgrammingLanguages();
        model.addAttribute("programmingLanguages", programmingLanguages);
        return "programmingLanguage/print";
    }
}
