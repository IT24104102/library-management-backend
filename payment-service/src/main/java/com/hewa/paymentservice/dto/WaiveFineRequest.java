package com.hewa.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaiveFineRequest {
    
    @NotNull(message = "Fine ID is required")
    private Long fineId;
    
    @NotNull(message = "Waived by (Librarian ID) is required")
    private Long waivedBy;
    
    @NotNull(message = "Waiver reason is required")
    private String reason;
    
    private String notes;
}