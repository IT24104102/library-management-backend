package com.disanayake.borrowservice.services;

import com.disanayake.borrowservice.dto.UserValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.user-service.url}")
    private String userServiceUrl;
    
    public UserValidationResponse validateUser(Long userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            log.info("Validating user with ID: {} at URL: {}", userId, url);
            
            UserValidationResponse response = restTemplate.getForObject(url, UserValidationResponse.class);
            
            if (response != null && response.isSuccess()) {
                log.info("User validation successful for user ID: {}", userId);
                return response;
            } else {
                log.warn("User validation failed for user ID: {}", userId);
                return UserValidationResponse.builder()
                        .success(false)
                        .message("User not found or inactive")
                        .build();
            }
        } catch (RestClientException e) {
            log.error("Error calling user service for user ID: {}", userId, e);
            return UserValidationResponse.builder()
                    .success(false)
                    .message("Unable to validate user: " + e.getMessage())
                    .build();
        }
    }
    
    public boolean isStudent(Long userId) {
        UserValidationResponse response = validateUser(userId);
        return response.isSuccess() && 
               response.getData() != null && 
               "STUDENT".equalsIgnoreCase(response.getData().getRole());
    }
    
    public boolean isLibrarianOrAdmin(Long userId) {
        UserValidationResponse response = validateUser(userId);
        return response.isSuccess() && 
               response.getData() != null && 
               ("LIBRARIAN".equalsIgnoreCase(response.getData().getRole()) || 
                "ADMIN".equalsIgnoreCase(response.getData().getRole()));
    }
}