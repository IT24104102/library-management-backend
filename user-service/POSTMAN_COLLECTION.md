# User Service API - Postman Collection

## Environment Variables
First, set up these environment variables in Postman:
- `baseUrl`: `http://localhost:8081`
- `accessToken`: (will be set after login)

## 1. Health Check

### GET Health Check
```
GET {{baseUrl}}/api/users/health
```

## 2. Authentication Endpoints

### POST Login - Default Admin
```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "admin@library.com",
  "password": "admin123456"
}
```

### POST Login - Librarian (After Creation)
```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "john.doe@library.com",
  "password": "TemporaryPassword123"
}
```

### POST Login - Student (After Creation)
```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "jane.smith@student.edu",
  "password": "TemporaryPassword123"
}
```

### POST Validate Token
```
POST {{baseUrl}}/api/auth/validate
Authorization: Bearer {{accessToken}}
```

### GET User Profile
```
GET {{baseUrl}}/api/auth/profile
Authorization: Bearer {{accessToken}}
```

### POST Change Password First Time
```
POST {{baseUrl}}/api/auth/change-password-first-time
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "currentPassword": "TemporaryPassword123",
  "newPassword": "MyNewSecurePassword123",
  "confirmPassword": "MyNewSecurePassword123"
}
```

## 3. User Management Endpoints

### POST Create Librarian
```
POST {{baseUrl}}/api/users/librarians
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@library.com",
  "employeeId": "EMP001",
  "branch": "Main Branch",
  "workShift": "Morning",
  "contactNumber": "+1234567890"
}
```

### POST Create Student
```
POST {{baseUrl}}/api/users/students
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "fullName": "Jane Smith",
  "email": "jane.smith@student.edu",
  "studentId": "STU001",
  "department": "Computer Science",
  "yearOfStudy": 2,
  "contactNumber": "+1987654321"
}
```

### POST Create Admin
```
POST {{baseUrl}}/api/users/admins
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "fullName": "Admin User",
  "email": "admin.user@library.com",
  "adminId": "ADMIN002",
  "department": "Administration",
  "contactNumber": "+1122334455",
  "permissions": "ALL"
}
```

### GET All Users (Paginated)
```
GET {{baseUrl}}/api/users?page=0&size=10&sort=createdAt,desc
Authorization: Bearer {{accessToken}}
```

### GET User by ID
```
GET {{baseUrl}}/api/users/1
Authorization: Bearer {{accessToken}}
```

### GET User by Email
```
GET {{baseUrl}}/api/users/email/admin@library.com
Authorization: Bearer {{accessToken}}
```

### PUT Update User
```
PUT {{baseUrl}}/api/users/1
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "fullName": "Updated Admin Name",
  "email": "updated.admin@library.com",
  "contactNumber": "+1999888777",
  "adminId": "ADMIN001",
  "department": "IT Administration"
}
```

### PATCH Deactivate User
```
PATCH {{baseUrl}}/api/users/2/deactivate
Authorization: Bearer {{accessToken}}
```

### PATCH Activate User
```
PATCH {{baseUrl}}/api/users/2/activate
Authorization: Bearer {{accessToken}}
```

### PATCH Change Password
```
PATCH {{baseUrl}}/api/users/1/change-password
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "currentPassword": "admin123456",
  "newPassword": "newSecurePassword123",
  "confirmPassword": "newSecurePassword123"
}
```

## 4. Test Scripts for Postman

### Auto-set Access Token (Add to Login request Tests tab):
```javascript
pm.test("Login successful", function () {
    pm.response.to.have.status(200);
    
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data.accessToken) {
        pm.environment.set("accessToken", jsonData.data.accessToken);
        console.log("Access token set:", jsonData.data.accessToken);
    }
});
```

### Validate Response Structure (Add to any request Tests tab):
```javascript
pm.test("Response has correct structure", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('success');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
});

pm.test("Status code is 200 or 201", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});
```

## 5. Complete Test Flow

### Step 1: Health Check
- Run the health check to ensure the service is running

### Step 2: Login as Default Admin
- Use the default admin credentials
- The access token will be automatically set

### Step 3: Create Test Users
- Create a librarian
- Create a student
- Note down their emails and temporary passwords from the welcome email logs

### Step 4: Test User Login
- Login with the librarian credentials using temporary password
- Login with the student credentials using temporary password
- Notice `mustChangePassword: true` in the response

### Step 5: Change Password First Time
- Use the first-time password change endpoint
- Login again with new password
- Notice `mustChangePassword: false` in the response

### Step 6: Test User Management
- Get all users
- Get user by ID
- Update user information
- Activate/Deactivate users

## 6. Expected Responses

### Successful Login Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "fullName": "System Administrator",
      "email": "admin@library.com",
      "role": "ADMIN",
      "status": "ACTIVE",
      "mustChangePassword": false
    }
  }
}
```

### User Creation Response:
```json
{
  "success": true,
  "message": "Librarian account created successfully. Welcome email sent.",
  "data": {
    "id": 2,
    "fullName": "John Doe",
    "email": "john.doe@library.com",
    "contactNumber": "+1234567890",
    "role": "LIBRARIAN",
    "status": "ACTIVE",
    "mustChangePassword": true,
    "createdAt": "2025-09-27T10:30:00",
    "lastPasswordChange": null,
    "employeeId": "EMP001",
    "branch": "Main Branch",
    "workShift": "Morning"
  }
}
```

### Error Response Example:
```json
{
  "success": false,
  "message": "Email already exists: john.doe@library.com",
  "data": null
}
```

## 7. Environment Setup

Create these environments in Postman:

### Local Development
- `baseUrl`: `http://localhost:8081`

### Production (when deployed)
- `baseUrl`: `https://your-production-url.com`

## 8. Collection Variables
Set these at the collection level:
- `adminEmail`: `admin@library.com`
- `adminPassword`: `admin123456`

## 9. Pre-request Scripts

### Auto-login if token is missing (Collection level):
```javascript
// Check if we have a valid token
if (!pm.environment.get("accessToken")) {
    console.log("No access token found. Attempting to login...");
    
    pm.sendRequest({
        url: pm.environment.get("baseUrl") + "/api/auth/login",
        method: 'POST',
        header: {
            'Content-Type': 'application/json',
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                email: pm.collectionVariables.get("adminEmail"),
                password: pm.collectionVariables.get("adminPassword")
            })
        }
    }, function (err, response) {
        if (response.json().success) {
            pm.environment.set("accessToken", response.json().data.accessToken);
            console.log("Auto-login successful");
        }
    });
}
```