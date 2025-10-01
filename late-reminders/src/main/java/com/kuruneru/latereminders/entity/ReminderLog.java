package com.kuruneru.latereminders.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "book_title")
    private String bookTitle;
    
    @Column(name = "book_isbn")
    private String bookIsbn;
    
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private ReminderType reminderType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReminderStatus status;
    
    @Column(name = "email_subject")
    private String emailSubject;
    
    @Column(name = "email_content", columnDefinition = "TEXT")
    private String emailContent;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    public enum ReminderType {
        DUE_TOMORROW,
        OVERDUE
    }
    
    public enum ReminderStatus {
        PENDING,
        SENT,
        FAILED,
        RETRY_SCHEDULED,
        MAX_RETRIES_EXCEEDED
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries && 
               (status == ReminderStatus.FAILED || status == ReminderStatus.RETRY_SCHEDULED);
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
        if (this.retryCount >= this.maxRetries) {
            this.status = ReminderStatus.MAX_RETRIES_EXCEEDED;
        } else {
            this.status = ReminderStatus.RETRY_SCHEDULED;
        }
    }
}