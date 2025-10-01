package com.fernando.bookservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {
    
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
    
    @Min(value = 1, message = "Total copies must be at least 1")
    @Max(value = 1000, message = "Total copies cannot exceed 1000")
    private Integer totalCopies;
    
    @Size(max = 100, message = "Shelf location must not exceed 100 characters")
    private String shelfLocation;
    
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;
    
    @Min(value = 1, message = "Pages must be at least 1")
    @Max(value = 10000, message = "Pages cannot exceed 10000")
    private Integer pages;
    
    private String status; // AVAILABLE, UNAVAILABLE, LOST, MAINTENANCE
}