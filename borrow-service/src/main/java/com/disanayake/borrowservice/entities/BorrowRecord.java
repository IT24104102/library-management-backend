package com.disanayake.borrowservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "book_isbn", nullable = false)
    private String bookIsbn;
    
    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Column(name = "return_date")
    private LocalDate returnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;
    
    @Column(name = "is_overdue")
    private Boolean isOverdue;
    
    @Column(name = "fine_amount")
    private Double fineAmount;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BorrowStatus.ACTIVE;
        }
        if (isOverdue == null) {
            isOverdue = false;
        }
        if (fineAmount == null) {
            fineAmount = 0.0;
        }
        // Set due date to 14 days from borrow date
        if (dueDate == null && borrowDate != null) {
            dueDate = borrowDate.plusDays(14);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum BorrowStatus {
        ACTIVE, RETURNED, OVERDUE, LOST, RENEWED
    }
}