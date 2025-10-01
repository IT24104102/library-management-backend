package com.disanayake.borrowservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkBookAsLostRequest {
    
    @NotNull(message = "Borrow record ID is required")
    private Long borrowRecordId;
    
    private Long markedByUserId; // ID of the user marking the book as lost (librarian/admin)
    
    @Positive(message = "Replacement cost must be positive")
    private Double replacementCost; // Cost to replace the lost book
    
    private String notes; // Additional notes about the lost book
}