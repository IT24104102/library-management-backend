package com.disanayake.borrowservice.repositories;

import com.disanayake.borrowservice.entities.BorrowRecord;
import com.disanayake.borrowservice.entities.BorrowRecord.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    
    // Find active borrow record for user and book
    Optional<BorrowRecord> findByUserIdAndBookIsbnAndStatus(Long userId, String bookIsbn, BorrowStatus status);
    
    // Find all active borrows for a user
    List<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowStatus status);
    
    // Count active borrows for a user
    long countByUserIdAndStatus(Long userId, BorrowStatus status);
    
    // Find all borrows by user
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);
    
    // Find all borrows by book ISBN
    Page<BorrowRecord> findByBookIsbn(String bookIsbn, Pageable pageable);
    
    // Find overdue books
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'ACTIVE' AND br.dueDate < :currentDate")
    List<BorrowRecord> findOverdueBooks(@Param("currentDate") LocalDate currentDate);
    
    // Find books due soon (within specified days)
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'ACTIVE' AND br.dueDate <= :dueDate AND br.dueDate >= :currentDate")
    List<BorrowRecord> findBooksDueSoon(@Param("currentDate") LocalDate currentDate, @Param("dueDate") LocalDate dueDate);
    
    // Find all borrows by status
    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);
    
    // Find borrows with fines
    @Query("SELECT br FROM BorrowRecord br WHERE br.fineAmount > 0")
    Page<BorrowRecord> findBorrowsWithFines(Pageable pageable);
    
    // Get borrowing history for a book
    Page<BorrowRecord> findByBookIsbnOrderByBorrowDateDesc(String bookIsbn, Pageable pageable);
    
    // Get user's borrowing history
    Page<BorrowRecord> findByUserIdOrderByBorrowDateDesc(Long userId, Pageable pageable);
    
    // Find borrows by date range
    Page<BorrowRecord> findByBorrowDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Find returns by date range
    Page<BorrowRecord> findByReturnDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Check if book is reserved (for future enhancement)
    @Query("SELECT COUNT(br) > 0 FROM BorrowRecord br WHERE br.bookIsbn = :isbn AND br.status = 'RESERVED'")
    boolean isBookReserved(@Param("isbn") String isbn);
}