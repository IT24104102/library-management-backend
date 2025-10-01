package com.fernando.bookservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkBookLostRequest {
    
    @NotNull(message = "Number of copies to mark as lost is required")
    @Min(value = 1, message = "Must mark at least 1 copy as lost")
    private Integer copiesToMarkLost;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason; // Optional reason for marking as lost
    
    @Size(max = 100, message = "Reported by field must not exceed 100 characters")
    private String reportedBy; // Who reported the loss
}