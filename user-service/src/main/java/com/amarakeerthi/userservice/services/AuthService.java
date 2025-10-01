package com.amarakeerthi.userservice.services;

import com.amarakeerthi.userservice.constants.UserStatus;
import com.amarakeerthi.userservice.dto.FirstTimePasswordChangeRequest;
import com.amarakeerthi.userservice.dto.LoginRequest;
import com.amarakeerthi.userservice.dto.LoginResponse;
import com.amarakeerthi.userservice.models.User;
import com.amarakeerthi.userservice.repositories.UserRepository;
import com.amarakeerthi.userservice.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        // Check if user is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active. Please contact administrator.");
        }
        
        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Generate JWT token
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        
        log.info("Successful login for user: {} with role: {}", user.getEmail(), user.getRole());
        
        // Build response
        return LoginResponse.builder()
            .token(accessToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getExpirationTime())
            .user(LoginResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .mustChangePassword(user.isMustChangePassword())
                .build())
            .build();
    }
    
    public boolean validateToken(String token) {
        try {
            // Extract email from token
            String email = jwtUtil.extractEmail(token);
            
            // Find user by email
            User user = userRepository.findByEmail(email)
                .orElse(null);
            
            if (user == null) {
                return false;
            }
            
            // Validate token
            return jwtUtil.validateToken(token, user.getEmail()) && user.getStatus() == UserStatus.ACTIVE;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
    
    public User getUserFromToken(String token) {
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    
    public void changePasswordFirstTime(Long userId, FirstTimePasswordChangeRequest request) {
        log.info("Processing first-time password change for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user must change password
        if (!user.isMustChangePassword()) {
            throw new IllegalArgumentException("User is not required to change password");
        }
        
        // Verify current password (temporary password)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        // Validate new password strength (basic validation)
        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        
        // Update password and remove the requirement to change password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setLastPasswordChange(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        log.info("First-time password changed successfully for user ID: {}", userId);
    }
}