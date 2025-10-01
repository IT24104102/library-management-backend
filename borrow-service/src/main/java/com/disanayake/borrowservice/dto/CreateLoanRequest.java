package com.disanayake.borrowservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Book ISBN is required")
    private String isbn;
    
    @NotNull(message = "Librarian ID is required")
    private Long librarianId;
    
    private String notes; // Optional notes about the loan creation
}