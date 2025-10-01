package com.fernando.bookservice.controllers;

import com.fernando.bookservice.dto.*;
import com.fernando.bookservice.services.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class BookController {
    
    private final BookService bookService;
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Book Service is running", "OK"));
    }
    
    // Fetch book metadata by ISBN (Step 1: Auto-fill)
    @GetMapping("/metadata/{isbn}")
    public ResponseEntity<ApiResponse<BookMetadataResponse.BookMetadata>> fetchBookMetadata(@PathVariable String isbn) {
        log.info("Fetching metadata for ISBN: {}", isbn);
        
        try {
            BookMetadataResponse metadataResponse = bookService.fetchBookMetadata(isbn);
            
            if (metadataResponse.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("Metadata retrieved successfully", metadataResponse.getData()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(metadataResponse.getMessage()));
            }
        } catch (Exception e) {
            log.error("Error fetching metadata for ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch book metadata"));
        }
    }
    
    // Create a new book with metadata auto-fill (Librarian/Admin only)
    @PostMapping("/with-metadata")
    public ResponseEntity<ApiResponse<BookResponse>> createBookWithMetadata(@Valid @RequestBody CreateBookWithMetadataRequest request) {
        log.info("Creating new book with ISBN: {} (auto-fill: {})", 
                request.getIsbn(), !request.getSkipMetadataFetch());
        
        try {
            BookResponse bookResponse = bookService.createBookWithMetadata(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Book created successfully", bookResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create book"));
        }
    }
    
    // Create a new book (Legacy - Librarian/Admin only)
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody CreateBookRequest request) {
        log.info("Creating new book with ISBN: {}", request.getIsbn());
        
        try {
            BookResponse bookResponse = bookService.createBook(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Book created successfully", bookResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create book"));
        }
    }
    
    // Get all books with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAllBooks(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        log.info("Fetching all books with pagination");
        
        try {
            Page<BookResponse> books = bookService.getAllBooks(pageable);
            return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
        } catch (Exception e) {
            log.error("Error fetching books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch books"));
        }
    }
    
    // Get book by ISBN
    @GetMapping("/{isbn}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookByIsbn(@PathVariable String isbn) {
        log.info("Fetching book with ISBN: {}", isbn);
        
        try {
            BookResponse bookResponse = bookService.getBookByIsbn(isbn);
            return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", bookResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching book with ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch book"));
        }
    }
    
    // Update book details (Librarian/Admin only)
    @PutMapping("/{isbn}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable String isbn, 
            @Valid @RequestBody UpdateBookRequest request) {
        log.info("Updating book with ISBN: {}", isbn);
        
        try {
            BookResponse bookResponse = bookService.updateBook(isbn, request);
            return ResponseEntity.ok(ApiResponse.success("Book updated successfully", bookResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating book with ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update book"));
        }
    }
    
    // Delete book (Librarian/Admin only)
    @DeleteMapping("/{isbn}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String isbn) {
        log.info("Deleting book with ISBN: {}", isbn);
        
        try {
            bookService.deleteBook(isbn);
            return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting book with ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete book"));
        }
    }
    
    // Search books
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        log.info("Searching books with criteria - Title: {}, Author: {}, Genre: {}, Status: {}", 
                title, author, genre, status);
        
        try {
            Page<BookResponse> books = bookService.searchBooks(title, author, genre, status, pageable);
            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", books));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error searching books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search books"));
        }
    }
    
    // Get available books only
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAvailableBooks(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        log.info("Fetching available books");
        
        try {
            Page<BookResponse> books = bookService.getAvailableBooks(pageable);
            return ResponseEntity.ok(ApiResponse.success("Available books retrieved successfully", books));
        } catch (Exception e) {
            log.error("Error fetching available books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch available books"));
        }
    }
    
    // Get all genres
    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getAllGenres() {
        log.info("Fetching all book genres");
        
        try {
            List<String> genres = bookService.getAllGenres();
            return ResponseEntity.ok(ApiResponse.success("Genres retrieved successfully", genres));
        } catch (Exception e) {
            log.error("Error fetching genres", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch genres"));
        }
    }
    
    // Mark book copies as lost (Librarian/Admin only)
    @PatchMapping("/{isbn}/mark-lost")
    public ResponseEntity<ApiResponse<BookResponse>> markBookAsLost(
            @PathVariable String isbn, 
            @Valid @RequestBody MarkBookLostRequest request) {
        log.info("Marking {} copies as lost for book with ISBN: {}", request.getCopiesToMarkLost(), isbn);
        
        try {
            BookResponse bookResponse = bookService.markBookAsLost(isbn, request);
            return ResponseEntity.ok(ApiResponse.success("Book copies marked as lost successfully", bookResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking book copies as lost for ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark book copies as lost"));
        }
    }
    
    // Add copies to existing book (Librarian/Admin only)
    @PatchMapping("/{isbn}/add-copies")
    public ResponseEntity<ApiResponse<BookResponse>> addCopies(
            @PathVariable String isbn, 
            @Valid @RequestBody AddCopiesRequest request) {
        log.info("Adding {} copies to book with ISBN: {}", request.getCopiesToAdd(), isbn);
        
        try {
            BookResponse bookResponse = bookService.addCopies(isbn, request);
            return ResponseEntity.ok(ApiResponse.success("Book copies added successfully", bookResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding copies to book with ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add book copies"));
        }
    }
    
    // Get low stock books (Librarian/Admin only)
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getLowStockBooks(
            @RequestParam(defaultValue = "5") Integer threshold) {
        log.info("Fetching books with low stock (threshold: {})", threshold);
        
        try {
            List<BookResponse> books = bookService.getLowStockBooks(threshold);
            return ResponseEntity.ok(ApiResponse.success("Low stock books retrieved successfully", books));
        } catch (Exception e) {
            log.error("Error fetching low stock books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch low stock books"));
        }
    }
    
    // Decrease available copies (called by borrow-service when book is borrowed)
    @PutMapping("/{isbn}/decrease-copies")
    public ResponseEntity<ApiResponse<Void>> decreaseAvailableCopies(@PathVariable String isbn) {
        log.info("Decreasing available copies for book with ISBN: {}", isbn);
        
        try {
            bookService.decreaseAvailableCopies(isbn);
            return ResponseEntity.ok(ApiResponse.success("Available copies decreased successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error decreasing available copies for ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to decrease available copies"));
        }
    }
    
    // Increase available copies (called by borrow-service when book is returned)
    @PutMapping("/{isbn}/increase-copies")
    public ResponseEntity<ApiResponse<Void>> increaseAvailableCopies(@PathVariable String isbn) {
        log.info("Increasing available copies for book with ISBN: {}", isbn);
        
        try {
            bookService.increaseAvailableCopies(isbn);
            return ResponseEntity.ok(ApiResponse.success("Available copies increased successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error increasing available copies for ISBN: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to increase available copies"));
        }
    }
}