package com.fernando.bookservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    
    @Id
    private String isbn; // Using ISBN as primary key
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    private String publisher;
    
    @Column(name = "publication_year")
    private Integer publicationYear;
    
    private String genre;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;
    
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;
    
    @Column(name = "shelf_location")
    private String shelfLocation;
    
    @Column(name = "language")
    private String language;
    
    @Column(name = "pages")
    private Integer pages;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BookStatus.AVAILABLE;
        }
        if (availableCopies == null) {
            availableCopies = totalCopies;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum BookStatus {
        AVAILABLE, UNAVAILABLE, LOST, MAINTENANCE
    }
}