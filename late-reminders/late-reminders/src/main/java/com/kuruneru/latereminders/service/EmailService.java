package com.kuruneru.latereminders.service;

import com.kuruneru.latereminders.dto.LoanDto;
import com.kuruneru.latereminders.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${library-reminders.email.from}")
    private String fromEmail;
    
    @Value("${library-reminders.email.from-name}")
    private String fromName;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
    
    /**
     * Send reminder email for loan due tomorrow
     */
    public boolean sendDueTomorrowReminder(UserDto user, LoanDto loan) {
        try {
            String subject = "📚 Book Due Tomorrow - " + getBookTitle(loan);
            String content = createDueTomorrowEmailContent(user, loan);
            
            return sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("Failed to send due tomorrow reminder for loan {} to user {}", 
                loan.getId(), user.getId(), e);
            return false;
        }
    }
    
    /**
     * Send reminder email for overdue loan
     */
    public boolean sendOverdueReminder(UserDto user, LoanDto loan) {
        try {
            String subject = "⚠️ Overdue Book Return - " + getBookTitle(loan);
            String content = createOverdueEmailContent(user, loan);
            
            return sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("Failed to send overdue reminder for loan {} to user {}", 
                loan.getId(), user.getId(), e);
            return false;
        }
    }
    
    /**
     * Send generic email
     */
    private boolean sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // true for HTML content
            
            mailSender.send(message);
            log.info("Successfully sent email to {} with subject: {}", toEmail, subject);
            return true;
            
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {} with subject: {}", toEmail, subject, e);
            return false;
        }
    }
    
    /**
     * Create email content for due tomorrow reminder
     */
    private String createDueTomorrowEmailContent(UserDto user, LoanDto loan) {
        String bookTitle = getBookTitle(loan);
        String dueDate = loan.getDueDate().format(DATE_FORMATTER);
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2563eb; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f8fafc; padding: 20px; border-radius: 0 0 8px 8px; }
                    .book-info { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #f59e0b; }
                    .warning { background-color: #fef3c7; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #f59e0b; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #6b7280; }
                    .button { background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📚 Book Due Tomorrow</h1>
                        <p>Hello %s,</p>
                    </div>
                    <div class="content">
                        <div class="warning">
                            <h3>⏰ Friendly Reminder</h3>
                            <p>This is a friendly reminder that you have a book due for return <strong>tomorrow</strong>.</p>
                        </div>
                        
                        <div class="book-info">
                            <h3>Book Details:</h3>
                            <p><strong>📖 Title:</strong> %s</p>
                            <p><strong>📚 ISBN:</strong> %s</p>
                            <p><strong>📅 Due Date:</strong> %s</p>
                            <p><strong>🆔 Loan ID:</strong> %s</p>
                        </div>
                        
                        <h3>What you need to do:</h3>
                        <ul>
                            <li>Return the book to the library before the due date</li>
                            <li>If you need more time, you can renew the book (subject to renewal limits)</li>
                            <li>Contact us if you have any questions</li>
                        </ul>
                        
                        <p><strong>Note:</strong> Late returns may result in fines. Please return the book on time to avoid any penalties.</p>
                        
                        <p>Thank you for using our library services!</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from the Library Management System.</p>
                        <p>Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            user.getName() != null ? user.getName() : "Valued Member",
            bookTitle,
            loan.getBookIsbn() != null ? loan.getBookIsbn() : "N/A",
            dueDate,
            loan.getId()
        );
    }
    
    /**
     * Create email content for overdue reminder
     */
    private String createOverdueEmailContent(UserDto user, LoanDto loan) {
        String bookTitle = getBookTitle(loan);
        String dueDate = loan.getDueDate().format(DATE_FORMATTER);
        long daysOverdue = java.time.Duration.between(loan.getDueDate(), java.time.LocalDateTime.now()).toDays();
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #dc2626; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f8fafc; padding: 20px; border-radius: 0 0 8px 8px; }
                    .book-info { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #dc2626; }
                    .urgent { background-color: #fee2e2; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #dc2626; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #6b7280; }
                    .button { background-color: #dc2626; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Overdue Book Return</h1>
                        <p>Hello %s,</p>
                    </div>
                    <div class="content">
                        <div class="urgent">
                            <h3>🚨 URGENT: Overdue Book</h3>
                            <p>Your borrowed book is now <strong>%d day(s) overdue</strong>. Please return it immediately to avoid additional fines.</p>
                        </div>
                        
                        <div class="book-info">
                            <h3>Book Details:</h3>
                            <p><strong>📖 Title:</strong> %s</p>
                            <p><strong>📚 ISBN:</strong> %s</p>
                            <p><strong>📅 Due Date:</strong> %s</p>
                            <p><strong>🆔 Loan ID:</strong> %s</p>
                            <p><strong>⏰ Days Overdue:</strong> %d day(s)</p>
                        </div>
                        
                        <h3>Immediate Action Required:</h3>
                        <ul>
                            <li><strong>Return the book immediately</strong> to minimize fines</li>
                            <li>Visit the library during operating hours</li>
                            <li>Contact us if the book is lost or damaged</li>
                            <li>Pay any accumulated fines</li>
                        </ul>
                        
                        <div class="urgent">
                            <p><strong>⚠️ Important:</strong> Continued delays may result in:</p>
                            <ul>
                                <li>Increased daily fines</li>
                                <li>Suspension of borrowing privileges</li>
                                <li>Replacement fees if the book is not returned</li>
                            </ul>
                        </div>
                        
                        <p>Please contact the library immediately if you have any questions or concerns.</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from the Library Management System.</p>
                        <p>Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            user.getName() != null ? user.getName() : "Valued Member",
            daysOverdue,
            bookTitle,
            loan.getBookIsbn() != null ? loan.getBookIsbn() : "N/A",
            dueDate,
            loan.getId(),
            daysOverdue
        );
    }
    
    /**
     * Get book title from loan, with fallback
     */
    private String getBookTitle(LoanDto loan) {
        if (loan.getBook() != null && loan.getBook().getTitle() != null) {
            return loan.getBook().getTitle();
        }
        return "Book (ISBN: " + (loan.getBookIsbn() != null ? loan.getBookIsbn() : "Unknown") + ")";
    }
}