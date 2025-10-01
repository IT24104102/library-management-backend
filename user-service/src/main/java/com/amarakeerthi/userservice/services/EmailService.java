package com.amarakeerthi.userservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@library.com}")
    private String fromEmail;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    
    public boolean sendWelcomeEmail(String toEmail, String fullName, String temporaryPassword, String userRole) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Library Management System");
            
            String emailBody = String.format("""
                Dear %s,
                
                Welcome to the Library Management System!
                
                Your account has been successfully created with the following details:
                - Email: %s
                - Role: %s
                - Temporary Password: %s
                
                Please login to the system using your email and temporary password.
                For security reasons, you will be required to change your password on first login.
                
                If you have any questions, please contact the system administrator.
                
                Best regards,
                Library Management Team
                """, fullName, toEmail, userRole, temporaryPassword);
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Welcome email sent successfully to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", toEmail, e.getMessage());
            return false;
        }
    }
    
    public String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}