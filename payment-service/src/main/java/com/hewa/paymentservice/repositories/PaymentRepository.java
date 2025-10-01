package com.hewa.paymentservice.repositories;

import com.hewa.paymentservice.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find payments by fine ID
    List<Payment> findByFineIdOrderByPaymentDateDesc(Long fineId);
    
    // Find payments by user ID
    Page<Payment> findByUserIdOrderByPaymentDateDesc(Long userId, Pageable pageable);
    
    // Find payment by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Find payments by status
    Page<Payment> findByStatusOrderByPaymentDateDesc(Payment.PaymentStatus status, Pageable pageable);
    
    // Calculate total payments for a user
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaymentsByUserId(@Param("userId") Long userId);
    
    // Find payments within date range
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Check if fine has been paid
    boolean existsByFineIdAndStatus(Long fineId, Payment.PaymentStatus status);
    
    // Find payments by date range (for payment summaries)
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}