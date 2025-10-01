package com.hettiarachchi.roomservice.service;

import com.hettiarachchi.roomservice.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@library.com}")
    private String fromEmail;
    
    @Value("${app.library.name:Library Management System}")
    private String libraryName;
    
    public boolean sendBookingCreatedEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getUserEmail());
            message.setSubject("Room Booking Request Submitted - " + libraryName);
            
            String emailBody = String.format("""
                Dear %s,
                
                Your room booking request has been successfully submitted to the %s.
                
                Booking Details:
                - Room: %s
                - Date: %s
                - Time: %s - %s
                - Purpose: %s
                - Status: PENDING APPROVAL
                - Booking ID: %s
                
                Your request is currently pending approval from our librarians. You will receive another email once your booking has been reviewed.
                
                If you need to cancel this booking, please contact the library staff or use the online system.
                
                Thank you for using our room booking service.
                
                Best regards,
                %s Team
                """, 
                booking.getUserName(),
                libraryName,
                booking.getRoom().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getId(),
                libraryName
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Booking creation email sent successfully to: {} for booking ID: {}", 
                    booking.getUserEmail(), booking.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send booking creation email to: {} for booking ID: {}. Error: {}", 
                    booking.getUserEmail(), booking.getId(), e.getMessage());
            return false;
        }
    }
    
    public boolean sendBookingApprovedEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getUserEmail());
            message.setSubject("Room Booking Approved - " + libraryName);
            
            String emailBody = String.format("""
                Dear %s,
                
                Great news! Your room booking request has been APPROVED.
                
                Booking Details:
                - Room: %s
                - Date: %s
                - Time: %s - %s
                - Purpose: %s
                - Booking ID: %s
                - Status: APPROVED
                
                Please arrive on time for your booking. If you need to make any changes or cancel this booking, please contact the library staff as soon as possible.
                
                Room Location: %s
                Capacity: %d people
                Available Facilities: %s
                
                We hope you have a productive session!
                
                Best regards,
                %s Team
                """, 
                booking.getUserName(),
                booking.getRoom().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getId(),
                booking.getRoom().getLocation() != null ? booking.getRoom().getLocation() : "Please check with library staff",
                booking.getRoom().getCapacity(),
                booking.getRoom().getFacilities() != null ? String.join(", ", booking.getRoom().getFacilities()) : "Standard facilities",
                libraryName
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Booking approval email sent successfully to: {} for booking ID: {}", 
                    booking.getUserEmail(), booking.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send booking approval email to: {} for booking ID: {}. Error: {}", 
                    booking.getUserEmail(), booking.getId(), e.getMessage());
            return false;
        }
    }
    
    public boolean sendBookingRejectedEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getUserEmail());
            message.setSubject("Room Booking Request - Update Required - " + libraryName);
            
            String emailBody = String.format("""
                Dear %s,
                
                We regret to inform you that your room booking request has not been approved at this time.
                
                Booking Details:
                - Room: %s
                - Date: %s
                - Time: %s - %s
                - Purpose: %s
                - Booking ID: %s
                - Status: NOT APPROVED
                
                Reason: %s
                
                Please feel free to submit a new booking request for alternative times or dates. You can also contact our library staff for assistance in finding suitable alternatives.
                
                We apologize for any inconvenience and appreciate your understanding.
                
                Best regards,
                %s Team
                """, 
                booking.getUserName(),
                booking.getRoom().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getId(),
                booking.getRejectionReason() != null ? booking.getRejectionReason() : "No specific reason provided",
                libraryName
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Booking rejection email sent successfully to: {} for booking ID: {}", 
                    booking.getUserEmail(), booking.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send booking rejection email to: {} for booking ID: {}. Error: {}", 
                    booking.getUserEmail(), booking.getId(), e.getMessage());
            return false;
        }
    }
    
    public boolean sendBookingCancelledEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getUserEmail());
            message.setSubject("Room Booking Cancelled - " + libraryName);
            
            String emailBody = String.format("""
                Dear %s,
                
                This is to confirm that your room booking has been cancelled.
                
                Cancelled Booking Details:
                - Room: %s
                - Date: %s
                - Time: %s - %s
                - Purpose: %s
                - Booking ID: %s
                - Status: CANCELLED
                
                If this cancellation was made in error, please contact the library staff immediately or submit a new booking request.
                
                Thank you for using our room booking service.
                
                Best regards,
                %s Team
                """, 
                booking.getUserName(),
                booking.getRoom().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getId(),
                libraryName
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Booking cancellation email sent successfully to: {} for booking ID: {}", 
                    booking.getUserEmail(), booking.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send booking cancellation email to: {} for booking ID: {}. Error: {}", 
                    booking.getUserEmail(), booking.getId(), e.getMessage());
            return false;
        }
    }
    
    public boolean sendBookingReminderEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getUserEmail());
            message.setSubject("Room Booking Reminder - Tomorrow - " + libraryName);
            
            String emailBody = String.format("""
                Dear %s,
                
                This is a friendly reminder about your upcoming room booking.
                
                Booking Details:
                - Room: %s
                - Date: %s (Tomorrow)
                - Time: %s - %s
                - Purpose: %s
                - Booking ID: %s
                
                Room Location: %s
                Available Facilities: %s
                
                Please arrive on time for your booking. If you need to cancel or make changes, please contact the library staff as soon as possible.
                
                We look forward to serving you!
                
                Best regards,
                %s Team
                """, 
                booking.getUserName(),
                booking.getRoom().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getId(),
                booking.getRoom().getLocation() != null ? booking.getRoom().getLocation() : "Please check with library staff",
                booking.getRoom().getFacilities() != null ? String.join(", ", booking.getRoom().getFacilities()) : "Standard facilities",
                libraryName
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Booking reminder email sent successfully to: {} for booking ID: {}", 
                    booking.getUserEmail(), booking.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send booking reminder email to: {} for booking ID: {}. Error: {}", 
                    booking.getUserEmail(), booking.getId(), e.getMessage());
            return false;
        }
    }
}