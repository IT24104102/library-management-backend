package com.amarakeerthi.userservice.controllers;

import com.amarakeerthi.userservice.dto.ApiResponse;
import com.amarakeerthi.userservice.dto.FirstTimePasswordChangeRequest;
import com.amarakeerthi.userservice.dto.LoginRequest;
import com.amarakeerthi.userservice.dto.LoginResponse;
import com.amarakeerthi.userservice.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        } catch (IllegalArgumentException e) {
            log.warn("Login failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Login failed. Please try again."));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from Bearer header
            String token = extractTokenFromHeader(authHeader);
            boolean isValid = authService.validateToken(token);
            
            return ResponseEntity.ok(ApiResponse.success("Token validation completed", isValid));
        } catch (Exception e) {
            log.error("Token validation error", e);
            return ResponseEntity.ok(ApiResponse.success("Token validation completed", false));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired token"));
            }
            
            var user = authService.getUserFromToken(token);
            
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .mustChangePassword(user.isMustChangePassword())
                .build();
            
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userInfo));
        } catch (Exception e) {
            log.error("Error retrieving user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve profile"));
        }
    }
    
    @PostMapping("/change-password-first-time")
    public ResponseEntity<ApiResponse<Void>> changePasswordFirstTime(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody FirstTimePasswordChangeRequest request) {
        
        try {
            String token = extractTokenFromHeader(authHeader);
            
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired token"));
            }
            
            var user = authService.getUserFromToken(token);
            log.info("First-time password change attempt for user: {}", user.getEmail());
            
            authService.changePasswordFirstTime(user.getId(), request);
            
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully. You can now use your new password to login."));
        } catch (IllegalArgumentException e) {
            log.warn("First-time password change failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during first-time password change", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to change password. Please try again."));
        }
    }
    
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        return authHeader.substring(7);
    }
}