# Payment Service

The Payment Service is a microservice that handles all payment-related operations for the Library Management System, including fine management, payment processing, and waiver functionality.

## Features

- **Fine Management**: Create, track, and manage library fines
- **Payment Processing**: Process payments with multiple payment methods
- **Waiver System**: Allow librarians to waive fines when appropriate
- **Multi-type Fines**: Support for OVERDUE, LOST_BOOK, and DAMAGE fines
- **Status Tracking**: Track fine and payment statuses (PENDING, PAID, WAIVED, CANCELLED)

## Architecture

The service follows a layered architecture:
- **Controller Layer**: REST API endpoints
- **Service Layer**: Business logic
- **Repository Layer**: Data access using JPA
- **Entity Layer**: Database models

## Database Schema

### Fine Entity
- `id`: Unique identifier
- `userId`: Reference to user who owes the fine
- `loanId`: Reference to the loan that generated the fine
- `bookIsbn`: ISBN of the book involved
- `amount`: Fine amount
- `type`: Fine type (OVERDUE, LOST_BOOK, DAMAGE)
- `status`: Fine status (PENDING, PAID, WAIVED, CANCELLED)
- `description`: Description of the fine
- `createdDate`: When the fine was created
- `updatedDate`: Last update timestamp

### Payment Entity
- `id`: Unique identifier
- `fineId`: Reference to the fine being paid
- `userId`: User making the payment
- `amount`: Payment amount
- `paymentMethod`: Method used (CASH, CARD, ONLINE, BANK_TRANSFER)
- `transactionId`: External transaction reference
- `paymentDate`: When payment was made
- `status`: Payment status (PENDING, COMPLETED, FAILED, CANCELLED)
- `notes`: Additional payment notes

## API Endpoints

### Fine Management
- `POST /fines`: Create a new fine
- `GET /fines`: Get all fines (with pagination)
- `GET /fines/{id}`: Get fine by ID
- `GET /fines/user/{userId}`: Get user's fines
- `GET /fines/user/{userId}/pending`: Get user's pending fines
- `GET /fines/user/{userId}/outstanding`: Get user's outstanding amount
- `PUT /fines/{id}/waive`: Waive a fine

### Payment Processing
- `POST /payments`: Process a payment
- `GET /payments`: Get all payments
- `GET /payments/{id}`: Get payment by ID
- `GET /payments/fine/{fineId}`: Get payments for a fine

## Configuration

### Database Configuration (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_payment_db
    username: root
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.MySQLDialect

server:
  port: 8084
```

## Integration with Other Services

The Payment Service integrates with:
- **Borrow Service**: Receives fine creation requests for overdue books
- **User Service**: References user information
- **Frontend**: Provides payment UI for students and fine management for librarians

## Running the Service

1. **Start MySQL Database**
   ```bash
   # Make sure MySQL is running and create the database
   CREATE DATABASE library_payment_db;
   ```

2. **Run the Service**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the API**
   - Base URL: `http://localhost:8084`
   - API Documentation: Available through your API testing tool

## Testing

### Sample API Calls

#### Create a Fine
```bash
curl -X POST http://localhost:8084/fines \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "loanId": 1,
    "bookIsbn": "978-0134685991",
    "amount": 5.50,
    "type": "OVERDUE",
    "description": "Late return - 5 days overdue"
  }'
```

#### Process Payment
```bash
curl -X POST http://localhost:8084/payments \
  -H "Content-Type: application/json" \
  -d '{
    "fineId": 1,
    "userId": 1,
    "amount": 5.50,
    "paymentMethod": "CARD",
    "notes": "Payment via credit card"
  }'
```

#### Waive Fine
```bash
curl -X PUT http://localhost:8084/fines/1/waive \
  -H "Content-Type: application/json" \
  -d '{
    "waivedBy": 2,
    "reason": "Book found in good condition",
    "notes": "Special circumstances waiver"
  }'
```

## Fine Calculation Logic

The fine calculation is handled by the calling service (Borrow Service) based on:
- **Overdue Days**: Number of days past due date
- **Fine Rate**: Configurable rate per day (e.g., $0.50/day)
- **Maximum Fine**: Optional cap on total fine amount
- **Book Type**: Different rates for different book categories

## Error Handling

The service includes comprehensive error handling:
- **400 Bad Request**: Invalid input data
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side errors

All errors return structured JSON responses with error details.

## Future Enhancements

- Integration with external payment gateways
- Automated email notifications for fines
- Payment plans for large fines
- Integration with accounting systems
- Advanced reporting and analytics
- Mobile payment support