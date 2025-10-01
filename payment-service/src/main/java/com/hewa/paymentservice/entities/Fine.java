package com.hewa.paymentservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(name = "borrow_record_id", nullable = false)
    private Long borrowRecordId;
    
    @Column(nullable = false, length = 20)
    private String bookIsbn;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineType type;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineStatus status;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "paid_date")
    private LocalDateTime paidDate;
    
    @Column(name = "waived_date")
    private LocalDateTime waivedDate;
    
    @Column(name = "waived_by")
    private Long waivedBy; // Librarian ID who waived the fine
    
    @Column(name = "waiver_reason", length = 500)
    private String waiverReason;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 500)
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (status == null) {
            status = FineStatus.PENDING;
        }
    }
    
    public enum FineType {
        OVERDUE,
        LOST_BOOK,
        DAMAGE
    }
    
    public enum FineStatus {
        PENDING,
        PAID,
        WAIVED,
        CANCELLED
    }
}