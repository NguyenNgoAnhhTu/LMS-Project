package com.example.auth;

import com.example.email.EmailService;
import com.example.otp.OtpService;
import com.example.role.RoleService;
import com.example.user.User;
import com.example.user.UserLoginDTO;
import com.example.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.stream.Collectors;

@RequestMapping("")
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;


    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new UserLoginDTO());
        return "login";
    }

    @PostMapping("/login")
    public String login(Authentication authentication, HttpSession session) {
        // Lưu role ban đầu vào session nếu chưa tồn tại
        if (session.getAttribute("originalRole") == null) {
            authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(role -> role.equals("SUPERADMIN"))
                    .findFirst()
                    .ifPresent(role -> session.setAttribute("originalRole", role));
        }
        return "login";
    }


    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, Model model, RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }
        // Tạo mã OTP ngẫu nhiên
        otpService.generateOtp(user.getEmail(), user);

//        // Gửi OTP qua email
//        emailService.sendEmail(user.getEmail(), "Your OTP Code", "Your OTP is: " + otp);
        redirectAttributes.addFlashAttribute("user", user);
        return "redirect:/otp";
    }

    @GetMapping("/profile")
    public String userProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        //String roles = authentication.getAuthorities().toString();


        String roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        model.addAttribute("roles", roles);
        model.addAttribute("username", username);
        return "profile";  // Trả về trang hiển thị thông tin người dùng
    }

}