package com.disanayake.borrowservice.dto;

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
        private String username;
        private String email;
        private String role;
        private String status;
    }
}