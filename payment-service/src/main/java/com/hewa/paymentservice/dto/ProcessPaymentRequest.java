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
public class ProcessPaymentRequest {
    
    @NotNull(message = "Fine ID is required")
    private Long fineId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private String method; // CASH, CARD, ONLINE, BANK_TRANSFER
    
    private String transactionId;
    private String gatewayReference;
    private Long processedBy; // Librarian ID if processed manually
    private String notes;
}