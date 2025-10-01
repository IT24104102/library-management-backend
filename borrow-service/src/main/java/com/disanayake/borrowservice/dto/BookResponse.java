package com.disanayake.borrowservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String genre;
    private Integer publicationYear;
    private Integer totalCopies;
    private Integer availableCopies;
    private String shelfLocation;
    private String description;
    private String status;
}