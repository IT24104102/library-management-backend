package com.amarakeerthi.userservice.dto;

import com.amarakeerthi.userservice.constants.UserRole;
import com.amarakeerthi.userservice.constants.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String email;
        private UserRole role;
        private UserStatus status;
        private boolean mustChangePassword;
    }
}