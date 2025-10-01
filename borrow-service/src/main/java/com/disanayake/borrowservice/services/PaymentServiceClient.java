package com.disanayake.borrowservice.services;

import com.disanayake.borrowservice.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8084/api/payments";
    private static final double FINE_PER_DAY = 1.0;
    
    public void createOverdueFine(Long userId, Long borrowRecordId, String bookIsbn, LocalDate dueDate) {
        try {
            log.info("Creating overdue fine for user {} and borrow record {}", userId, borrowRecordId);
            
            // Calculate fine amount based on days overdue
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            BigDecimal fineAmount = BigDecimal.valueOf(daysOverdue * FINE_PER_DAY);
            
            CreateFineRequestDto request = CreateFineRequestDto.builder()
                    .userId(userId)
                    .borrowRecordId(borrowRecordId)
                    .bookIsbn(bookIsbn)
                    .type("OVERDUE")
                    .amount(fineAmount)
                    .description("Overdue fine for " + daysOverdue + " days")
                    .notes("Automatically generated overdue fine")
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateFineRequestDto> entity = new HttpEntity<>(request, headers);
            
            restTemplate.postForObject(PAYMENT_SERVICE_URL + "/fines", entity, ApiResponse.class);
            log.info("Successfully created overdue fine for user {} with amount {}", userId, fineAmount);
            
        } catch (Exception e) {
            log.error("Failed to create overdue fine for user {} and borrow record {}: {}", 
                    userId, borrowRecordId, e.getMessage());
            // Don't throw exception to avoid breaking the return process
        }
    }
    
    public void createLostBookFine(Long userId, Long borrowRecordId, String bookIsbn, Double replacementCost) {
        try {
            log.info("Creating lost book fine for user {} and borrow record {}", userId, borrowRecordId);
            
            BigDecimal fineAmount = BigDecimal.valueOf(replacementCost);
            
            CreateFineRequestDto request = CreateFineRequestDto.builder()
                    .userId(userId)
                    .borrowRecordId(borrowRecordId)
                    .bookIsbn(bookIsbn)
                    .type("LOST_BOOK")
                    .amount(fineAmount)
                    .description("Lost book replacement fine")
                    .notes("Automatically generated lost book fine")
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateFineRequestDto> entity = new HttpEntity<>(request, headers);
            
            restTemplate.postForObject(PAYMENT_SERVICE_URL + "/fines", entity, ApiResponse.class);
            log.info("Successfully created lost book fine for user {} with amount {}", userId, fineAmount);
            
        } catch (Exception e) {
            log.error("Failed to create lost book fine for user {} and borrow record {}: {}", 
                    userId, borrowRecordId, e.getMessage());
            // Don't throw exception to avoid breaking the process
        }
    }
    
    public boolean hasUserPendingFines(Long userId) {
        try {
            String url = PAYMENT_SERVICE_URL + "/fines/user/" + userId + "/has-pending";
            ApiResponse<?> response = restTemplate.getForObject(url, ApiResponse.class);
            return response != null && response.isSuccess() && Boolean.TRUE.equals(response.getData());
        } catch (Exception e) {
            log.error("Failed to check pending fines for user {}: {}", userId, e.getMessage());
            return false; // Default to false to not block operations
        }
    }
    
    // DTO for creating fine request
    public static class CreateFineRequestDto {
        private Long userId;
        private Long borrowRecordId;
        private String bookIsbn;
        private String type;
        private BigDecimal amount;
        private String description;
        private String notes;
        
        public CreateFineRequestDto() {}
        
        public static CreateFineRequestDtoBuilder builder() {
            return new CreateFineRequestDtoBuilder();
        }
        
        public static class CreateFineRequestDtoBuilder {
            private Long userId;
            private Long borrowRecordId;
            private String bookIsbn;
            private String type;
            private BigDecimal amount;
            private String description;
            private String notes;
            
            public CreateFineRequestDtoBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }
            
            public CreateFineRequestDtoBuilder borrowRecordId(Long borrowRecordId) {
                this.borrowRecordId = borrowRecordId;
                return this;
            }
            
            public CreateFineRequestDtoBuilder bookIsbn(String bookIsbn) {
                this.bookIsbn = bookIsbn;
                return this;
            }
            
            public CreateFineRequestDtoBuilder type(String type) {
                this.type = type;
                return this;
            }
            
            public CreateFineRequestDtoBuilder amount(BigDecimal amount) {
                this.amount = amount;
                return this;
            }
            
            public CreateFineRequestDtoBuilder description(String description) {
                this.description = description;
                return this;
            }
            
            public CreateFineRequestDtoBuilder notes(String notes) {
                this.notes = notes;
                return this;
            }
            
            public CreateFineRequestDto build() {
                CreateFineRequestDto dto = new CreateFineRequestDto();
                dto.userId = this.userId;
                dto.borrowRecordId = this.borrowRecordId;
                dto.bookIsbn = this.bookIsbn;
                dto.type = this.type;
                dto.amount = this.amount;
                dto.description = this.description;
                dto.notes = this.notes;
                return dto;
            }
        }
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getBorrowRecordId() { return borrowRecordId; }
        public void setBorrowRecordId(Long borrowRecordId) { this.borrowRecordId = borrowRecordId; }
        
        public String getBookIsbn() { return bookIsbn; }
        public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}