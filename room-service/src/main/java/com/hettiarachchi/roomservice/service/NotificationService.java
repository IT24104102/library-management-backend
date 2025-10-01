package com.hettiarachchi.roomservice.service;

import com.hettiarachchi.roomservice.entity.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    
    private final EmailService emailService;
    
    public NotificationService(@Autowired(required = false) EmailService emailService) {
        this.emailService = emailService;
    }
    
    // In a production environment, this would integrate with email service, 
    // message queues, or notification services
    
    public void sendBookingCreatedNotification(Booking booking) {
        log.info("Notification: Booking created for user {} in room {} on {}", 
                booking.getUserName(), booking.getRoom().getName(), booking.getBookingDate());
        
        if (emailService != null) {
            try {
                boolean emailSent = emailService.sendBookingCreatedEmail(booking);
                if (emailSent) {
                    log.info("Booking creation notification sent successfully to: {}", booking.getUserEmail());
                } else {
                    log.warn("Failed to send booking creation notification to: {}", booking.getUserEmail());
                }
            } catch (Exception e) {
                log.error("Error sending booking creation notification to: {}. Error: {}", 
                        booking.getUserEmail(), e.getMessage());
            }
        } else {
            // Fallback when email service is not configured
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
        
        // Future: Add other notification methods (SMS, Push notifications, etc.)
    }
    
    public void sendBookingApprovedNotification(Booking booking) {
        log.info("Notification: Booking approved for user {} in room {}", 
                booking.getUserName(), booking.getRoom().getName());
        
        if (emailService != null) {
            try {
                boolean emailSent = emailService.sendBookingApprovedEmail(booking);
                if (emailSent) {
                    log.info("Booking approval notification sent successfully to: {}", booking.getUserEmail());
                } else {
                    log.warn("Failed to send booking approval notification to: {}", booking.getUserEmail());
                }
            } catch (Exception e) {
                log.error("Error sending booking approval notification to: {}. Error: {}", 
                        booking.getUserEmail(), e.getMessage());
            }
        } else {
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
    }
    
    public void sendBookingRejectedNotification(Booking booking) {
        log.info("Notification: Booking rejected for user {} in room {}", 
                booking.getUserName(), booking.getRoom().getName());
        
        if (emailService != null) {
            try {
                boolean emailSent = emailService.sendBookingRejectedEmail(booking);
                if (emailSent) {
                    log.info("Booking rejection notification sent successfully to: {}", booking.getUserEmail());
                } else {
                    log.warn("Failed to send booking rejection notification to: {}", booking.getUserEmail());
                }
            } catch (Exception e) {
                log.error("Error sending booking rejection notification to: {}. Error: {}", 
                        booking.getUserEmail(), e.getMessage());
            }
        } else {
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
    }
    
    public void sendBookingCancelledNotification(Booking booking) {
        log.info("Notification: Booking cancelled for user {} in room {}", 
                booking.getUserName(), booking.getRoom().getName());
        
        if (emailService != null) {
            try {
                boolean emailSent = emailService.sendBookingCancelledEmail(booking);
                if (emailSent) {
                    log.info("Booking cancellation notification sent successfully to: {}", booking.getUserEmail());
                } else {
                    log.warn("Failed to send booking cancellation notification to: {}", booking.getUserEmail());
                }
            } catch (Exception e) {
                log.error("Error sending booking cancellation notification to: {}. Error: {}", 
                        booking.getUserEmail(), e.getMessage());
            }
        } else {
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
    }
    
    public void sendBookingReminderNotification(Booking booking) {
        log.info("Notification: Sending reminder for booking {} to user {} for room {}", 
                booking.getId(), booking.getUserName(), booking.getRoom().getName());
        
        if (emailService != null) {
            try {
                boolean emailSent = emailService.sendBookingReminderEmail(booking);
                if (emailSent) {
                    log.info("Booking reminder notification sent successfully to: {}", booking.getUserEmail());
                } else {
                    log.warn("Failed to send booking reminder notification to: {}", booking.getUserEmail());
                }
            } catch (Exception e) {
                log.error("Error sending booking reminder notification to: {}. Error: {}", 
                        booking.getUserEmail(), e.getMessage());
            }
        } else {
            log.info("Reminder notification for booking {} would be sent to: {}", 
                    booking.getId(), booking.getUserEmail());
            log.warn("Email service not configured. Reminder notification not sent.");
        }
    }
}