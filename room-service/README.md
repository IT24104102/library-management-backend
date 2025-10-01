# Room Booking Service

A production-ready Spring Boot microservice for conference room booking in a library management system.

## Features

- 🏢 **Room Management**: View available rooms, facilities, and capacity
- 📅 **Booking Management**: Create, approve, reject, and cancel room bookings
- ⏰ **Time Slot Management**: Real-time availability checking and conflict prevention
- 👥 **User Integration**: Seamless integration with user-service for user details
- 📧 **Notifications**: Automated email notifications for booking status changes
- 📊 **Reporting**: Administrative reporting for booking analytics
- 🔒 **Business Rules**: Configurable booking limits and validation rules

## Technical Stack

- **Framework**: Spring Boot 3.5.6
- **Database**: MySQL with JPA/Hibernate
- **Java Version**: 21
- **Build Tool**: Maven
- **Documentation**: OpenAPI/Swagger (auto-generated)

## API Endpoints

### Room Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rooms` | Get all available rooms |
| GET | `/api/rooms/available?date={date}` | Get rooms available for specific date |
| GET | `/api/rooms/{id}` | Get room details by ID |
| GET | `/api/rooms/by-capacity?minCapacity={capacity}` | Get rooms by minimum capacity |
| GET | `/api/rooms/by-facilities?facilities={facility1,facility2}` | Get rooms by facilities |

### Booking Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Create a new booking request |
| GET | `/api/bookings/{id}` | Get booking details |
| GET | `/api/bookings/user/{userId}` | Get all bookings for a user |
| GET | `/api/bookings/user/{userId}/paginated` | Get paginated bookings for a user |
| GET | `/api/bookings/pending` | Get pending bookings (for librarians) |
| PUT | `/api/bookings/{id}/approve?librarianId={id}` | Approve a booking |
| PUT | `/api/bookings/{id}/reject?librarianId={id}` | Reject a booking |
| DELETE | `/api/bookings/{id}/cancel?userId={id}` | Cancel a booking |
| GET | `/api/bookings/alternatives` | Get suggested alternative rooms |

### Report Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/bookings` | Generate booking reports for admins |

## Request/Response Examples

### Create Booking Request
```json
{
  "roomId": 1,
  "userId": 123,
  "bookingDate": "2024-12-01",
  "startTime": "10:00",
  "endTime": "12:00",
  "purpose": "Team meeting to discuss project requirements"
}
```

### Booking Response
```json
{
  "success": true,
  "message": "Booking request created successfully",
  "data": {
    "id": 456,
    "roomId": 1,
    "roomName": "Conference Room A",
    "userId": 123,
    "userName": "John Doe",
    "userEmail": "john.doe@university.edu",
    "bookingDate": "2024-12-01",
    "startTime": "10:00",
    "endTime": "12:00",
    "purpose": "Team meeting to discuss project requirements",
    "status": "PENDING",
    "createdAt": "2024-11-28T14:30:00"
  }
}
```

### Reject Booking Request
```json
{
  "rejectionReason": "Room is needed for urgent faculty meeting"
}
```

## Business Rules

- **Booking Duration**: Maximum 4 hours per booking
- **Daily Limit**: Maximum 2 bookings per user per day
- **Weekly Limit**: Maximum 5 bookings per user per week
- **Advance Booking**: Can book up to 30 days in advance
- **Business Hours**: Bookings allowed between 8:00 AM - 6:00 PM
- **Conflict Prevention**: Real-time checking for scheduling conflicts

## Configuration

Configure the following properties in `application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/room_service_db
spring.datasource.username=root
spring.datasource.password=password

# User Service Integration
user-service.base-url=http://localhost:8081/api/users

# Business Rules (customizable)
room-booking.max-duration-hours=4
room-booking.max-daily-bookings=2
room-booking.max-weekly-bookings=5
room-booking.advance-booking-days=30
```

## Database Schema

### Rooms Table
- `id` (Primary Key)
- `name` (Unique room identifier)
- `capacity` (Number of people)
- `description` (Room description)
- `location` (Physical location)
- `is_active` (Active status)
- `created_at`, `updated_at` (Timestamps)

### Bookings Table
- `id` (Primary Key)
- `room_id` (Foreign Key to rooms)
- `user_id` (Reference to user-service)
- `booking_date` (Date of booking)
- `start_time`, `end_time` (Time slot)
- `purpose` (Booking purpose)
- `status` (PENDING, APPROVED, REJECTED, CANCELLED)
- `rejection_reason` (If rejected)
- `approved_by`, `rejected_by` (Librarian IDs)
- `user_name`, `user_email`, `user_role` (Cached user data)
- `created_at`, `updated_at` (Timestamps)

### Room Facilities Table
- `room_id` (Foreign Key)
- `facility` (Facility name)

## Status Transitions

```
PENDING → APPROVED (by librarian)
PENDING → REJECTED (by librarian with reason)
PENDING → CANCELLED (by user)
APPROVED → CANCELLED (by user)
```

## Error Handling

The API uses standardized error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors)
- `404` - Not Found
- `409` - Conflict (booking conflicts)
- `500` - Internal Server Error

## Running the Service

1. **Prerequisites**:
   - Java 21+
   - MySQL 8.0+
   - Maven 3.8+

2. **Database Setup**:
   ```sql
   CREATE DATABASE room_service_db;
   ```

3. **Start the Service**:
   ```bash
   mvn spring-boot:run
   ```

4. **Health Check**:
   ```bash
   curl http://localhost:8082/api/rooms/health
   ```

## Integration with User Service

The room service integrates with the user-service to fetch user details. Ensure the user-service is running on the configured URL (`http://localhost:8081/api/users`).

## Future Enhancements

- 📱 **Mobile API**: RESTful endpoints optimized for mobile apps
- 🔔 **Real-time Notifications**: WebSocket support for real-time updates
- 📧 **Email Integration**: SMTP configuration for actual email notifications
- 🔐 **Security**: JWT authentication and role-based authorization
- 📊 **Analytics**: Enhanced reporting with charts and statistics
- 🌍 **Multi-tenant**: Support for multiple library branches
- 📱 **QR Codes**: QR code generation for room access
- 🕒 **Recurring Bookings**: Support for recurring meeting patterns

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.