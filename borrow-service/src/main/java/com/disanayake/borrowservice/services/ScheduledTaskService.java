package com.disanayake.borrowservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {
    
    private final BorrowService borrowService;
    
    /**
     * Runs every day at 2:00 AM to check for overdue books and create fines
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkOverdueBooks() {
        log.info("Starting daily overdue books check...");
        try {
            borrowService.updateOverdueStatus();
            log.info("Daily overdue books check completed successfully");
        } catch (Exception e) {
            log.error("Error during daily overdue books check", e);
        }
    }
    
    /**
     * Runs every hour during business hours (9 AM to 6 PM) to check for overdue books
     */
    @Scheduled(cron = "0 0 9-18 * * MON-FRI")
    public void hourlyOverdueCheck() {
        log.info("Starting hourly overdue books check...");
        try {
            borrowService.updateOverdueStatus();
            log.info("Hourly overdue books check completed successfully");
        } catch (Exception e) {
            log.error("Error during hourly overdue books check", e);
        }
    }
}