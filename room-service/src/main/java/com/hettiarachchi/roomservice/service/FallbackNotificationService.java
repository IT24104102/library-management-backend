package com.hettiarachchi.roomservice.service;

import com.hettiarachchi.roomservice.entity.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnMissingBean(EmailService.class)
public class FallbackNotificationService {
    
    // Fallback notification service when email is not configured
    
    public void sendBookingCreatedNotification(Booking booking) {
        log.info("Notification: Booking created for user {} in room {} on {}", 
                booking.getUserName(), booking.getRoom().getName(), booking.getBookingDate());
        
        String message = String.format(
            "Your booking request for %s on %s from %s to %s has been submitted and is pending approval.",
            booking.getRoom().getName(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime()
        );
        
        log.info("Email notification would be sent to {}: {}", booking.getUserEmail(), message);
        log.warn("Email service not configured. Email notification not sent.");
    }
    
    public void sendBookingApprovedNotification(Booking booking) {
        log.info("Notification: Booking approved for user {} in room {}", 
                booking.getUserName(), booking.getRoom().getName());
        
        String message = String.format(
            "Your booking request for %s on %s from %s to %s has been approved.",
            booking.getRoom().getName(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime()
        );
        
        log.info("Email notification would be sent to {}: {}", booking.getUserEmail(), message);
        log.warn("Email service not configured. Email notification not sent.");
    }
    
    public void sendBookingRejectedNotification(Booking booking) {
        log.info("Notification: Booking rejected for user {} in room {}", 
                booking.getUserName(), booking.getRoom().getName());
        
        String message = String.format(
            "Your booking request for %s on %s from %s to %s has been rejected. Reason: %s",
            booking.getRoom().getName(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getRejectionReason()
        );
        
        log.info("Email notification would be sent to {}: {}", booking.getUserEmail(), message);
        log.warn("Email service not configured. Email notification not sent.");
    }
    
    public void sendBookingCancelledNotification(Booking booking) {
        log.info("Notification: Booking cancelled for user {} in room {}", 
                booking.getUserName(), booking.getRoom().getName());
        
        String message = String.format(
            "Your booking for %s on %s from %s to %s has been cancelled.",
            booking.getRoom().getName(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime()
        );
        
        log.info("Email notification would be sent to {}: {}", booking.getUserEmail(), message);
        log.warn("Email service not configured. Email notification not sent.");
    }
    
    public void sendBookingReminderNotification(Booking booking) {
        log.info("Notification: Sending reminder for booking {} to user {} for room {}", 
                booking.getId(), booking.getUserName(), booking.getRoom().getName());
        
        log.warn("Email service not configured. Reminder notification not sent.");
    }
}