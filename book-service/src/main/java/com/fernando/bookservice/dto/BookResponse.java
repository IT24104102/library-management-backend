package com.fernando.bookservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String genre;
    private String description;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status;
    private String shelfLocation;
    private String language;
    private Integer pages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}