package com.example.otp;
import com.example.email.EmailService;
import com.example.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final Map<String, String> otpStorage = new HashMap<>(); // Lưu trữ OTP của từng email
    private final Map<String, Long> otpTimestamps = new HashMap<>(); // Lưu thời gian gửi OTP cuối cùng
    private final EmailService emailService;
    public void generateOtp(String email, User user) {
        long currentTime = System.currentTimeMillis();
        // Kiểm tra khoảng thời gian giữa hai lần gửi OTP 30s
        if (otpTimestamps.containsKey(email) && currentTime - otpTimestamps.get(email) < 30 * 1000) {
            return;
        }

        // Tạo OTP ngẫu nhiên (6 chữ số)
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        otpTimestamps.put(email, currentTime);

        // Mã OTP hết hạn sau 2 phút
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                otpStorage.remove(email);
                otpTimestamps.remove(email);
            }
        }, 2 * 60 * 1000);

        // Gửi OTP qua email
        String subject = "Your OTP Code for Account Registration";
        String body = "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f9; color: #333; padding: 20px; }"
                + ".container { width: 100%; max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; padding: 20px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }"
                + ".header { text-align: center; color: #007bff; }"
                + ".content { margin-top: 20px; font-size: 16px; color: #000000; }"
                + ".otp-code { font-size: 24px; font-weight: bold; color: #000000; background-color: #f0f0f0; padding: 10px; border-radius: 4px; }"
                + ".footer { margin-top: 30px; text-align: left; font-size: 14px; color: #888; }" // Kích thước chữ tăng lên và căn lề trái
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Welcome to FPT LMS!</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Hello,</p>"
                + "<p>Thank you for registering with us. To complete your account registration, please use the OTP code below:</p>"
                + "<p class='otp-code'>" + otp + "</p>"
                + "<p>This OTP is valid for 2 minutes. Please enter it on the registration page to complete your sign-up.</p>"
                + "<p>If you did not request this OTP, please ignore this email.</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>If you have any questions, feel free to contact our support team.</p>"
                + "<p>PhucTH50@fpt.com - Senior IT</p>"
                + "<p>Thank you!</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";



        emailService.sendEmail(email, subject, body);
    }

    public boolean validateOtp(String email, String otp) {
        return otp.equals(otpStorage.get(email));
    }
}
