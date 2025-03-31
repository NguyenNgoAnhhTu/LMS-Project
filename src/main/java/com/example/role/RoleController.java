package com.example.role;

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
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // Get paginated list of roles
    @GetMapping()
    public String getRoles(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<Role> rolesPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the roles
            rolesPage = roleService.searchRoles(searchQuery, page, size);
        } else {
            // If no search query, just get all roles with pagination
            rolesPage = roleService.getRoles(page, size);
        }

        model.addAttribute("rolesPage", rolesPage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view
        return "role/list";  // your view template for displaying roles
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("role", new Role());
        return "role/create"; // Points to create.html
    }

    // Create new role
    @PostMapping("/create")
    public String createRole(@ModelAttribute Role role, Model model) {
        if (roleService.isRoleNameExists(role.getName())) {
            model.addAttribute("error", "Role name already exists!");
            return "role/create"; // Redisplay the create form with the error message
        }
        roleService.createRole(role.getName());
        model.addAttribute("success", "Role created successfully!");
        return "redirect:/roles"; // Redirect to roles list
    }

    // Show edit form for a specific role
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("role", roleService.getRoleById(id));
        return "role/edit";
    }

    // Update existing role
    @PostMapping("/edit/{id}")
    public String updateRole(@PathVariable Integer id, @ModelAttribute Role role, Model model) {
        if (roleService.isRoleNameExists(role.getName())) {
            model.addAttribute("error", "Role name already exists!");
            return "role/edit"; // Ensure this is the correct view name
        }
        roleService.updateRole(id, role.getName());
        return "redirect:/roles";
    }

    // Delete a role
    @GetMapping("/delete/{id}")
    public String deleteRole(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("success", "Role deleted successfully!");
        } catch (IllegalArgumentException e) {
            // Handle exception
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/roles";
    }

    // Print roles page
    @GetMapping("/print")
    public String printRoles(Model model) {
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);
        return "role/print";
    }

    // Export roles to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportRoles() {
        // Fetch all roles (page size set to max to get all records)
        Page<Role> rolesPage = roleService.getRoles(0, Integer.MAX_VALUE);

        // Convert roles to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = roleService.exportRolesToExcel(rolesPage.getContent());

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=roles.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import roles from Excel
    @PostMapping("/import")
    public String importRoles(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        List<Role> roles = null;
        try {
            roles = roleService.importExcel(file);
            roleService.saveAll(roles);
            redirectAttributes.addFlashAttribute("success", "Import successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
          // Save the roles in the database
        return "redirect:/roles";  // Redirect to the roles list page after import
    }
}
