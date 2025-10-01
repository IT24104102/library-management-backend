package com.hettiarachchi.roomservice.service;

import com.hettiarachchi.roomservice.entity.Booking;
import com.hettiarachchi.roomservice.entity.BookingStatus;
import com.hettiarachchi.roomservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReminderService {
    
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    
    /**
     * Send reminder emails for bookings scheduled for tomorrow
     * Runs every day at 6:00 PM
     */
    @Scheduled(cron = "0 0 18 * * *") // 6:00 PM every day
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Sending booking reminders for date: {}", tomorrow);
        
        try {
            List<Booking> tomorrowBookings = bookingRepository.findBookingsByRoomAndDate(null, tomorrow)
                .stream()
                .filter(booking -> booking.getStatus() == BookingStatus.APPROVED)
                .toList();
            
            log.info("Found {} approved bookings for tomorrow", tomorrowBookings.size());
            
            for (Booking booking : tomorrowBookings) {
                try {
                    notificationService.sendBookingReminderNotification(booking);
                    log.debug("Reminder sent for booking ID: {}", booking.getId());
                } catch (Exception e) {
                    log.error("Failed to send reminder for booking ID: {}. Error: {}", 
                            booking.getId(), e.getMessage());
                }
            }
            
            log.info("Completed sending {} booking reminders for {}", 
                    tomorrowBookings.size(), tomorrow);
                    
        } catch (Exception e) {
            log.error("Error in daily reminder process for date {}: {}", tomorrow, e.getMessage());
        }
    }
    
    /**
     * Clean up old cancelled/rejected bookings
     * Runs every Sunday at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN") // 2:00 AM every Sunday
    public void cleanupOldBookings() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        log.info("Starting cleanup of old bookings before date: {}", cutoffDate);
        
        try {
            // This would need a custom repository method to delete old cancelled/rejected bookings
            // For now, just log the intent
            log.info("Cleanup process would remove old cancelled/rejected bookings before: {}", cutoffDate);
            
        } catch (Exception e) {
            log.error("Error in booking cleanup process: {}", e.getMessage());
        }
    }
}