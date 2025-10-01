package com.kuruneru.latereminders.service.external;

import com.kuruneru.latereminders.dto.ApiResponse;
import com.kuruneru.latereminders.dto.LoanDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${library-reminders.services.borrow-service-url}")
    private String borrowServiceUrl;
    
    /**
     * Get all active loans from borrow service
     */
    public List<LoanDto> getActiveLoans() {
        try {
            String url = borrowServiceUrl + "/api/loans?status=ACTIVE&size=1000";
            log.debug("Fetching active loans from: {}", url);
            
            ResponseEntity<ApiResponse<List<LoanDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<LoanDto>>>() {}
            );
            
            if (response.getBody() != null && response.getBody().isSuccess()) {
                List<LoanDto> loans = response.getBody().getData();
                log.info("Successfully fetched {} active loans", loans != null ? loans.size() : 0);
                return loans != null ? loans : List.of();
            } else {
                log.warn("Failed to fetch active loans: {}", 
                    response.getBody() != null ? response.getBody().getMessage() : "No response body");
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error fetching active loans from borrow service", e);
            return List.of();
        }
    }
    
    /**
     * Get overdue loans from borrow service
     */
    public List<LoanDto> getOverdueLoans() {
        try {
            String url = borrowServiceUrl + "/api/loans/overdue";
            log.debug("Fetching overdue loans from: {}", url);
            
            ResponseEntity<ApiResponse<List<LoanDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<LoanDto>>>() {}
            );
            
            if (response.getBody() != null && response.getBody().isSuccess()) {
                List<LoanDto> loans = response.getBody().getData();
                log.info("Successfully fetched {} overdue loans", loans != null ? loans.size() : 0);
                return loans != null ? loans : List.of();
            } else {
                log.warn("Failed to fetch overdue loans: {}", 
                    response.getBody() != null ? response.getBody().getMessage() : "No response body");
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error fetching overdue loans from borrow service", e);
            return List.of();
        }
    }
    
    /**
     * Get loans due tomorrow
     */
    public List<LoanDto> getLoansDueTomorrow() {
        try {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            String tomorrowDate = tomorrow.toLocalDate().toString();
            
            String url = borrowServiceUrl + "/api/loans?dueDate=" + tomorrowDate + "&size=1000";
            log.debug("Fetching loans due tomorrow from: {}", url);
            
            ResponseEntity<ApiResponse<List<LoanDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<LoanDto>>>() {}
            );
            
            if (response.getBody() != null && response.getBody().isSuccess()) {
                List<LoanDto> loans = response.getBody().getData();
                log.info("Successfully fetched {} loans due tomorrow", loans != null ? loans.size() : 0);
                return loans != null ? loans : List.of();
            } else {
                log.warn("Failed to fetch loans due tomorrow: {}", 
                    response.getBody() != null ? response.getBody().getMessage() : "No response body");
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error fetching loans due tomorrow from borrow service", e);
            return List.of();
        }
    }
}