# Late Reminders Service 📧

A microservice for automatically sending email reminders for library loan due dates and overdue books.

## Features ✨

- **Automated Daily Processing**: Runs daily at midnight to check for loans due tomorrow and overdue books
- **Smart Email Reminders**: 
  - "Due Tomorrow" notifications for books due in 24 hours
  - "Overdue" notifications for books past their due date
- **Retry Mechanism**: Automatic retry for failed email deliveries with configurable attempts
- **Comprehensive Logging**: Tracks all reminder attempts, successes, and failures
- **REST API**: Endpoints for monitoring, manual triggers, and statistics
- **Gmail SMTP Support**: Uses Gmail's SMTP server for reliable email delivery

## Architecture 🏗️

### Use Case Implementation
**Send Automated Reminders**
- **Actor**: System (IT Staff only for failure handling)
- **Scheduler**: Runs daily at 00:00 and retries failed reminders during the day
- **Queries**: Loans due in 24 hours and overdue loans from borrow-service
- **Messaging**: Composes and sends HTML email reminders
- **Logging**: Records every sent reminder with status tracking
- **Failure Handling**: Retries failed emails with exponential backoff, alerts IT staff after max retries

### Components
- **ReminderService**: Core business logic for processing reminders
- **EmailService**: HTML email composition and delivery via Gmail SMTP
- **SchedulerService**: Cron-based scheduling with manual triggers
- **External Clients**: Communication with borrow-service and user-service
- **ReminderLog Entity**: Comprehensive audit trail with retry tracking

## Setup Instructions 🚀

### Prerequisites
- Java 21+
- Maven 3.6+
- MySQL 8.0+
- Gmail account with App Password
- Running borrow-service (port 8083)
- Running user-service (port 8081)

### 1. Clone and Navigate
```bash
cd library-management-backend/late-reminders
```

### 2. Database Setup
Ensure MySQL is running and the `library_management` database exists:
```sql
CREATE DATABASE IF NOT EXISTS library_management;
```

### 3. Email Configuration
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password:
   - Go to Google Account settings
   - Security → 2-Step Verification → App Passwords
   - Generate password for "Mail"
3. Update `.env` file with your credentials:
```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
REMINDER_EMAIL_FROM=library-system@yourdomain.com
```

### 4. Environment Configuration
Update `.env` file with your settings:
```env
# Database
DATABASE_URL=jdbc:mysql://localhost:3306/library_management
DATABASE_USERNAME=root
DATABASE_PASSWORD=your-password

# Email (Gmail SMTP)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password

# Services
BORROW_SERVICE_URL=http://localhost:8083
USER_SERVICE_URL=http://localhost:8081
```

### 5. Build and Run
```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run
```

The service will start on **port 8085**.

## API Endpoints 🔗

### Health & Monitoring
- `GET /api/reminders/health` - Service health and statistics
- `GET /api/reminders/config` - Service configuration info
- `GET /api/reminders/statistics?days=7` - Reminder statistics

### Manual Operations
- `POST /api/reminders/trigger` - Manually trigger reminder processing
- `POST /api/reminders/retry` - Retry failed reminders

### Logs & History
- `GET /api/reminders/logs` - Get reminder logs (paginated)
- `GET /api/reminders/logs/{id}` - Get specific reminder log

## Scheduler Configuration ⏰

### Default Schedule
- **Daily Reminders**: `0 0 0 * * ?` (Midnight daily)
- **Retry Failed**: Every 30 minutes
- **Health Check**: Every hour

### Customization
Update `application.yml` or `.env`:
```yaml
library-reminders:
  scheduler:
    enabled: true
    daily-check-time: "0 0 8 * * ?" # 8 AM daily
    timezone: "America/New_York"
  email:
    retry-attempts: 5
    retry-delay-minutes: 60
```

## Email Templates 📧

### Due Tomorrow Reminder
- **Subject**: 📚 Book Due Tomorrow - [Book Title]
- **Content**: Friendly reminder with book details and due date
- **Tone**: Informative and helpful

### Overdue Reminder
- **Subject**: ⚠️ Overdue Book Return - [Book Title]
- **Content**: Urgent notice with days overdue and penalty warnings
- **Tone**: Firm but professional

## Monitoring & Troubleshooting 🔍

### Health Check
```bash
curl http://localhost:8085/api/reminders/health
```

### View Recent Logs
```bash
curl "http://localhost:8085/api/reminders/logs?size=10"
```

### Manual Trigger (for testing)
```bash
curl -X POST http://localhost:8085/api/reminders/trigger
```

### Common Issues
1. **Email Not Sending**: Check Gmail App Password and 2FA
2. **Service Unreachable**: Verify borrow-service and user-service are running
3. **Database Errors**: Ensure MySQL is running and credentials are correct
4. **Scheduler Not Running**: Check `SCHEDULER_ENABLED=true` in environment

## Dependencies 📦

### External Services
- **borrow-service** (localhost:8083): Loan data
- **user-service** (localhost:8081): User information

### Database Tables
- `reminder_logs`: Audit trail of all reminder attempts
- `qrtz_*`: Quartz scheduler tables (auto-created)

## Testing 🧪

### Manual Testing
1. Create a loan due tomorrow in the system
2. Trigger manual processing: `POST /api/reminders/trigger`
3. Check logs: `GET /api/reminders/logs`
4. Verify email delivery

### Production Readiness
- Configure proper Gmail credentials
- Set up monitoring alerts for failed reminders
- Review and adjust retry policies
- Monitor disk space for log files

## Security Considerations 🔒

- Gmail credentials stored in environment variables
- No API authentication (add if needed for production)
- Email content includes personal information (ensure compliance)
- Database contains user emails (follow data protection regulations)

## Support 📞

For issues or questions:
1. Check service logs: `GET /api/reminders/health`
2. Review recent reminder attempts: `GET /api/reminders/statistics`
3. Verify external service connectivity
4. Check email service configuration

---
*Automated Library Reminder Service - Keeping readers informed! 📚*