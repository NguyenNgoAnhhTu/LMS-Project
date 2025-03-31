package com.example.otp;

import com.example.auth.AuthService;
import com.example.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@SessionAttributes("user")
public class OtpController {
    private final OtpService otpService;
    private final AuthService authService;

    @GetMapping("/otp")
    public String otpPage(@ModelAttribute("user") User user, Model model) {
        model.addAttribute("user", user);
        return "otp"; // Trả về giao diện nhập OTP
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, @ModelAttribute("user") User user, Model model) {
        boolean isOtpValid = otpService.validateOtp(user.getEmail(), otp);
        if (isOtpValid) {
            // OTP hợp lệ -> Đánh dấu người dùng đã được xác minh
            authService.activateUser(user);
            model.asMap().remove("user");
            return "redirect:/login"; // Chuyển hướng đến trang đăng nhập
        } else {
            // OTP không hợp lệ
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "otp"; // Quay lại trang nhập OTP
        }
    }

}
