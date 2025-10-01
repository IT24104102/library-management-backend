package com.hewa.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFineRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Borrow record ID is required")
    private Long borrowRecordId;
    
    @NotNull(message = "Book ISBN is required")
    private String bookIsbn;
    
    @NotNull(message = "Fine type is required")
    private String type; // OVERDUE, LOST_BOOK, DAMAGE
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String description;
    private String notes;
}