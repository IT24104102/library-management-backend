package com.fernando.bookservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookMetadataResponse {
    
    private boolean success;
    private String message;
    private BookMetadata data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookMetadata {
        private String isbn;
        private String title;
        private String author;
        private String publisher;
        private Integer publicationYear;
        private String genre;
        private String description;
        private String language;
        private Integer pages;
        private String coverImageUrl;
        private String source; // e.g., "Google Books API", "Open Library"
    }
}