package com.disanayake.borrowservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecordResponse {
    
    private Long id;
    private Long userId;
    private String bookIsbn;
    private BookResponse book;  // Book details from book-service
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private Boolean isOverdue;
    private Double fineAmount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}