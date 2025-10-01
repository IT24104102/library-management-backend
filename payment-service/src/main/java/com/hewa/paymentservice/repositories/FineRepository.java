package com.hewa.paymentservice.repositories;

import com.hewa.paymentservice.entities.Fine;
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
public interface FineRepository extends JpaRepository<Fine, Long> {
    
    // Find fines by user ID
    Page<Fine> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);
    
    // Find fines by user ID and status
    List<Fine> findByUserIdAndStatus(Long userId, Fine.FineStatus status);
    
    // Find fine by borrow record ID
    Optional<Fine> findByBorrowRecordId(Long borrowRecordId);
    
    // Find all pending fines for a user
    List<Fine> findByUserIdAndStatusOrderByCreatedDateDesc(Long userId, Fine.FineStatus status);
    
    // Calculate total outstanding fines for a user
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.userId = :userId AND f.status = 'PENDING'")
    BigDecimal getTotalOutstandingFinesByUserId(@Param("userId") Long userId);
    
    // Find all fines by status
    Page<Fine> findByStatusOrderByCreatedDateDesc(Fine.FineStatus status, Pageable pageable);
    
    // Find fines by book ISBN
    List<Fine> findByBookIsbnOrderByCreatedDateDesc(String bookIsbn);
    
    // Check if user has any pending fines
    boolean existsByUserIdAndStatus(Long userId, Fine.FineStatus status);
    
    // Find fines by date range (for payment summaries)
    List<Fine> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}