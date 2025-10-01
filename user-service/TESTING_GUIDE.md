# Testing Guide - User Service API

## Quick Setup in Postman

### 1. Import Files
1. Open Postman
2. Click "Import" → "Upload Files"
3. Import these files:
   - `User-Service-API.postman_collection.json`
   - `User-Service-Local.postman_environment.json`
4. Select the "User Service - Local" environment

### 2. Start Your Application
```bash
cd user-service
mvn spring-boot:run
```

### 3. Testing Flow

#### Step 1: Health Check ✅
- Run: `GET Health Check`
- Expected: Status 200, "User Service is running"

#### Step 2: Login as Admin ✅
- Run: `Login - Default Admin`
- Expected: Status 200, JWT token auto-saved
- Note: Default credentials are `admin@library.com` / `admin123456`

#### Step 3: Create Test Users ✅
1. Run: `Create Librarian`
   - Expected: Status 201, user created
   - Note: Temporary password will be shown in logs
   
2. Run: `Create Student` 
   - Expected: Status 201, user created
   - Note: Temporary password will be shown in logs

#### Step 4: Test User Management ✅
1. Run: `Get All Users`
   - Expected: Paginated list of users
   
2. Run: `Get User by ID` (change ID to 1, 2, 3...)
   - Expected: User details
   
3. Run: `Get User by Email`
   - Expected: User details

#### Step 5: Test User Login with Temporary Password ✅
1. Update the email/password in `Login - Librarian`
2. Use the temporary password from the logs
3. Expected: Login successful with `mustChangePassword: true`

#### Step 6: Change Password First Time ✅
1. Run: `Change Password First Time`
2. Use temporary password as current, set new password
3. Expected: Password changed successfully

#### Step 7: Login with New Password ✅
1. Login again with the new password
2. Expected: Login successful with `mustChangePassword: false`

#### Step 8: Test User Operations ✅
1. Run: `Update User` (modify user details)
2. Run: `Deactivate User` (deactivate a user)  
3. Run: `Activate User` (reactivate the user)
4. Run: `Change Password` (admin changes user password)

## Common Test Scenarios

### Scenario 1: New Librarian Onboarding
```
1. Admin creates librarian account ✓
2. Librarian receives welcome email with temp password ✓
3. Librarian logs in with temp password ✓
4. System forces password change ✓
5. Librarian sets new password ✓
6. Librarian can access system normally ✓
```

### Scenario 2: User Management
```
1. Admin views all users ✓
2. Admin searches for specific user ✓
3. Admin updates user information ✓
4. Admin deactivates problematic user ✓
5. Admin reactivates user when needed ✓
```

### Scenario 3: Error Handling
```
1. Try creating user with existing email → Error ✓
2. Try login with wrong password → Error ✓
3. Try accessing protected endpoint without token → Error ✓
4. Try using expired/invalid token → Error ✓
```

## Expected API Responses

### ✅ Success Response Format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### ❌ Error Response Format:
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Sample JWT Token Claims:
```json
{
  "userId": 1,
  "email": "admin@library.com", 
  "role": "ADMIN",
  "sub": "admin@library.com",
  "iat": 1695816000,
  "exp": 1695902400
}
```

## Troubleshooting

### Issue: 401 Unauthorized
**Solution:** Check if access token is set in environment variables

### Issue: 400 Bad Request  
**Solution:** Verify request body format and required fields

### Issue: 500 Internal Server Error
**Solution:** Check application logs for detailed error information

### Issue: Connection Refused
**Solution:** Ensure the Spring Boot application is running on port 8081

## Test Data

### Default Admin Account:
- Email: `admin@library.com`
- Password: `admin123456`
- Role: `ADMIN`

### Test Librarian Data:
```json
{
  "fullName": "John Doe",
  "email": "john.doe@library.com", 
  "employeeId": "EMP001",
  "branch": "Main Branch",
  "workShift": "Morning"
}
```

### Test Student Data:
```json
{
  "fullName": "Jane Smith",
  "email": "jane.smith@student.edu",
  "studentId": "STU001", 
  "department": "Computer Science",
  "yearOfStudy": 2
}
```

## Security Testing

### Authentication Tests:
- ✅ Valid credentials → Success
- ❌ Invalid credentials → 401 Unauthorized  
- ❌ Missing credentials → 400 Bad Request
- ❌ Malformed JWT → 401 Unauthorized

### Authorization Tests:
- ✅ Valid JWT token → Access granted
- ❌ No JWT token → 401 Unauthorized
- ❌ Expired JWT token → 401 Unauthorized
- ❌ Invalid JWT signature → 401 Unauthorized

### Input Validation Tests:
- ❌ Invalid email format → 400 Bad Request
- ❌ Short password → 400 Bad Request  
- ❌ Missing required fields → 400 Bad Request
- ❌ Duplicate email → 400 Bad Request

## Performance Testing (Optional)

### Load Testing with Newman (Postman CLI):
```bash
# Install Newman
npm install -g newman

# Run collection 100 times
newman run User-Service-API.postman_collection.json \
  -e User-Service-Local.postman_environment.json \
  -n 100 --delay-request 100
```

## Database Verification

After testing, verify in your MySQL database:
```sql
USE library_user_db;
SELECT id, full_name, email, role, status, must_change_password FROM users;
```

Expected users:
1. System Administrator (ADMIN)
2. John Doe (LIBRARIAN) 
3. Jane Smith (STUDENT)