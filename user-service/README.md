# User Service - Library Management System

## Overview
This microservice handles user management operations for the Library Management System. It provides REST API endpoints for creating, viewing, updating, and deactivating user accounts (Students and Librarians).

## Features
- ✅ Create Student and Librarian accounts
- ✅ Send welcome emails with temporary passwords
- ✅ User CRUD operations (Create, Read, Update, Deactivate)
- ✅ Email duplicate validation
- ✅ Input validation with detailed error messages
- ✅ Pagination support for user listing
- ✅ Comprehensive error handling
- ✅ Audit logging
- ✅ **Automatic Admin Account Creation** on first startup

## Tech Stack
- Java 21
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Mail
- MySQL Database
- Lombok
- BCrypt Password Encoding
- Bean Validation

## API Endpoints

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "userPassword"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "fullName": "John Doe",
      "email": "john.doe@library.com",
      "role": "LIBRARIAN",
      "status": "ACTIVE",
      "mustChangePassword": true
    }
  }
}
```

#### Validate Token
```http
POST /api/auth/validate
Authorization: Bearer <token>
```

#### Get User Profile
```http
GET /api/auth/profile
Authorization: Bearer <token>
```

#### First-Time Password Change
```http
POST /api/auth/change-password-first-time
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "TemporaryPassword123",
  "newPassword": "MyNewSecurePassword123",
  "confirmPassword": "MyNewSecurePassword123"
}
```

### User Management Endpoints

#### Health Check
```http
GET /api/users/health
```

### Create Librarian
```http
POST /api/users/librarians
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@library.com",
  "password": "password123",
  "employeeId": "EMP001",
  "branch": "Main Branch",
  "workShift": "Morning",
  "contactNumber": "+1234567890"
}
```

### Create Student
```http
POST /api/users/students
Content-Type: application/json

{
  "fullName": "Jane Smith",
  "email": "jane.smith@student.edu",
  "password": "password123",
  "studentId": "STU001",
  "department": "Computer Science",
  "yearOfStudy": 2,
  "contactNumber": "+1234567890"
}
```

### Create Admin (Admin Only)
```http
POST /api/users/admins
Content-Type: application/json

{
  "fullName": "Admin User",
  "email": "admin.user@library.com",
  "password": "securePassword123",
  "adminId": "ADMIN002",
  "department": "Administration",
  "contactNumber": "+1234567890",
  "permissions": "ALL"
}
```

### Get All Users (Paginated)
```http
GET /api/users?page=0&size=10&sort=createdAt
```

### Get User by ID
```http
GET /api/users/{id}
```

### Get User by Email
```http
GET /api/users/email/{email}
```

### Update User
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "fullName": "Updated Name",
  "email": "updated.email@example.com",
  "contactNumber": "+0987654321",
  "employeeId": "EMP002",  // For Librarian
  "branch": "New Branch",  // For Librarian
  "workShift": "Evening"   // For Librarian
}
```

### Deactivate User
```http
PATCH /api/users/{id}/deactivate
```

### Activate User
```http
PATCH /api/users/{id}/activate
```

### Change Password
```http
PATCH /api/users/{id}/change-password
Content-Type: application/json

{
  "currentPassword": "oldPassword",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

## Response Format
All API responses follow this standard format:

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## User Response Model
```json
{
  "id": 1,
  "fullName": "John Doe",
  "email": "john.doe@library.com",
  "contactNumber": "+1234567890",
  "role": "LIBRARIAN",
  "status": "ACTIVE",
  "mustChangePassword": false,
  "createdAt": "2025-09-27T10:00:00",
  "lastPasswordChange": "2025-09-27T10:15:00",
  "employeeId": "EMP001",    // Librarian specific
  "branch": "Main Branch",   // Librarian specific
  "workShift": "Morning",    // Librarian specific
  "studentId": null,         // Student specific
  "department": null,        // Student specific
  "yearOfStudy": null,       // Student specific
  "borrowLimit": null,       // Student specific
  "borrowedCount": null      // Student specific
}
```

## Configuration

### Database Configuration
Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_user_db?createDatabaseIfNotExist=true
    username: root
    password: password
```

### Email Configuration
Update `application.yml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

## Environment Variables
Create a `.env` file or set environment variables:
```bash
SPRING_APPLICATION_NAME=user-service
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/library_user_db?createDatabaseIfNotExist=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@library.com
SERVER_PORT=8081
JWT_SECRET=mySecretKey123
JWT_ACCESS_TOKEN_EXPIRY_MS=86400000
```

## Running the Application

### Prerequisites
- Java 21
- MySQL Server
- Maven

### Build and Run
```bash
# Clone the repository
git clone <repository-url>
cd user-service

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## 🔐 Default Admin Account

When the application starts for the **first time**, it automatically creates a default admin account:

```
📧 Email: admin@library.com
🔑 Password: admin123456
👤 Role: ADMIN
🆔 Admin ID: ADMIN001
🏢 Department: IT Administration
```

### ⚠️ Important Security Notes:
1. **Change the default password immediately** after first login
2. The admin account is only created if no admin accounts exist
3. You can customize the default admin credentials using environment variables:

```bash
DEFAULT_ADMIN_EMAIL=your-admin@library.com
DEFAULT_ADMIN_PASSWORD=your-secure-password
DEFAULT_ADMIN_NAME=Your Admin Name
DEFAULT_ADMIN_ID=ADMIN001
DEFAULT_ADMIN_DEPT=Your Department
```

### Admin Capabilities:
- ✅ Create new user accounts (Students, Librarians, Admins)
- ✅ View all users with pagination
- ✅ Update user information
- ✅ Activate/Deactivate users
- ✅ Full system access

## Use Case Implementation

This service implements the "Manage User Accounts" use case:

### Basic Flow
1. ✅ Admin navigates to user management (API endpoints available)
2. ✅ Admin selects "Add New User" (POST endpoints for students/librarians)
3. ✅ Admin enters user details (Validation implemented)
4. ✅ System validates data and creates account (Comprehensive validation)
5. ✅ System sends welcome email with login instructions (Email service integrated)

### Alternative Flows
- ✅ **A1: Duplicate Email** - System displays error and prevents creation
- ✅ **A2: Email Delivery Failure** - System logs failure and shows warning

### Postconditions
- ✅ Account is created/updated/deactivated
- ✅ Audit log is recorded (via application logging)

## Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Logging
The application provides comprehensive logging:
- User creation/update/deactivation events
- Email sending status
- Error tracking
- API access logs

## Security Features
- JWT-based authentication
- Password hashing with BCrypt
- Token-based authorization
- Role-based access (ADMIN, LIBRARIAN, STUDENT)
- Input validation
- Email format validation
- SQL injection prevention (JPA)
- Secure token generation with user ID, email, and role

## JWT Token Details

### Token Structure
The JWT token contains the following claims:
- `userId`: User's unique identifier
- `email`: User's email address
- `role`: User's role (ADMIN, LIBRARIAN, STUDENT)
- `sub`: Subject (email)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

### Token Usage
Include the JWT token in the Authorization header for protected endpoints:
```http
Authorization: Bearer <your-jwt-token>
```

### Token Expiration
- Default expiration: 24 hours (86400000 ms)
- Configurable via `JWT_ACCESS_TOKEN_EXPIRY_MS` environment variable

## Authentication Flow

### First-Time User Login
1. Admin creates user account (Student/Librarian)
2. System generates temporary password and sends welcome email
3. User logs in with email and temporary password
4. Login response includes `mustChangePassword: true`
5. User must call `/api/auth/change-password-first-time` with current and new password
6. After successful password change, `mustChangePassword` is set to `false`
7. User can now access system normally with new credentials

### Subsequent Logins
1. User logs in with email and permanent password
2. Login response includes `mustChangePassword: false`
3. User has full access to the system

### Login Examples

#### Admin Login (Default Account)
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@library.com",
    "password": "admin123456"
  }'
```

#### Librarian Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "librarian@library.com",
    "password": "temporaryPass123"
  }'
```

#### Student Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@university.edu",
    "password": "temporaryPass123"
  }'
```

#### First-Time Password Change
```bash
curl -X POST http://localhost:8081/api/auth/change-password-first-time \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "currentPassword": "temporaryPass123",
    "newPassword": "MyNewSecurePassword123",
    "confirmPassword": "MyNewSecurePassword123"
  }'
```

#### Using JWT Token
```bash
curl -X GET http://localhost:8081/api/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Future Enhancements
- [ ] Password reset functionality via email
- [ ] Two-factor authentication (2FA)
- [ ] Session management
- [ ] User profile image upload
- [ ] Advanced search and filtering
- [ ] User activity tracking
- [ ] Bulk user operations
- [ ] Integration with other microservices
- [ ] Rate limiting for login attempts
- [ ] Account lockout after failed attempts