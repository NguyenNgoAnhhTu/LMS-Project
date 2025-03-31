package com.example.user;

import com.example.role.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    // Get paginated list of users
    @GetMapping()
    public String getUsers(Model model,
                           @RequestParam(name = "page",defaultValue = "0") int page,
                           @RequestParam(name = "size",defaultValue = "10") int size,
                           @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        Page<User> usersPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // If there is a search query, use it to filter the users
            usersPage = userService.searchUsers(searchQuery, page, size);
        } else {
            // If no search query, just get all users with pagination
            usersPage = userService.getUsers(page, size);
        }

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("searchQuery", searchQuery); // Pass search query back to the view
        return "user/list";  // Your view template for displaying users
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.getAllRoles()); // Assuming roleService is a service to fetch all roles
        return "user/create"; // Points to create.html
    }
    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, Model model) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty");
            return "user/create"; // Redirect to create form with error
        }

        try {
            userService.createUser(user);
        }
        catch (Exception e) {
            model.addAttribute("error",  e.getMessage());
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user/create"; // Redirect to create form with error
        }
        model.addAttribute("success", "User created successfully!");
        return "redirect:/users"; // Redirect to users list
    }

    // Show edit form for a specific user
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable(name= "id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("allRoles", roleService.getAllRoles()); // Assuming roleService is a service to fetch all roles
        return "user/edit";
    }

    // Update existing user
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable(name ="id") Long id, @ModelAttribute User user, Model model, Authentication
                             authentication) {
        // You can add validation here if needed
        userService.updateUser(id, user);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                AuthorityUtils.createAuthorityList(String.valueOf(user.getRoles()))
        );

        // Cập nhật SecurityContext
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return "redirect:/users";
    }

    // Delete a user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable(name ="id") Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    // Export users to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportUsers() {
        // Fetch all users (page size set to max to get all records)
        List<User> users = userService.getAllUsers();

        // Convert users to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = userService.exportUsersToExcel(users);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=users.xlsx");
        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    // Import users from Excel
    @PostMapping("/import")
    public String importUsers(@RequestParam("file") MultipartFile file, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> users = userService.importExcel(file);
            userService.saveAll(users);  // Save the users in the database
            redirectAttributes.addFlashAttribute("success", "Users imported successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
//            model.addAttribute("showImportModal", true); // Đánh dấu để mở modal
//            return "users/list"; // Trả về trang hiện tại
        }
        // Redirect to the users list page after import

        return "redirect:/users";
    }

//    @PostMapping("/import")
//    public ResponseEntity<String> importUsers(@RequestParam("file") MultipartFile file) {
//        try {
//            List<User> users = userService.importExcel(file);
//            userService.saveAll(users); // Save users in the database
//            return ResponseEntity.ok("Users imported successfully!");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error importing users: " + e.getMessage());
//        }
//    }

}

