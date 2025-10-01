package com.kuruneru.latereminders.service;

import com.kuruneru.latereminders.dto.LoanDto;
import com.kuruneru.latereminders.dto.UserDto;
import com.kuruneru.latereminders.entity.ReminderLog;
import com.kuruneru.latereminders.repository.ReminderLogRepository;
import com.kuruneru.latereminders.service.external.BorrowServiceClient;
import com.kuruneru.latereminders.service.external.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    
    private final BorrowServiceClient borrowServiceClient;
    private final UserServiceClient userServiceClient;
    private final EmailService emailService;
    private final ReminderLogRepository reminderLogRepository;
    
    @Value("${library-reminders.email.retry-delay-minutes}")
    private int retryDelayMinutes;
    
    /**
     * Process all reminders - main entry point for scheduler
     */
    @Transactional
    public void processAllReminders() {
        log.info("Starting reminder processing...");
        
        try {
            // Process due tomorrow reminders
            processDueTomorrowReminders();
            
            // Process overdue reminders  
            processOverdueReminders();
            
            // Retry failed reminders
            retryFailedReminders();
            
            log.info("Reminder processing completed successfully");
            
        } catch (Exception e) {
            log.error("Error during reminder processing", e);
            // Don't throw exception to prevent scheduler from stopping
        }
    }
    
    /**
     * Process reminders for loans due tomorrow
     */
    @Transactional
    public void processDueTomorrowReminders() {
        log.info("Processing due tomorrow reminders...");
        
        try {
            List<LoanDto> loansDueTomorrow = borrowServiceClient.getLoansDueTomorrow();
            log.info("Found {} loans due tomorrow", loansDueTomorrow.size());
            
            for (LoanDto loan : loansDueTomorrow) {
                processLoanReminder(loan, ReminderLog.ReminderType.DUE_TOMORROW);
            }
            
        } catch (Exception e) {
            log.error("Error processing due tomorrow reminders", e);
        }
    }
    
    /**
     * Process reminders for overdue loans
     */
    @Transactional
    public void processOverdueReminders() {
        log.info("Processing overdue reminders...");
        
        try {
            List<LoanDto> overdueLoans = borrowServiceClient.getOverdueLoans();
            log.info("Found {} overdue loans", overdueLoans.size());
            
            for (LoanDto loan : overdueLoans) {
                processLoanReminder(loan, ReminderLog.ReminderType.OVERDUE);
            }
            
        } catch (Exception e) {
            log.error("Error processing overdue reminders", e);
        }
    }
    
    /**
     * Process individual loan reminder
     */
    private void processLoanReminder(LoanDto loan, ReminderLog.ReminderType reminderType) {
        try {
            // Check if reminder already sent for this loan and type
            Optional<ReminderLog> existingReminder = reminderLogRepository
                .findByLoanIdAndReminderType(loan.getId(), reminderType);
            
            if (existingReminder.isPresent()) {
                ReminderLog reminder = existingReminder.get();
                if (reminder.getStatus() == ReminderLog.ReminderStatus.SENT) {
                    log.debug("Reminder already sent for loan {} type {}", loan.getId(), reminderType);
                    return;
                }
            }
            
            // Get user details
            Optional<UserDto> userOpt = userServiceClient.getUserById(loan.getUserId());
            if (userOpt.isEmpty()) {
                log.warn("User not found for loan {}", loan.getId());
                return;
            }
            
            UserDto user = userOpt.get();
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.warn("User {} has no email address for loan {}", user.getId(), loan.getId());
                return;
            }
            
            // Create or update reminder log
            ReminderLog reminderLog = existingReminder.orElse(new ReminderLog());
            populateReminderLog(reminderLog, loan, user, reminderType);
            
            // Send email
            boolean emailSent = sendReminderEmail(user, loan, reminderType);
            
            if (emailSent) {
                reminderLog.setStatus(ReminderLog.ReminderStatus.SENT);
                reminderLog.setSentAt(LocalDateTime.now());
                log.info("Successfully sent {} reminder for loan {} to user {}", 
                    reminderType, loan.getId(), user.getId());
            } else {
                reminderLog.setStatus(ReminderLog.ReminderStatus.FAILED);
                reminderLog.setErrorMessage("Failed to send email");
                reminderLog.setNextRetryAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
                log.warn("Failed to send {} reminder for loan {} to user {}", 
                    reminderType, loan.getId(), user.getId());
            }
            
            reminderLogRepository.save(reminderLog);
            
        } catch (Exception e) {
            log.error("Error processing reminder for loan {} type {}", loan.getId(), reminderType, e);
        }
    }
    
    /**
     * Retry failed reminders
     */
    @Transactional
    public void retryFailedReminders() {
        log.info("Processing retry reminders...");
        
        try {
            List<ReminderLog> failedReminders = reminderLogRepository.findRemindersForRetry(LocalDateTime.now());
            log.info("Found {} reminders to retry", failedReminders.size());
            
            for (ReminderLog reminder : failedReminders) {
                retryReminder(reminder);
            }
            
        } catch (Exception e) {
            log.error("Error processing retry reminders", e);
        }
    }
    
    /**
     * Retry individual reminder
     */
    private void retryReminder(ReminderLog reminder) {
        try {
            log.info("Retrying reminder {} (attempt {}/{})", 
                reminder.getId(), reminder.getRetryCount() + 1, reminder.getMaxRetries());
            
            // Get fresh user data
            Optional<UserDto> userOpt = userServiceClient.getUserById(reminder.getUserId());
            if (userOpt.isEmpty()) {
                log.warn("User not found for reminder retry {}", reminder.getId());
                reminder.setStatus(ReminderLog.ReminderStatus.FAILED);
                reminder.setErrorMessage("User not found");
                reminderLogRepository.save(reminder);
                return;
            }
            
            UserDto user = userOpt.get();
            
            // Attempt to send email
            boolean emailSent = sendReminderEmailFromLog(user, reminder);
            
            if (emailSent) {
                reminder.setStatus(ReminderLog.ReminderStatus.SENT);
                reminder.setSentAt(LocalDateTime.now());
                reminder.setErrorMessage(null);
                log.info("Successfully retried reminder {}", reminder.getId());
            } else {
                reminder.incrementRetryCount();
                if (reminder.getStatus() == ReminderLog.ReminderStatus.MAX_RETRIES_EXCEEDED) {
                    log.error("Max retries exceeded for reminder {}", reminder.getId());
                } else {
                    reminder.setNextRetryAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
                    log.warn("Retry failed for reminder {}, scheduling next retry", reminder.getId());
                }
            }
            
            reminderLogRepository.save(reminder);
            
        } catch (Exception e) {
            log.error("Error retrying reminder {}", reminder.getId(), e);
        }
    }
    
    /**
     * Send reminder email based on type
     */
    private boolean sendReminderEmail(UserDto user, LoanDto loan, ReminderLog.ReminderType reminderType) {
        try {
            return switch (reminderType) {
                case DUE_TOMORROW -> emailService.sendDueTomorrowReminder(user, loan);
                case OVERDUE -> emailService.sendOverdueReminder(user, loan);
            };
        } catch (Exception e) {
            log.error("Error sending reminder email", e);
            return false;
        }
    }
    
    /**
     * Send reminder email from log data
     */
    private boolean sendReminderEmailFromLog(UserDto user, ReminderLog reminder) {
        try {
            // Create minimal loan DTO from reminder log data
            LoanDto loan = new LoanDto();
            loan.setId(reminder.getLoanId());
            loan.setUserId(reminder.getUserId());
            loan.setBookIsbn(reminder.getBookIsbn());
            loan.setDueDate(reminder.getDueDate());
            
            if (reminder.getBookTitle() != null) {
                loan.setBook(new com.kuruneru.latereminders.dto.BookDto());
                loan.getBook().setTitle(reminder.getBookTitle());
                loan.getBook().setIsbn(reminder.getBookIsbn());
            }
            
            return sendReminderEmail(user, loan, reminder.getReminderType());
        } catch (Exception e) {
            log.error("Error sending reminder email from log", e);
            return false;
        }
    }
    
    /**
     * Populate reminder log with loan and user data
     */
    private void populateReminderLog(ReminderLog reminderLog, LoanDto loan, UserDto user, ReminderLog.ReminderType reminderType) {
        reminderLog.setLoanId(loan.getId());
        reminderLog.setUserId(loan.getUserId());
        reminderLog.setUserEmail(user.getEmail());
        reminderLog.setUserName(user.getName());
        reminderLog.setDueDate(loan.getDueDate());
        reminderLog.setReminderType(reminderType);
        reminderLog.setStatus(ReminderLog.ReminderStatus.PENDING);
        
        // Set book information
        if (loan.getBook() != null) {
            reminderLog.setBookTitle(loan.getBook().getTitle());
            reminderLog.setBookIsbn(loan.getBook().getIsbn());
        } else {
            reminderLog.setBookIsbn(loan.getBookIsbn());
        }
        
        // Set email content for logging
        String subject = reminderType == ReminderLog.ReminderType.DUE_TOMORROW 
            ? "📚 Book Due Tomorrow" 
            : "⚠️ Overdue Book Return";
        reminderLog.setEmailSubject(subject);
    }
}