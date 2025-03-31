package com.example.group.controller;

import com.example.department.Department;
import com.example.department.DepartmentService;
import com.example.group.model.Group;
import com.example.group.service.GroupService;
import com.example.user.User;
import com.example.user.UserService;
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
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    // Thêm model attribute chung cho tất cả các phương thức
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("title", "Groups");
        model.addAttribute("links", "/style.css");
    }

    @GetMapping
    public String list(Model model,
                              @RequestParam(value = "searchQuery", required = false) String searchQuery,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Page<Group> groups;
        Pageable pageable = PageRequest.of(page, pageSize);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            groups = groupService.search(searchQuery, pageable);
        } else {
            groups = groupService.findAll(pageable);
        }

        model.addAttribute("groups", groups.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", groups.getTotalPages());
        model.addAttribute("totalItems", groups.getTotalElements());
        model.addAttribute("searchQuery", searchQuery);
        // add attribute for layout
        model.addAttribute("content","groups/list");

        return "layout";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // Add a new Group object to the model
        model.addAttribute("group", new Group());

        // Add all departments to the model for selection in the form
        model.addAttribute("departments", departmentService.getAllDepartments());

        // Get the currently logged-in user (assuming you have a method for this)
        User currentUser = groupService.getCurrentUser();

        // Add all users to the model for selection in the form
        model.addAttribute("users", userService.getAllUsers());

        // Add the current user to the model (so it can be set as the creator)
        model.addAttribute("currentUser", currentUser);

        // Specify which content view to render within the layout
        model.addAttribute("content", "groups/create");

        return "layout";
    }


    @PostMapping("/create")
    public String create(@ModelAttribute Group group) {
        groupService.save(group);
        return "redirect:/groups";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Group group = groupService.findById(id).orElse(null);
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("group", group);
        model.addAttribute("departments", departments);

        // Get the currently logged-in user (assuming you have a method for this)
        User currentUser = groupService.getCurrentUser();

        // Add all users to the model for selection in the form
        model.addAttribute("users", userService.getAllUsers());

        // Add the current user to the model (so it can be set as the creator)
        model.addAttribute("currentUser", currentUser);

        model.addAttribute("content", "groups/edit");
        return "layout";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute Group group) {
        group.setId(id);
        groupService.save(group);
        return "redirect:/groups";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String delete(@PathVariable("id") Long id) {
        groupService.deleteById(id);
        return "redirect:/groups";
    }

    // Print roles page
    @GetMapping("/print")
    public String print(Model model) {
        List<Group> groups = groupService.findAll();
        model.addAttribute("groups", groups);
        return "groups/print";
    }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        // Fetch all roles (page size set to max to get all records)
        List<Group> groups = groupService.findAll();

        // Convert roles to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = groupService.exportToExcel(groups);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=groups.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        groupService.importExcel(file);
        return "redirect:/groups";
    }
}
