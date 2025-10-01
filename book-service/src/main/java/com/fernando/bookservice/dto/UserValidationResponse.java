package com.fernando.bookservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserValidationResponse {
    
    private boolean success;
    private String message;
    private UserData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        private Long id;
        private String email;
        private String fullName;
        private String role;
        private boolean isActive;
    }
}