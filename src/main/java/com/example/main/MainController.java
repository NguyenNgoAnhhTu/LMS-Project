package com.example.main;

import com.example.module.Module;
import com.example.module.ModuleService;
import com.example.module_group.ModuleGroup;
import com.example.module_group.ModuleGroupRepository;
import com.example.module_group.ModuleGroupService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.List;

@Controller
@SessionAttributes("moduleGroups") // Specify attributes to store in session
public class MainController {

    @Autowired
    private ModuleService moduleService; // Service to fetch modules

    @Autowired
    private ModuleGroupService moduleGroupService;

    @GetMapping("/")
    public String dashboard(Model model,
                            Authentication authentication, HttpSession session) {
        // Lưu role ban đầu vào session nếu chưa tồn tại
        if (session.getAttribute("originalRole") == null) {
            authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(role -> role.equals("SUPERADMIN"))
                    .findFirst()
                    .ifPresent(role -> session.setAttribute("originalRole", role));
        }
        List<ModuleGroup> moduleGroups = moduleGroupService.getAllModuleGroups(); // Fetch all module groups and their modules
        model.addAttribute("moduleGroups", moduleGroups);

        List<Module> modules = moduleService.findAllModules(); // Fetch modules from the service
        model.addAttribute("modules", modules); // Add modules to the model

        return "dashboard"; // Corresponds to dashboard.html in templates folder
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.getContext().setAuthentication(null); // Clear authentication
        return "redirect:/login"; // Redirect to login page
    }
}
