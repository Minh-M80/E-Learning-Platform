package com.example.E_Learning_Platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendResetToken(String toEmail, String token, long ttlMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("E-Learning Platform - Reset Password");
        message.setText(
                "Ban vua yeu cau dat lai mat khau.\n\n"
                        + "Reset token cua ban la: " + token + "\n\n"
                        + "Token nay co hieu luc trong " + ttlMinutes + " phut.\n"
                        + "Hay mo Swagger va goi API /auth/reset-password voi token nay."
        );

        mailSender.send(message);
    }
}
