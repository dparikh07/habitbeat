package com.habitbeat.backend.auth.service;

import com.habitbeat.backend.auth.config.EmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getFromAddress());
        message.setTo(to);
        message.setSubject("Verify your Habitbeat account");
        message.setText("Click to verify: http://localhost:8080/auth/verify?token=" + token);
        
        try {
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Email sending failed");
        }
    }
    
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getFromAddress());
        message.setTo(to);
        message.setSubject("Reset your Habitbeat password");
        message.setText("Click to reset password: http://localhost:3000/reset?token=" + token);
        
        try {
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Email sending failed");
        }
    }
}