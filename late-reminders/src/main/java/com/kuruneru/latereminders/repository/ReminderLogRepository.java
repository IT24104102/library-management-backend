package com.kuruneru.latereminders.repository;

import com.kuruneru.latereminders.entity.ReminderLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    
    /**
     * Find existing reminder for a specific loan and reminder type
     */
    Optional<ReminderLog> findByLoanIdAndReminderType(Long loanId, ReminderLog.ReminderType reminderType);
    
    /**
     * Find all reminders that need to be retried
     */
    @Query("SELECT r FROM ReminderLog r WHERE r.status IN ('FAILED', 'RETRY_SCHEDULED') " +
           "AND r.retryCount < r.maxRetries " +
           "AND (r.nextRetryAt IS NULL OR r.nextRetryAt <= :now)")
    List<ReminderLog> findRemindersForRetry(@Param("now") LocalDateTime now);
    
    /**
     * Find reminders by status
     */
    List<ReminderLog> findByStatus(ReminderLog.ReminderStatus status);
    
    /**
     * Find reminders by user ID
     */
    List<ReminderLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find reminders by loan ID
     */
    List<ReminderLog> findByLoanIdOrderByCreatedAtDesc(Long loanId);
    
    /**
     * Find reminders created within a date range
     */
    @Query("SELECT r FROM ReminderLog r WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY r.createdAt DESC")
    Page<ReminderLog> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * Get statistics for dashboard
     */
    @Query("SELECT r.status, COUNT(r) FROM ReminderLog r " +
           "WHERE r.createdAt >= :startDate " +
           "GROUP BY r.status")
    List<Object[]> getStatusStatistics(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Count failed reminders that have exceeded max retries
     */
    @Query("SELECT COUNT(r) FROM ReminderLog r WHERE r.status = 'MAX_RETRIES_EXCEEDED' " +
           "AND r.createdAt >= :startDate")
    Long countFailedReminders(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find reminders that haven't been sent after creation time plus delay
     */
    @Query("SELECT r FROM ReminderLog r WHERE r.status = 'PENDING' " +
           "AND r.createdAt <= :cutoffTime")
    List<ReminderLog> findStuckReminders(@Param("cutoffTime") LocalDateTime cutoffTime);
}