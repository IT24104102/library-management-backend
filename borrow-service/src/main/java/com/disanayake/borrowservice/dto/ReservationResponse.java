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
public class ReservationResponse {
    
    private Long id;
    private Long userId;
    private String bookIsbn;
    private BookResponse book;  // Book details from book-service
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    private String status; // ACTIVE, EXPIRED, FULFILLED, CANCELLED
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}