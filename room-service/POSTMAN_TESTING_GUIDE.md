# Room Service API - Postman Testing Guide

## Overview
This guide provides comprehensive instructions for testing the Room Service API using the provided Postman collection and environment.

## Files Included
- `Room-Service-API.postman_collection.json` - Complete API collection
- `Room-Service-Local.postman_environment.json` - Local environment variables

## Quick Setup

### 1. Import Collections
1. Open Postman
2. Click **Import** button
3. Select both JSON files:
   - `Room-Service-API.postman_collection.json`
   - `Room-Service-Local.postman_environment.json`

### 2. Set Environment
1. Select **Room Service - Local** environment from dropdown
2. Verify base URL is set to `http://localhost:8082`

### 3. Start Room Service
```bash
cd room-service
./mvnw spring-boot:run
```

## Testing Scenarios

### 📋 API Endpoints Overview

#### **Health & Status**
- `GET /api/rooms/health` - Service health check

#### **Room Management**
- `GET /api/rooms` - Get all available rooms
- `GET /api/rooms/available?date=YYYY-MM-DD` - Get available rooms for specific date
- `GET /api/rooms/{id}` - Get room by ID
- `GET /api/rooms/by-capacity?minCapacity=N` - Filter rooms by minimum capacity
- `GET /api/rooms/by-facilities?facilities=X&facilities=Y` - Filter rooms by facilities

#### **Booking Management**
- `POST /api/bookings` - Create new booking request
- `GET /api/bookings/{id}` - Get booking details
- `GET /api/bookings/user/{userId}` - Get all user bookings
- `GET /api/bookings/user/{userId}/paginated` - Get paginated user bookings
- `GET /api/bookings/pending` - Get pending bookings (for librarians)

#### **Booking Approval (Librarian Functions)**
- `PUT /api/bookings/{id}/approve?librarianId=N` - Approve booking
- `PUT /api/bookings/{id}/reject?librarianId=N` - Reject booking with reason

#### **Booking Actions**
- `DELETE /api/bookings/{id}/cancel?userId=N` - Cancel booking
- `GET /api/bookings/alternatives` - Get alternative room suggestions

#### **Reports (Admin Functions)**
- `GET /api/reports/bookings` - Generate bookings report for date range

## 🧪 Recommended Testing Workflow

### Step 1: Service Verification
1. **Health Check** - Verify service is running
2. **Get All Rooms** - Confirm rooms are available in database

### Step 2: Basic Room Operations
1. **Get Room by ID** - Test individual room retrieval
2. **Get Available Rooms** - Test date-based availability
3. **Filter by Capacity** - Test capacity-based filtering
4. **Filter by Facilities** - Test facility-based filtering

### Step 3: Booking Creation & Management
1. **Create Valid Booking** - Create a standard booking request
2. **Get Booking Details** - Verify booking was created correctly
3. **Get User Bookings** - Test user booking history

### Step 4: Validation Testing (Error Scenarios)
Run these tests to verify business rule enforcement:

1. **Time Conflict** - Try booking same room/time (should fail)
2. **Future Limit** - Try booking >30 days ahead (should fail)
3. **Duration Limit** - Try booking >4 hours (should fail)
4. **Past Date** - Try booking past date (should fail)
5. **Business Hours** - Try booking outside 8AM-6PM (should fail)

### Step 5: Approval Workflow
1. **Get Pending Bookings** - View bookings awaiting approval
2. **Approve Booking** - Test approval process
3. **Reject Booking** - Test rejection with reason

### Step 6: Advanced Features
1. **Alternative Suggestions** - Test room alternatives feature
2. **Booking Reports** - Generate usage reports
3. **Cancel Booking** - Test user cancellation

## 📊 Environment Variables

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8082` | Service base URL |
| `user_id` | `123` | Test user ID |
| `librarian_id` | `456` | Test librarian ID |
| `room_id` | `1` | Test room ID |
| `booking_date` | `2025-09-30` | Test booking date |
| `start_time` | `10:00` | Test start time |
| `end_time` | `12:00` | Test end time |

## 🔍 Sample Test Data

### Valid Booking Request
```json
{
    "roomId": 1,
    "userId": 123,
    "bookingDate": "2025-09-30",
    "startTime": "10:00",
    "endTime": "12:00",
    "purpose": "Team meeting for project planning"
}
```

### Rejection Request
```json
{
    "rejectionReason": "Room needed for urgent faculty meeting. Please book alternative room or different time."
}
```

## 🚀 Business Rules Testing

The collection includes specific test scenarios to validate these business rules:

### ⏰ **Time Constraints**
- Bookings only allowed during business hours (8:00 AM - 6:00 PM)
- Maximum booking duration: 4 hours
- No overlapping bookings for same room

### 📅 **Date Constraints**
- No bookings for past dates
- Maximum advance booking: 30 days
- Maximum 2 bookings per day per user
- Maximum 5 bookings per week per user

### 🔄 **Workflow States**
- New bookings start as "PENDING"
- Librarian approval changes to "APPROVED"
- Users can only cancel their own bookings
- Approved bookings send email notifications

## 📈 Expected Response Format

All endpoints return standardized responses:

```json
{
    "success": true,
    "message": "Operation completed successfully",
    "data": { /* response data */ },
    "timestamp": "2025-09-28T10:00:00Z"
}
```

Error responses:
```json
{
    "success": false,
    "message": "Error description",
    "data": null,
    "timestamp": "2025-09-28T10:00:00Z"
}
```

## 🛠 Troubleshooting

### Common Issues

1. **Service Not Running**
   - Check if room-service is started on port 8082
   - Verify database connection

2. **Database Empty**
   - Run database initialization scripts
   - Insert sample room data

3. **Time Zone Issues**
   - Use ISO format for dates (YYYY-MM-DD)
   - Use 24-hour format for times (HH:MM)

### Status Code Reference
- `200` - Success
- `201` - Created (new booking)
- `400` - Bad Request (validation error)
- `404` - Not Found
- `500` - Internal Server Error

## 📝 Testing Checklist

- [ ] Service health check passes
- [ ] Can retrieve rooms list
- [ ] Can create valid booking
- [ ] Validation rules work (conflict, duration, date limits)
- [ ] Approval workflow functions
- [ ] User can view their bookings
- [ ] Cancellation works
- [ ] Reports generate correctly
- [ ] Email notifications sent (check logs)

## 🎯 Performance Testing

For load testing, use the collection with:
- Multiple concurrent users (different user IDs)
- Various booking times and dates
- Different room selections
- Automated test runs with Newman CLI

```bash
# Install Newman (optional)
npm install -g newman

# Run collection
newman run Room-Service-API.postman_collection.json -e Room-Service-Local.postman_environment.json
```

---

**Note**: Ensure the Room Service application is running and properly configured with database connections before executing tests.