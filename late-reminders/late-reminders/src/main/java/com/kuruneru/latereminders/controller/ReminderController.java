package com.kuruneru.latereminders.controller;

import com.kuruneru.latereminders.dto.ApiResponse;
import com.kuruneru.latereminders.entity.ReminderLog;
import com.kuruneru.latereminders.repository.ReminderLogRepository;
import com.kuruneru.latereminders.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Slf4j
public class ReminderController {
    
    private final SchedulerService schedulerService;
    private final ReminderLogRepository reminderLogRepository;
    
    /**
     * Get system health and status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "late-reminders");
            health.put("version", "1.0.0");
            
            // Get basic statistics
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            long totalReminders = reminderLogRepository.count();
            long recentReminders = reminderLogRepository.findByCreatedAtBetween(
                last24Hours, LocalDateTime.now(), PageRequest.of(0, 1)).getTotalElements();
            long failedReminders = reminderLogRepository.countFailedReminders(last24Hours);
            
            health.put("statistics", Map.of(
                "totalReminders", totalReminders,
                "remindersLast24Hours", recentReminders,
                "failedRemindersLast24Hours", failedReminders
            ));
            
            return ResponseEntity.ok(ApiResponse.success(health));
        } catch (Exception e) {
            log.error("Error getting health status", e);
            return ResponseEntity.ok(ApiResponse.error("Health check failed: " + e.getMessage()));
        }
    }
    
    /**
     * Manual trigger for reminder processing
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<String>> triggerReminderProcessing() {
        try {
            log.info("Manual trigger requested for reminder processing");
            schedulerService.triggerImmediateReminderProcessing();
            return ResponseEntity.ok(ApiResponse.success("Reminder processing triggered successfully"));
        } catch (Exception e) {
            log.error("Error triggering reminder processing", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to trigger reminder processing: " + e.getMessage()));
        }
    }
    
    /**
     * Manual trigger for retrying failed reminders
     */
    @PostMapping("/retry")
    public ResponseEntity<ApiResponse<String>> retryFailedReminders() {
        try {
            log.info("Manual trigger requested for retry failed reminders");
            schedulerService.triggerRetryFailedReminders();
            return ResponseEntity.ok(ApiResponse.success("Retry failed reminders triggered successfully"));
        } catch (Exception e) {
            log.error("Error triggering retry failed reminders", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to trigger retry: " + e.getMessage()));
        }
    }
    
    /**
     * Get reminder logs with pagination
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<ReminderLog>>> getReminderLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReminderLog.ReminderStatus status,
            @RequestParam(required = false) ReminderLog.ReminderType type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long loanId) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ReminderLog> logs;
            
            if (userId != null) {
                logs = reminderLogRepository.findByCreatedAtBetween(
                    LocalDateTime.now().minusDays(30), LocalDateTime.now(), pageable);
                // Filter by userId in service layer if needed
            } else if (loanId != null) {
                // Find by loanId - for now just return all, can be enhanced later
                logs = reminderLogRepository.findAll(pageable);
            } else {
                logs = reminderLogRepository.findAll(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            log.error("Error getting reminder logs", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get reminder logs: " + e.getMessage()));
        }
    }
    
    /**
     * Get reminder statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(defaultValue = "7") int days) {
        
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            
            Map<String, Object> stats = new HashMap<>();
            
            // Get status statistics
            List<Object[]> statusStats = reminderLogRepository.getStatusStatistics(startDate);
            Map<String, Long> statusCounts = new HashMap<>();
            for (Object[] stat : statusStats) {
                ReminderLog.ReminderStatus status = (ReminderLog.ReminderStatus) stat[0];
                Long count = (Long) stat[1];
                statusCounts.put(status.toString(), count);
            }
            
            stats.put("statusStatistics", statusCounts);
            stats.put("failedReminders", reminderLogRepository.countFailedReminders(startDate));
            stats.put("periodDays", days);
            stats.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get specific reminder log by ID
     */
    @GetMapping("/logs/{id}")
    public ResponseEntity<ApiResponse<ReminderLog>> getReminderLog(@PathVariable Long id) {
        try {
            return reminderLogRepository.findById(id)
                .map(log -> ResponseEntity.ok(ApiResponse.success(log)))
                .orElse(ResponseEntity.ok(ApiResponse.error("Reminder log not found")));
        } catch (Exception e) {
            log.error("Error getting reminder log {}", id, e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get reminder log: " + e.getMessage()));
        }
    }
    
    /**
     * Get system configuration info
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("service", "late-reminders");
            config.put("description", "Automated email reminder service for library loans");
            config.put("features", List.of(
                "Daily reminder processing",
                "Due tomorrow notifications", 
                "Overdue notifications",
                "Automatic retry for failed emails",
                "Comprehensive logging and monitoring"
            ));
            config.put("endpoints", Map.of(
                "health", "GET /api/reminders/health",
                "trigger", "POST /api/reminders/trigger",
                "retry", "POST /api/reminders/retry",
                "logs", "GET /api/reminders/logs",
                "statistics", "GET /api/reminders/statistics"
            ));
            
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (Exception e) {
            log.error("Error getting config", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get config: " + e.getMessage()));
        }
    }
}