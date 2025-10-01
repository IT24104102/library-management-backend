package com.fernando.bookservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCopiesRequest {
    
    @NotNull(message = "Number of copies to add is required")
    @Min(value = 1, message = "Must add at least 1 copy")
    private Integer copiesToAdd;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason; // Optional reason for adding copies
    
    @Size(max = 100, message = "Added by field must not exceed 100 characters")
    private String addedBy; // Who added the copies
}