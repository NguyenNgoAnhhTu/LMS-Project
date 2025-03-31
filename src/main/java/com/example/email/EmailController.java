package com.example.email;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
}
