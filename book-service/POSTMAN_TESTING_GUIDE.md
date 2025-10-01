# Book Service API Testing Guide

## 📋 Overview

This guide provides comprehensive instructions for testing the Book Service microservice using the provided Postman collection. The Book Service handles book catalog management and features ISBN-based metadata auto-fill functionality. 

**Note**: Borrowing operations are now handled by the dedicated borrow-service microservice on port 8083.

## 🚀 Quick Setup

### Prerequisites
1. **Book Service Running**: Ensure the book-service is running on `http://localhost:8082`
2. **User Service Running**: User validation requires user-service on `http://localhost:8081`
3. **MySQL Database**: Database should be accessible and configured
4. **Postman**: Latest version of Postman installed

### Import Collection & Environment

1. **Import Collection**:
   - Open Postman
   - Click "Import" → "Upload Files"
   - Select `Book-Service-API.postman_collection.json`

2. **Import Environment**:
   - Click "Import" → "Upload Files"
   - Select `Book-Service-Local.postman_environment.json`
   - Set as active environment

## 🧪 Testing Scenarios

### 1. Service Health Check

**Purpose**: Verify the service is running and accessible.

```
GET {{baseUrl}}/api/books/health
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Book Service is running",
  "data": "OK"
}
```

### 2. Book Metadata Auto-fill Testing

#### Scenario A: Successful Metadata Fetch
```
GET {{baseUrl}}/api/books/metadata/9780132350884
```

**Expected**: Returns book metadata from external APIs (Google Books/Open Library)

#### Scenario B: ISBN Not Found
```
GET {{baseUrl}}/api/books/metadata/1234567890123
```

**Expected**: Returns error message indicating metadata not found

### 3. Enhanced Book Creation Workflow

#### Scenario A: Create Book with Auto-fill
```json
POST {{baseUrl}}/api/books/with-metadata
{
  "isbn": "9780132350884",
  "totalCopies": 5,
  "shelfLocation": "CS-101",
  "skipMetadataFetch": false
}
```

**Expected**: Book created with auto-filled metadata from external APIs

#### Scenario B: Create Book with Manual Override
```json
POST {{baseUrl}}/api/books/with-metadata
{
  "isbn": "9780439708180",
  "title": "Custom Title Override",
  "author": "J.K. Rowling",
  "totalCopies": 3,
  "skipMetadataFetch": true,
  "shelfLocation": "FICTION-HP"
}
```

**Expected**: Book created with provided data, metadata fetch skipped

### 4. Book Management Operations

#### Get All Books (with Pagination)
```
GET {{baseUrl}}/api/books?page=0&size=10&sort=title
```

#### Search Books
```
GET {{baseUrl}}/api/books/search?title=Clean&author=Martin&genre=Computer Science
```

#### Update Book
```json
PUT {{baseUrl}}/api/books/9780132350884
{
  "title": "Updated Book Title",
  "totalCopies": 8,
  "status": "AVAILABLE"
}
```

#### Mark Book as Lost
```json
PUT {{baseUrl}}/api/books/9781234567890
{
  "status": "LOST"
}
```

### 5. Copy Management Operations

#### Decrease Available Copies (for integration with borrow-service)
```json
PUT {{baseUrl}}/api/books/9780132350884/copies/decrease
```

**Expected**: Available copies decreased by 1, HTTP 200 OK

#### Increase Available Copies (for integration with borrow-service)
```json
PUT {{baseUrl}}/api/books/9780132350884/copies/increase
```

**Expected**: Available copies increased by 1, HTTP 200 OK

### 6. Library Management Features

#### Get Low Stock Books
```
GET {{baseUrl}}/api/books/low-stock?threshold=5
```

## 🔍 Error Scenarios Testing

### 1. Validation Errors

#### Invalid ISBN Format
```json
POST {{baseUrl}}/api/books
{
  "isbn": "invalid-isbn",
  "title": "Test Book",
  "author": "Test Author",
  "totalCopies": 1
}
```

**Expected**: HTTP 400 with validation error details

#### Missing Required Fields
```json
POST {{baseUrl}}/api/books
{
  "isbn": "9781234567890"
}
```

**Expected**: HTTP 400 with missing field errors

### 2. Business Logic Errors

#### Try to Delete Book with Active Borrows
```json
DELETE {{baseUrl}}/api/books/9780132350884
```

**Note**: If this book has active borrows in the borrow-service, it should be protected from deletion

**Expected**: HTTP 400 "Cannot delete book with active borrows"

## 📊 Test Data Recommendations

### Sample ISBNs for Testing

1. **Clean Architecture**: `9780132350884`
2. **Harry Potter**: `9780439708180`  
3. **Design Patterns**: `9780201633610`
4. **Java Effective**: `9780134685991`
5. **Custom Book**: `9781234567890`

### Test Users (from user-service)

1. **Student User**: ID 1 (role: STUDENT)
2. **Librarian User**: ID 2 (role: LIBRARIAN)
3. **Admin User**: ID 3 (role: ADMIN)

## 🔄 Testing Workflow

### Complete Book Management Test

1. **Create Book**: Use metadata auto-fill
2. **Search Book**: Verify it appears in search
3. **Update Book**: Modify book details
4. **Check Availability**: Verify book status
5. **Delete Book**: Remove book from catalog (if no active borrows)

### Integration Testing with Borrow Service

1. **Create Book**: Add a new book via book-service
2. **Decrease Copies**: Simulate borrow via copies/decrease endpoint
3. **Check Availability**: Verify available copies decreased
4. **Increase Copies**: Simulate return via copies/increase endpoint
5. **Verify Update**: Check copies were restored

## 🛠️ Troubleshooting

### Common Issues

1. **Service Not Responding**:
   - Check if service is running on port 8082
   - Verify database connection
   - Check application logs

2. **Service Integration Issues**:
   - Check connectivity to borrow-service for copy management
   - Verify user-service is accessible for user validation
   - Test inter-service communication endpoints

3. **Metadata Not Loading**:
   - Check internet connection
   - Verify external API access (Google Books/Open Library)
   - Check API rate limits

4. **Database Errors**:
   - Verify MySQL is running
   - Check database credentials
   - Ensure database schema is created

### Debug Endpoints

- Health Check: `GET /api/books/health`
- All Books: `GET /api/books`
- Book by ISBN: `GET /api/books/{isbn}`
- Copy Management: `PUT /api/books/{isbn}/copies/increase|decrease`

## 📈 Performance Testing

### Load Testing Scenarios

1. **Concurrent Book Creation**: Multiple books created simultaneously
2. **Search Performance**: Large result set searches
3. **Metadata API Load**: Multiple ISBN lookups
4. **Copy Management Load**: Multiple copy increase/decrease operations

### Recommended Tools

- **Postman Runner**: For collection-based load testing
- **JMeter**: For detailed performance testing
- **Newman**: For CI/CD integration

## 📝 Test Report Template

### Test Execution Summary

- **Total Tests**: X
- **Passed**: X
- **Failed**: X
- **Skipped**: X

### Failed Test Details

1. **Test Name**: 
   - **Error**: 
   - **Expected**: 
   - **Actual**: 
   - **Action**: 

### Performance Metrics

- **Average Response Time**: X ms
- **95th Percentile**: X ms
- **Error Rate**: X%
- **Throughput**: X requests/sec

---

**Note**: Always test in a dedicated test environment before deploying to production. Ensure test data doesn't interfere with production data.