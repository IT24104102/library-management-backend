package com.hewa.paymentservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fine_id", nullable = false)
    private Long fineId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;
    
    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    
    @Column(name = "processed_by")
    private Long processedBy; // Librarian ID if processed manually
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;
    
    @PrePersist
    protected void onCreate() {
        paymentDate = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }
    
    public enum PaymentMethod {
        CASH,
        CARD,
        ONLINE,
        BANK_TRANSFER
    }
    
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED,
        CANCELLED
    }
}