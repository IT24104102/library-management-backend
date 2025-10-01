# Email Service Configuration Guide

## Overview

The Room Service includes a comprehensive email notification system that automatically sends emails for various booking events. The service is designed to be optional and fail gracefully when email is not configured.

## Email Notifications

### Supported Notifications

1. **Booking Created** - Sent when a user submits a booking request
2. **Booking Approved** - Sent when a librarian approves a booking
3. **Booking Rejected** - Sent when a librarian rejects a booking with reason
4. **Booking Cancelled** - Sent when a booking is cancelled
5. **Booking Reminder** - Sent daily at 6 PM for next day's approved bookings

### Email Templates

All emails are professionally formatted and include:
- Booking details (room, date, time, purpose)
- Booking ID for reference
- Room information (location, capacity, facilities)
- Clear next steps for the user
- Library branding and contact information

## Configuration

### Environment Variables

Configure the following environment variables to enable email notifications:

```bash
# Email Server Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-library-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@library.com

# Library Information
LIBRARY_NAME=University Library Management System
```

### Application.yml Configuration

The service automatically loads email configuration from environment variables:

```yaml
spring:
  mail:
    host: ${MAIL_HOST:}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
    from: ${MAIL_FROM:noreply@library.com}

app:
  library:
    name: ${LIBRARY_NAME:University Library Management System}
```

### Gmail Configuration

If using Gmail, follow these steps:

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password**:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate password for "Mail"
3. **Use App Password** as MAIL_PASSWORD (not your regular password)

### Other Email Providers

The service works with any SMTP provider:

#### Outlook/Hotmail
```bash
MAIL_HOST=smtp-mail.outlook.com
MAIL_PORT=587
```

#### Yahoo Mail
```bash
MAIL_HOST=smtp.mail.yahoo.com
MAIL_PORT=587
```

#### Custom SMTP
```bash
MAIL_HOST=your-smtp-server.com
MAIL_PORT=587  # or 465 for SSL
```

## Features

### Graceful Degradation
- Service works normally even without email configuration
- Falls back to console logging when email is not configured
- No errors or failures when SMTP is unavailable

### Conditional Loading
- EmailService only loads when `spring.mail.host` is configured
- Uses `@ConditionalOnProperty` for safe initialization
- Automatic dependency injection with fallback

### Scheduled Reminders
- Daily reminders sent at 6:00 PM for next day's bookings
- Only sends reminders for APPROVED bookings
- Configurable reminder timing via cron expressions

### Error Handling
- Comprehensive error logging for debugging
- Individual email failures don't affect other operations
- Retry logic can be added for production environments

## Testing Email Configuration

### 1. Check Application Startup
Look for these log messages on startup:
```
INFO  - EmailService loaded successfully
DEBUG - Email configuration: host=smtp.gmail.com, port=587
```

### 2. Test with a Booking
1. Create a test booking
2. Check logs for email sending attempts
3. Verify email delivery to user's inbox

### 3. Enable Email Debug Logging
```yaml
logging:
  level:
    org.springframework.mail: DEBUG
```

## Production Considerations

### Security
- Use app passwords, not regular passwords
- Store credentials in secure environment variables
- Consider using cloud email services (SendGrid, Amazon SES)

### Performance
- Email sending is asynchronous and won't block API responses
- Consider implementing email queues for high-volume environments
- Monitor email delivery rates and failures

### Monitoring
- Log all email attempts and results
- Set up alerts for email service failures
- Track email delivery metrics

### Customization
- Email templates can be customized in `EmailService.java`
- Support for HTML emails can be added
- Multiple languages support can be implemented

## Development Setup

### Option 1: Skip Email (Development)
Leave email configuration empty - service will log to console:
```bash
# No MAIL_* variables set
```

### Option 2: Use Gmail (Testing)
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-test@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@yourtest.com
```

### Option 3: Use MailHog (Local Testing)
```bash
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
```

## Troubleshooting

### Common Issues

**1. Authentication Failed**
- Verify app password is correct
- Ensure 2FA is enabled on Gmail
- Check MAIL_USERNAME format

**2. Connection Timeout**
- Verify MAIL_HOST and MAIL_PORT
- Check firewall settings
- Test SMTP connectivity

**3. Emails Not Sent**
- Check application logs for errors
- Verify recipient email addresses
- Check spam/junk folders

**4. Configuration Not Loaded**
- Ensure environment variables are set
- Restart application after config changes
- Check application.yml syntax

### Debug Commands

```bash
# Test SMTP connection
telnet smtp.gmail.com 587

# Check environment variables
echo $MAIL_HOST
echo $MAIL_USERNAME

# View application logs
tail -f logs/room-service.log | grep -i mail
```

## Future Enhancements

- HTML email templates with rich formatting
- Email template engine integration (Thymeleaf, FreeMarker)
- Multi-language email support
- Email delivery status tracking
- Bulk email operations for announcements
- Email preferences per user
- Integration with email marketing platforms