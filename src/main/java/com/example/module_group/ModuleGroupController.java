package com.example.module_group;

import com.example.exception.ObjectAlreadyExistsException;
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
@RequestMapping("/module-groups")
public class ModuleGroupController {

    @Autowired
    private ModuleGroupService moduleGroupService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Module Group");
        model.addAttribute("links", "/style.css");
    }

    @GetMapping()
    public String getAllModuleGroups(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<ModuleGroup> moduleGroupPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the assessmentTypes
            moduleGroupPage = moduleGroupService.searchModuleGroups(searchQuery, page, size);
        } else {
            // If no search query, just get all assessmentTypes with pagination
            moduleGroupPage = moduleGroupService.getModuleGroups(page, size);
        }

        model.addAttribute("moduleGroupPage", moduleGroupPage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view

        // add attribute for layout
        model.addAttribute("content","module_group/list");
        addCommonAttributes(model);
        return "layout";
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("moduleGroup", new ModuleGroup());

        model.addAttribute("content", "module_group/create");
        addCommonAttributes(model);
        return "layout";
    }

    @PostMapping("/create")
    public String createModuleGroup(@ModelAttribute ModuleGroup moduleGroup, Model model) {
        try {
            moduleGroupService.createModuleGroup(moduleGroup.getName());
            return "redirect:/module-groups"; // Ensure this is the correct view name
        } catch (ObjectAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("content", "module_group/create");
            addCommonAttributes(model);
            return "layout";
        }
    }

	// Show edit form for a specific assessment_type
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("moduleGroup", moduleGroupService.getModuleGroupById(id));

        model.addAttribute("content", "module_group/edit");
        addCommonAttributes(model);
        return "layout";
    }

    // Update existing assessment_type
    @PostMapping("/edit/{id}")
    public String updateModuleGroup(@PathVariable Long id, @ModelAttribute ModuleGroup moduleGroup, Model model) {
        try {
            moduleGroupService.updateModuleGroup(id, moduleGroup.getName());
            return "redirect:/module-groups";
        } catch (ObjectAlreadyExistsException e) {
            model.addAttribute("error", "Module Group name already exists!");
            model.addAttribute("content", "module_group/edit");
            addCommonAttributes(model);
            return "layout";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteModuleGroup(@PathVariable Long id) {
        moduleGroupService.deleteModuleGroup((long) Math.toIntExact(id));
        return "redirect:/module-groups";
    }

    // Export moduleGroups to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportModuleGroups() {
        // Fetch all moduleGroups (page size set to max to get all records)
        List<ModuleGroup> moduleGroups = moduleGroupService.getAllModuleGroups();

        // Convert moduleGroups to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = moduleGroupService.exportModuleGroupsToExcel(moduleGroups);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=moduleGroups.xlsx");
        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import module-groups from Excel
    @PostMapping("/import")
    public String importModuleGroups(@RequestParam("file") MultipartFile file) {
        List<ModuleGroup> moduleGroups = moduleGroupService.importExcel(file);
        moduleGroupService.saveAll(moduleGroups);  // Save the moduleGroups in the database
        return "redirect:/module-groups";  // Redirect to the moduleGroups list page after import
    }

    // print module-group
    @GetMapping("/print")
    public String printModuleGroup(Model model){
        List<ModuleGroup> moduleGroupList = moduleGroupService.getAllModuleGroups();
        model.addAttribute("moduleGroupList", moduleGroupList);
        return "module_group/print";
    }
}
