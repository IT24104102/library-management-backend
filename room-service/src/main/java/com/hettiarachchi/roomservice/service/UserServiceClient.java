package com.hettiarachchi.roomservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hettiarachchi.roomservice.dto.ApiResponse;
import com.hettiarachchi.roomservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Qualifier("userServiceBaseUrl")
    private final String userServiceBaseUrl;
    
    public UserDto getUserById(Long userId) {
        try {
            String url = userServiceBaseUrl + "/" + userId;
            log.debug("Calling user service: {}", url);
            
            // Use ParameterizedTypeReference to properly deserialize ApiResponse<UserDto>
            ParameterizedTypeReference<ApiResponse<Object>> responseType = 
                new ParameterizedTypeReference<ApiResponse<Object>>() {};
            
            ResponseEntity<ApiResponse<Object>> responseEntity = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                responseType
            );
            
            ApiResponse<Object> response = responseEntity.getBody();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                // Convert the LinkedHashMap to UserDto using ObjectMapper
                Object userData = response.getData();
                UserDto userDto = objectMapper.convertValue(userData, UserDto.class);
                log.debug("Successfully retrieved user: {}", userDto);
                return userDto;
            } else {
                log.warn("User not found or service returned error for userId: {}", userId);
                return null;
            }
        } catch (RestClientException e) {
            log.error("Error calling user service for userId: {}", userId, e);
            return null;
        } catch (Exception e) {
            log.error("Error converting user data for userId: {}", userId, e);
            return null;
        }
    }
    
    public UserDto getUserByEmail(String email) {
        try {
            String url = userServiceBaseUrl + "/email/" + email;
            log.debug("Calling user service: {}", url);
            
            // Use ParameterizedTypeReference to properly deserialize ApiResponse<UserDto>
            ParameterizedTypeReference<ApiResponse<Object>> responseType = 
                new ParameterizedTypeReference<ApiResponse<Object>>() {};
            
            ResponseEntity<ApiResponse<Object>> responseEntity = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                responseType
            );
            
            ApiResponse<Object> response = responseEntity.getBody();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                // Convert the LinkedHashMap to UserDto using ObjectMapper
                Object userData = response.getData();
                UserDto userDto = objectMapper.convertValue(userData, UserDto.class);
                log.debug("Successfully retrieved user by email: {}", userDto);
                return userDto;
            } else {
                log.warn("User not found or service returned error for email: {}", email);
                return null;
            }
        } catch (RestClientException e) {
            log.error("Error calling user service for email: {}", email, e);
            return null;
        } catch (Exception e) {
            log.error("Error converting user data for email: {}", email, e);
            return null;
        }
    }
}