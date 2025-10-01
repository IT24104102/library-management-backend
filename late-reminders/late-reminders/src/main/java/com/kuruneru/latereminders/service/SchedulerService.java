package com.kuruneru.latereminders.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "library-reminders.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerService {
    
    private final ReminderService reminderService;
    
    @Value("${library-reminders.scheduler.daily-check-time}")
    private String dailyCheckTime;
    
    /**
     * Daily reminder job - runs at midnight (00:00:00) by default
     * Processes all due tomorrow and overdue reminders
     */
    @Scheduled(cron = "${library-reminders.scheduler.daily-check-time:0 0 0 * * ?}")
    public void dailyReminderJob() {
        log.info("=== Starting daily reminder job ===");
        
        try {
            reminderService.processAllReminders();
            log.info("=== Daily reminder job completed successfully ===");
        } catch (Exception e) {
            log.error("=== Daily reminder job failed ===", e);
            // Don't rethrow - let the scheduler continue
        }
    }
    
    /**
     * Retry failed reminders every 30 minutes
     * This catches any reminders that failed during the daily job
     */
    @Scheduled(fixedDelayString = "${library-reminders.email.retry-delay-minutes:30}000", initialDelay = 300000) // 5 min initial delay
    public void retryFailedRemindersJob() {
        log.debug("Starting retry failed reminders job");
        
        try {
            reminderService.retryFailedReminders();
            log.debug("Retry failed reminders job completed");
        } catch (Exception e) {
            log.error("Retry failed reminders job failed", e);
            // Don't rethrow - let the scheduler continue
        }
    }
    
    /**
     * Health check job - runs every hour to ensure scheduler is working
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void healthCheckJob() {
        log.debug("Reminder scheduler health check - system is running");
    }
    
    /**
     * Manual trigger for immediate reminder processing
     * Can be called via REST API for testing or manual execution
     */
    public void triggerImmediateReminderProcessing() {
        log.info("=== Manual trigger: Starting immediate reminder processing ===");
        
        try {
            reminderService.processAllReminders();
            log.info("=== Manual trigger: Reminder processing completed successfully ===");
        } catch (Exception e) {
            log.error("=== Manual trigger: Reminder processing failed ===", e);
            throw new RuntimeException("Failed to process reminders", e);
        }
    }
    
    /**
     * Manual trigger for retrying failed reminders
     */
    public void triggerRetryFailedReminders() {
        log.info("=== Manual trigger: Starting retry failed reminders ===");
        
        try {
            reminderService.retryFailedReminders();
            log.info("=== Manual trigger: Retry failed reminders completed successfully ===");
        } catch (Exception e) {
            log.error("=== Manual trigger: Retry failed reminders failed ===", e);
            throw new RuntimeException("Failed to retry reminders", e);
        }
    }
}