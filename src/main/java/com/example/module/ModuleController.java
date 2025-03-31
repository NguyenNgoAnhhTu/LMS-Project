package com.example.module;

import com.example.module_group.ModuleGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Modules");
        model.addAttribute("links", "/style.css");
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('ADMIN')")
    public String listModules(Model model,
                              @RequestParam(value = "searchQuery", required = false) String searchQuery,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Page<Module> modules;
        Pageable pageable = PageRequest.of(page, pageSize);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            modules = moduleService.searchModules(searchQuery, pageable);
        } else {
            modules = moduleService.getAllModules(pageable);
        }

        model.addAttribute("modules", modules.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", modules.getTotalPages());
        model.addAttribute("totalItems", modules.getTotalElements());
        model.addAttribute("searchQuery", searchQuery);
        // add attribute for layout
        model.addAttribute("content","modules/list");

        return "layout";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("module", new Module());
        model.addAttribute("moduleGroups", moduleService.getAllModuleGroups());
        model.addAttribute("content", "modules/create");
        return "layout";
    }

    @PostMapping("/create")
    public String createModule(@ModelAttribute Module module) {
        moduleService.saveModule(module);
        return "redirect:/modules";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Module module = moduleService.getModuleById(id).orElse(null);
        List<ModuleGroup> moduleGroups = moduleService.getAllModuleGroups();
        model.addAttribute("module", module);
        model.addAttribute("moduleGroups", moduleGroups);
        model.addAttribute("content", "modules/edit");
        return "layout";
    }

    @PostMapping("/edit/{id}")
    public String updateModule(@PathVariable("id") Long id, @ModelAttribute Module module) {
        module.setId(id);
        moduleService.saveModule(module);
        return "redirect:/modules";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteModule(@PathVariable("id") Long id) {
        moduleService.deleteModule(id);
        return "redirect:/modules";
    }

    // Print roles page
    @GetMapping("/print")
    public String printRoles(Model model) {
        List<Module> modules = moduleService.findAllModules();
        model.addAttribute("modules", modules);
        return "modules/print";
    }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportModules() {
        // Fetch all roles (page size set to max to get all records)
       List<Module> modules = moduleService.findAllModules();

        // Convert roles to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = moduleService.exportModulesToExcel(modules);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=modules.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    @PostMapping("/import")
    public String importModules(@RequestParam("file") MultipartFile file) {
        moduleService.importExcel(file);
        return "redirect:/modules";
    }
}
