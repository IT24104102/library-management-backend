package com.fernando.bookservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookWithMetadataRequest {
    
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", 
             message = "Invalid ISBN format")
    private String isbn;
    
    // These fields will be auto-filled from metadata but can be overridden
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 255, message = "Author name must not exceed 255 characters")
    private String author;
    
    @Size(max = 255, message = "Publisher name must not exceed 255 characters")
    private String publisher;
    
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 2030, message = "Publication year cannot be in the future")
    private Integer publicationYear;
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;
    
    @Min(value = 1, message = "Pages must be at least 1")
    @Max(value = 10000, message = "Pages cannot exceed 10000")
    private Integer pages;
    
    // Required library-specific fields
    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Max(value = 1000, message = "Total copies cannot exceed 1000")
    private Integer totalCopies;
    
    @Size(max = 100, message = "Shelf location must not exceed 100 characters")
    private String shelfLocation;
    
    // Flag to indicate if metadata auto-fill should be skipped
    private Boolean skipMetadataFetch = false;
    
    // Flag to indicate if this is manual entry (after metadata fetch failed)
    private Boolean isManualEntry = false;
}