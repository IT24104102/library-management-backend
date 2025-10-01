package com.disanayake.borrowservice.controllers;

import com.disanayake.borrowservice.dto.*;
import com.disanayake.borrowservice.services.BorrowService;
import com.disanayake.borrowservice.services.ReservationService;
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
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class BorrowController {
    
    private final BorrowService borrowService;
    private final ReservationService reservationService;
    
    // ========== RESERVATION ENDPOINTS (for Students) ==========
    
    // Reserve a book (Student only)
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<ReservationResponse>> reserveBook(@Valid @RequestBody ReserveBookRequest request) {
        log.info("Processing reservation request for user {} and book {}", request.getUserId(), request.getIsbn());
        
        try {
            ReservationResponse reservationResponse = reservationService.reserveBook(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Book reserved successfully", reservationResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing reservation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to reserve book"));
        }
    }
    
    // Get user's reservations
    @GetMapping("/user/{userId}/reservations")
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getUserReservations(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "reservationDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching reservations for user: {}", userId);
        
        try {
            Page<ReservationResponse> reservations = reservationService.getUserReservations(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("User reservations retrieved successfully", reservations));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching user reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reservations"));
        }
    }
    
    // Get user's active reservations
    @GetMapping("/user/{userId}/reservations/active")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getUserActiveReservations(@PathVariable Long userId) {
        log.info("Fetching active reservations for user: {}", userId);
        
        try {
            List<ReservationResponse> reservations = reservationService.getUserActiveReservations(userId);
            return ResponseEntity.ok(ApiResponse.success("Active reservations retrieved successfully", reservations));
        } catch (Exception e) {
            log.error("Error fetching active reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch active reservations"));
        }
    }
    
    // Cancel reservation
    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam Long userId) {
        log.info("Cancelling reservation {} for user {}", reservationId, userId);
        
        try {
            ReservationResponse reservationResponse = reservationService.cancelReservation(reservationId, userId);
            return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully", reservationResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error cancelling reservation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cancel reservation"));
        }
    }
    
    // ========== LOAN CREATION ENDPOINTS (for Librarians) ==========
    
    // Create loan (Librarian checkout - replaces the old borrow endpoint)
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("Processing loan creation request for user {} and book {} by librarian {}", 
                request.getUserId(), request.getIsbn(), request.getLibrarianId());
        
        try {
            BorrowRecordResponse loanResponse = borrowService.createLoan(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Loan created successfully", loanResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing loan creation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create loan"));
        }
    }
    
    // Get book's reservations (for librarians to see who has reserved the book)
    @GetMapping("/book/{isbn}/reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getBookReservations(@PathVariable String isbn) {
        log.info("Fetching reservations for book: {}", isbn);
        
        try {
            List<ReservationResponse> reservations = reservationService.getBookReservations(isbn);
            return ResponseEntity.ok(ApiResponse.success("Book reservations retrieved successfully", reservations));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching book reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch book reservations"));
        }
    }
    
    // ========== EXISTING LOAN MANAGEMENT ENDPOINTS ==========
    
    // Borrow a book (Student only) - DEPRECATED: Use reservation system instead
    @PostMapping("/borrow")
    @Deprecated
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> borrowBook(@Valid @RequestBody BorrowBookRequest request) {
        log.info("Processing borrow request for user {} and book {}", request.getUserId(), request.getIsbn());
        log.warn("Direct borrow endpoint is deprecated. Please use reservation system: /reserve followed by librarian checkout");
        
        try {
            BorrowRecordResponse borrowResponse = borrowService.borrowBook(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Book borrowed successfully (deprecated - use reservation system)", borrowResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing borrow request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to borrow book"));
        }
    }
    
    // Return a book (Student/Librarian) - Return/Update
    @PostMapping("/return")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> returnBook(@Valid @RequestBody ReturnBookRequest request) {
        log.info("Processing return request for user {} and book {}", request.getUserId(), request.getIsbn());
        
        try {
            BorrowRecordResponse returnResponse = borrowService.returnBook(request);
            return ResponseEntity.ok(ApiResponse.success("Book returned successfully", returnResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing return request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to return book"));
        }
    }
    
    // Renew a loan (Student) - Renew/Update
    @PostMapping("/renew")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> renewLoan(@Valid @RequestBody RenewLoanRequest request) {
        log.info("Processing renewal request for user {} and borrow record {}", 
                request.getUserId(), request.getBorrowRecordId());
        
        try {
            BorrowRecordResponse renewResponse = borrowService.renewLoan(request);
            return ResponseEntity.ok(ApiResponse.success("Loan renewed successfully", renewResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing renewal request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to renew loan"));
        }
    }
    
    // Get user's loan history
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<ApiResponse<Page<BorrowRecordResponse>>> getUserLoanHistory(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "borrowDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching loan history for user: {}", userId);
        
        try {
            Page<BorrowRecordResponse> loanHistory = borrowService.getUserBorrowHistory(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("Loan history retrieved successfully", loanHistory));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching loan history for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch loan history"));
        }
    }
    
    // Get book's loan history
    @GetMapping("/book/{isbn}/history")
    public ResponseEntity<ApiResponse<Page<BorrowRecordResponse>>> getBookLoanHistory(
            @PathVariable String isbn,
            @PageableDefault(size = 10, sort = "borrowDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching loan history for book: {}", isbn);
        
        try {
            Page<BorrowRecordResponse> loanHistory = borrowService.getBookBorrowHistory(isbn, pageable);
            return ResponseEntity.ok(ApiResponse.success("Book loan history retrieved successfully", loanHistory));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching loan history for book: {}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch book loan history"));
        }
    }
    
    // Get user's active loans
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> getUserActiveLoans(@PathVariable Long userId) {
        log.info("Fetching active loans for user: {}", userId);
        
        try {
            List<BorrowRecordResponse> activeLoans = borrowService.getUserActiveBorrows(userId);
            return ResponseEntity.ok(ApiResponse.success("Active loans retrieved successfully", activeLoans));
        } catch (Exception e) {
            log.error("Error fetching active loans for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch active loans"));
        }
    }
    
    // Get all loan records (Librarian/Admin only)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BorrowRecordResponse>>> getAllLoanRecords(
            @PageableDefault(size = 10, sort = "borrowDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching all loan records with pagination");
        
        try {
            Page<BorrowRecordResponse> allLoans = borrowService.getAllBorrowRecords(pageable);
            return ResponseEntity.ok(ApiResponse.success("All loan records retrieved successfully", allLoans));
        } catch (Exception e) {
            log.error("Error fetching all loan records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch loan records"));
        }
    }
    
    // Get overdue books (Librarian/Admin only)
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> getOverdueBooks() {
        log.info("Fetching overdue books");
        
        try {
            List<BorrowRecordResponse> overdueBooks = borrowService.getOverdueBooks();
            return ResponseEntity.ok(ApiResponse.success("Overdue books retrieved successfully", overdueBooks));
        } catch (Exception e) {
            log.error("Error fetching overdue books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch overdue books"));
        }
    }
    
    // Get books due soon (Librarian/Admin only)
    @GetMapping("/due-soon")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> getBooksDueSoon(
            @RequestParam(defaultValue = "3") int days) {
        log.info("Fetching books due within {} days", days);
        
        try {
            List<BorrowRecordResponse> booksDueSoon = borrowService.getBooksDueSoon(days);
            return ResponseEntity.ok(ApiResponse.success("Books due soon retrieved successfully", booksDueSoon));
        } catch (Exception e) {
            log.error("Error fetching books due soon", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch books due soon"));
        }
    }
    
    // Mark book as lost (Librarian/Admin only)
    @PostMapping("/mark-lost")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> markBookAsLost(@Valid @RequestBody MarkBookAsLostRequest request) {
        log.info("Processing mark as lost request for borrow record {}", request.getBorrowRecordId());
        
        try {
            BorrowRecordResponse lostResponse = borrowService.markBookAsLost(request);
            return ResponseEntity.ok(ApiResponse.success("Book marked as lost successfully", lostResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking book as lost", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark book as lost"));
        }
    }
    
    // Update overdue status (Internal/Admin only)
    @PostMapping("/update-overdue")
    public ResponseEntity<ApiResponse<Void>> updateOverdueStatus() {
        log.info("Updating overdue status for all active loans");
        
        try {
            borrowService.updateOverdueStatus();
            return ResponseEntity.ok(ApiResponse.success("Overdue status updated successfully", null));
        } catch (Exception e) {
            log.error("Error updating overdue status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update overdue status"));
        }
    }
}