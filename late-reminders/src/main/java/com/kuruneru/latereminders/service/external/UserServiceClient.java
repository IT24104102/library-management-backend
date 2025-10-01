package com.kuruneru.latereminders.service.external;

import com.kuruneru.latereminders.dto.ApiResponse;
import com.kuruneru.latereminders.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${library-reminders.services.user-service-url}")
    private String userServiceUrl;
    
    /**
     * Get user details by user ID
     */
    public Optional<UserDto> getUserById(Long userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            log.debug("Fetching user details from: {}", url);
            
            ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<UserDto>>() {}
            );
            
            if (response.getBody() != null && response.getBody().isSuccess()) {
                UserDto user = response.getBody().getData();
                log.debug("Successfully fetched user details for ID: {}", userId);
                return Optional.ofNullable(user);
            } else {
                log.warn("Failed to fetch user details for ID {}: {}", userId,
                    response.getBody() != null ? response.getBody().getMessage() : "No response body");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error fetching user details for ID {} from user service", userId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Get user email by user ID
     */
    public Optional<String> getUserEmail(Long userId) {
        return getUserById(userId).map(UserDto::getEmail);
    }
    
    /**
     * Check if user exists and has valid email
     */
    public boolean isValidUserWithEmail(Long userId) {
        Optional<UserDto> user = getUserById(userId);
        return user.isPresent() && 
               user.get().getEmail() != null && 
               !user.get().getEmail().trim().isEmpty() &&
               user.get().getEmail().contains("@");
    }
}