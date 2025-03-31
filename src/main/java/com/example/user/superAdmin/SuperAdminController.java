package com.example.user.superAdmin;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @GetMapping("/switch-role")
    public String showSwitchRolePage(Model model) {
        model.addAttribute("roles", List.of("ADMIN", "INSTRUCTOR", "STUDENT", "SUPERADMIN"));
        return "super_admin/switch-role"; // Tên file giao diện
    }

    @PostMapping("/switch-role")
    public String switchRole(@RequestParam String newRole, HttpSession session,
                             Authentication authentication, Model model) {
        // Kiểm tra role ban đầu trong session
        String originalRole = (String) session.getAttribute("originalRole");

        if (originalRole == null || !originalRole.equals("SUPERADMIN")) {
            model.addAttribute("error", "Bạn không phải là SUPERADMIN, không thể chuyển role.");
            return "super_admin/error"; // Trang thông báo lỗi
        }
        // Tạo Authentication mới với role mới
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                AuthorityUtils.createAuthorityList(newRole)
        );

        // Cập nhật SecurityContext
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // Điều hướng đến giao diện role mới
        return "redirect:/";
    }
    //tạm thời bở
    @Deprecated
    @PostMapping("/return-superadmin")
    public String returnToSuperadmin(Authentication authentication) {
        // Lấy lại quyền SUPERADMIN
        Authentication superAdminAuth = new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                AuthorityUtils.createAuthorityList("SUPERADMIN")
        );

        SecurityContextHolder.getContext().setAuthentication(superAdminAuth);

        // Điều hướng về giao diện SUPERADMIN
        return "redirect:/";
    }
}
