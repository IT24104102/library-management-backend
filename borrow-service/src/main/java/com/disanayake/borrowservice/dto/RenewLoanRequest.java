package com.disanayake.borrowservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewLoanRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Borrow record ID is required")
    private Long borrowRecordId;
    
    private String notes; // Optional notes about the renewal
}