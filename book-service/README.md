# Book Service - Library Management System

## Overview
The Book Service is a microservice responsible for managing the book catalog in the Library Management System. It features **ISBN-based metadata auto-fill** for streamlined book cataloging and provides copy management endpoints for integration with the borrow-service.

**Note**: Borrowing and returning operations are now handled by the dedicated borrow-service microservice.

## 🚀 Key Features

### 📚 Enhanced Book Management
- **ISBN Metadata Auto-Fill** from Google Books API and Open Library
- **Smart Book Creation Workflow** with librarian confirmation
- **Manual Entry Fallback** when metadata is unavailable
- **CRUD Operations** with comprehensive validation
- **Advanced Search** by title, author, genre, status
- **Inventory Management** with low stock alerts
- **Book Status Tracking** (AVAILABLE, UNAVAILABLE, LOST, MAINTENANCE)

### 📖 Copy Management for Borrow Service Integration
- **Copy Decrease/Increase Endpoints** for borrow-service integration
- **Available Copy Tracking** synchronized with borrowing operations
- **Inventory Protection** prevents deletion of books with active borrows
- **Inter-service Communication** with borrow-service

### 🔍 External API Integration
- **Google Books API** for comprehensive book metadata
- **Open Library API** as reliable fallback
- **Automatic Field Population** (title, author, publisher, etc.)
- **Borrow Service Integration** for copy management operations

## 📋 Enhanced Book Creation Workflow

### Basic Flow (Recommended)
1. **Librarian selects "Add Book"**
2. **Librarian scans or enters ISBN**
3. **System auto-fills details from external metadata service**
4. **Librarian confirms and adds book to catalog**

### Alternative Flow (Manual Entry)
- **3a.** If ISBN not found, librarian manually enters title, author, and details

### Implementation Example
```javascript
// Step 1: Fetch metadata
const metadata = await fetch(`/api/books/metadata/${isbn}`);

// Step 2: Auto-fill or show manual form
if (metadata.success) {
  populateForm(metadata.data);
} else {
  showManualEntryForm();
}

// Step 3: Create book with confirmation
await createBook(formData);
```

## 🛠 API Endpoints

### 📚 Book Metadata & Creation
- `GET /api/books/metadata/{isbn}` - **Fetch book metadata for auto-fill**
- `POST /api/books/with-metadata` - **Create book with metadata auto-fill**
- `POST /api/books` - Create book (legacy manual entry)

### 📖 Book Management  
- `GET /api/books/health` - Health check
- `GET /api/books` - Get all books (paginated)
- `GET /api/books/{isbn}` - Get book by ISBN
- `PUT /api/books/{isbn}` - Update book details
- `DELETE /api/books/{isbn}` - Delete book
- `GET /api/books/search` - Advanced search with filters
- `GET /api/books/available` - Get available books only
- `GET /api/books/genres` - Get all genres
- `GET /api/books/low-stock` - Get books with low inventory

### 🔄 Copy Management (for borrow-service integration)
- `PUT /api/books/{isbn}/copies/decrease` - Decrease available copies (called by borrow-service)
- `PUT /api/books/{isbn}/copies/increase` - Increase available copies (called by borrow-service)

### Configuration

### Database
- MySQL database: `book_service_db`
- Port: 8082
- Auto-creates database if not exists

### Borrow Service Integration
- Provides copy management endpoints for borrow-service
- Maintains book availability synchronization
- Protects books with active borrows from deletion

### Business Rules
- Books can only be deleted if no active borrows exist
- Available copies are managed via dedicated endpoints
- Copy management operations are restricted to service-to-service calls

## Running the Service

### Prerequisites
- Java 21
- MySQL 8.0+
- Borrow Service for borrowing operations (port 8083)

### Environment Variables
```bash
DB_USERNAME=root
DB_PASSWORD=your_password
```

### Start the Service
```bash
./mvnw spring-boot:run
```

The service will be available at `http://localhost:8082`

## Sample Data
The service includes sample book data that is automatically loaded on startup for testing purposes.

## Error Handling
- Comprehensive validation for all inputs
- Proper HTTP status codes
- Detailed error messages
- Global exception handling