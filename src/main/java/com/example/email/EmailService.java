package com.example.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);  // true indicates HTML content
            helper.setFrom("your-email@example.com");  // Replace with your email

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public void sendEmailRegister(String toEmail) {
//        try {
//            String body =
//                    "<html lang=\"en\">\n" +
//                    "<head>\n" +
//                    "    <meta charset=\"UTF-8\">\n" +
//                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
//                    "    <title>Verify Your Account</title>\n" +
//                    "    <style>\n" +
//                    "        body {\n" +
//                    "            font-family: Arial, sans-serif;\n" +
//                    "            background-color: #f9f9f9;\n" +
//                    "            margin: 0;\n" +
//                    "            padding: 0;\n" +
//                    "        }\n" +
//                    "        .email-container {\n" +
//                    "            max-width: 600px;\n" +
//                    "            margin: 20px auto;\n" +
//                    "            background-color: #ffffff;\n" +
//                    "            border: 1px solid #dddddd;\n" +
//                    "            border-radius: 8px;\n" +
//                    "            overflow: hidden;\n" +
//                    "        }\n" +
//                    "        .header {\n" +
//                    "            background-color: #4CAF50;\n" +
//                    "            color: #ffffff;\n" +
//                    "            padding: 20px;\n" +
//                    "            text-align: center;\n" +
//                    "        }\n" +
//                    "        .content {\n" +
//                    "            padding: 20px;\n" +
//                    "            color: #333333;\n" +
//                    "        }\n" +
//                    "        .otp-code {\n" +
//                    "            display: inline-block;\n" +
//                    "            font-size: 24px;\n" +
//                    "            font-weight: bold;\n" +
//                    "            color: #4CAF50;\n" +
//                    "            background-color: #f4f4f4;\n" +
//                    "            padding: 10px 20px;\n" +
//                    "            border-radius: 5px;\n" +
//                    "            margin: 20px 0;\n" +
//                    "        }\n" +
//                    "        .footer {\n" +
//                    "            background-color: #f4f4f4;\n" +
//                    "            color: #888888;\n" +
//                    "            padding: 10px;\n" +
//                    "            text-align: center;\n" +
//                    "            font-size: 12px;\n" +
//                    "        }\n" +
//                    "        a {\n" +
//                    "            color: #4CAF50;\n" +
//                    "            text-decoration: none;\n" +
//                    "        }\n" +
//                    "    </style>\n" +
//                    "</head>\n" +
//                    "<body>\n" +
//                    "    <div class=\"email-container\">\n" +
//                    "        <div class=\"header\">\n" +
//                    "            <h1>Verify Your Account</h1>\n" +
//                    "        </div>\n" +
//                    "        <div class=\"content\">\n" +
//                    "            <p>Dear [User],</p>\n" +
//                    "            <p>Thank you for signing up! To complete your registration, please use the OTP code below:</p>\n" +
//                    "            <div class=\"otp-code\">123456</div>\n" +
//                    "            <p>The OTP code is valid for <strong>10 minutes</strong>. If you did not request this, please ignore this email.</p>\n" +
//                    "            <p>If you have any questions, feel free to contact our support team.</p>\n" +
//                    "            <p>Best regards,</p>\n" +
//                    "            <p>The [Your Company] Team</p>\n" +
//                    "        </div>\n" +
//                    "        <div class=\"footer\">\n" +
//                    "            <p>&copy; 2025 [Your Company]. All rights reserved.</p>\n" +
//                    "            <p><a href=\"[Your Website URL]\">Visit our website</a></p>\n" +
//                    "        </div>\n" +
//                    "    </div>\n" +
//                    "</body>\n" +
//                    "</html>\n";
//            Email email = new Email();
//            email.setToEmail(toEmail);
//            email.setSubject("");
//            email.setBody(body);
//            sendEmail(email);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
