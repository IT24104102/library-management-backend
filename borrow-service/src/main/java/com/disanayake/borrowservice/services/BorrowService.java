package com.disanayake.borrowservice.services;

import com.disanayake.borrowservice.dto.*;
import com.disanayake.borrowservice.entities.BorrowRecord;
import com.disanayake.borrowservice.repositories.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {
    
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService bookService;
    private final UserValidationService userValidationService;
    private final ReservationService reservationService;
    private final PaymentServiceClient paymentServiceClient;
    
    // Maximum books a student can borrow
    private static final int MAX_BORROW_LIMIT = 5;
    // Loan period in days
    private static final int LOAN_PERIOD_DAYS = 14;
    // Fine per day for overdue books
    private static final double FINE_PER_DAY = 1.0;
    // Maximum renewal times
    private static final int MAX_RENEWAL_COUNT = 2;
    
    @Transactional
    public BorrowRecordResponse borrowBook(BorrowBookRequest request) {
        log.info("Processing borrow request for user {} and book {}", request.getUserId(), request.getIsbn());
        
        // Note: This method is deprecated in favor of reservation system
        // Students should use reserve book, and librarians should use createLoan
        log.warn("Direct borrowing used. Consider using reservation system instead.");
        
        // Validate user
        UserValidationResponse userValidation = userValidationService.validateUser(request.getUserId());
        if (!userValidation.isSuccess()) {
            throw new IllegalArgumentException("Invalid user: " + userValidation.getMessage());
        }
        
        // Check if user is a student
        if (!userValidationService.isStudent(request.getUserId())) {
            throw new IllegalArgumentException("Only students can borrow books");
        }
        
        // Check if user has reached borrow limit
        long activeBorrows = borrowRecordRepository.countByUserIdAndStatus(
                request.getUserId(), BorrowRecord.BorrowStatus.ACTIVE);
        if (activeBorrows >= MAX_BORROW_LIMIT) {
            throw new IllegalStateException("Maximum borrow limit reached. You can borrow up to " + MAX_BORROW_LIMIT + " books.");
        }
        
        // Check if user already has this book borrowed
        boolean alreadyBorrowed = borrowRecordRepository.findByUserIdAndBookIsbnAndStatus(
                request.getUserId(), request.getIsbn(), BorrowRecord.BorrowStatus.ACTIVE).isPresent();
        if (alreadyBorrowed) {
            throw new IllegalStateException("You have already borrowed this book");
        }
        
        // Check if book has active reservations (block if reserved by someone else)
        if (reservationService.hasActiveReservations(request.getIsbn())) {
            Long nextUserInQueue = reservationService.getNextUserInQueue(request.getIsbn());
            if (nextUserInQueue != null && !nextUserInQueue.equals(request.getUserId())) {
                throw new IllegalStateException("This book is reserved by another student");
            }
        }
        
        // Check if book exists and is available
        if (!bookService.isBookAvailable(request.getIsbn())) {
            throw new IllegalArgumentException("Book is not available for borrowing");
        }
        
        // Create borrow record
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(LOAN_PERIOD_DAYS);
        
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .userId(request.getUserId())
                .bookIsbn(request.getIsbn())
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowRecord.BorrowStatus.ACTIVE)
                .isOverdue(false)
                .fineAmount(0.0)
                .notes("Borrowed via system")
                .build();
        
        BorrowRecord savedBorrowRecord = borrowRecordRepository.save(borrowRecord);
        
        // Decrease available copies in book service
        bookService.decreaseAvailableCopies(request.getIsbn());
        
        // Fulfill reservation if user had one
        reservationService.fulfillReservation(request.getUserId(), request.getIsbn());
        
        log.info("Successfully created borrow record for user {} and book {}", 
                request.getUserId(), request.getIsbn());
        
        return mapToBorrowRecordResponse(savedBorrowRecord);
    }
    
    // NEW METHOD: Create Loan (Librarian checkout)
    @Transactional
    public BorrowRecordResponse createLoan(CreateLoanRequest request) {
        log.info("Processing loan creation for user {} and book {} by librarian {}", 
                request.getUserId(), request.getIsbn(), request.getLibrarianId());
        
        // Validate librarian
        UserValidationResponse librarianValidation = userValidationService.validateUser(request.getLibrarianId());
        if (!librarianValidation.isSuccess()) {
            throw new IllegalArgumentException("Invalid librarian: " + librarianValidation.getMessage());
        }
        
        // Check if librarian has appropriate role
        String librarianRole = librarianValidation.getData().getRole();
        if (!"LIBRARIAN".equals(librarianRole) && !"ADMIN".equals(librarianRole)) {
            throw new IllegalArgumentException("Only librarians and admins can create loans");
        }
        
        // Validate student
        UserValidationResponse userValidation = userValidationService.validateUser(request.getUserId());
        if (!userValidation.isSuccess()) {
            throw new IllegalArgumentException("Invalid user: " + userValidation.getMessage());
        }
        
        // Check if user is a student
        if (!userValidationService.isStudent(request.getUserId())) {
            throw new IllegalArgumentException("Loans can only be created for students");
        }
        
        // Check if user account is active
        if (!"ACTIVE".equals(userValidation.getData().getStatus())) {
            throw new IllegalArgumentException("User account is not active");
        }
        
        // Check if user has reached borrow limit
        long activeBorrows = borrowRecordRepository.countByUserIdAndStatus(
                request.getUserId(), BorrowRecord.BorrowStatus.ACTIVE);
        if (activeBorrows >= MAX_BORROW_LIMIT) {
            throw new IllegalStateException("Student has reached maximum borrow limit of " + MAX_BORROW_LIMIT + " books.");
        }
        
        // Check if user already has this book borrowed
        boolean alreadyBorrowed = borrowRecordRepository.findByUserIdAndBookIsbnAndStatus(
                request.getUserId(), request.getIsbn(), BorrowRecord.BorrowStatus.ACTIVE).isPresent();
        if (alreadyBorrowed) {
            throw new IllegalStateException("Student has already borrowed this book");
        }
        
        // Check if book exists and is available
        if (!bookService.isBookAvailable(request.getIsbn())) {
            throw new IllegalArgumentException("Book is not available for borrowing");
        }
        
        // Check if book is reserved by another student
        if (reservationService.hasActiveReservations(request.getIsbn())) {
            Long nextUserInQueue = reservationService.getNextUserInQueue(request.getIsbn());
            if (nextUserInQueue != null && !nextUserInQueue.equals(request.getUserId())) {
                throw new IllegalStateException("This book is reserved by another student. Checkout blocked until reservation is fulfilled or expired.");
            }
        }
        
        // Create borrow record
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(LOAN_PERIOD_DAYS);
        
        String notes = "Checked out by librarian " + librarianValidation.getData().getUsername();
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            notes += "; " + request.getNotes();
        }
        
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .userId(request.getUserId())
                .bookIsbn(request.getIsbn())
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowRecord.BorrowStatus.ACTIVE)
                .isOverdue(false)
                .fineAmount(0.0)
                .notes(notes)
                .build();
        
        BorrowRecord savedBorrowRecord = borrowRecordRepository.save(borrowRecord);
        
        // Decrease available copies in book service
        bookService.decreaseAvailableCopies(request.getIsbn());
        
        // Fulfill reservation if user had one
        reservationService.fulfillReservation(request.getUserId(), request.getIsbn());
        
        log.info("Successfully created loan for user {} and book {} by librarian {}", 
                request.getUserId(), request.getIsbn(), request.getLibrarianId());
        
        return mapToBorrowRecordResponse(savedBorrowRecord);
    }
    
    @Transactional
    public BorrowRecordResponse returnBook(ReturnBookRequest request) {
        log.info("Processing return request for user {} and book {}", request.getUserId(), request.getIsbn());
        
        // Find active borrow record
        BorrowRecord borrowRecord = borrowRecordRepository.findByUserIdAndBookIsbnAndStatus(
                request.getUserId(), request.getIsbn(), BorrowRecord.BorrowStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active borrow record found for this user and book"));
        
        // Calculate fine if overdue
        LocalDate today = LocalDate.now();
        if (today.isAfter(borrowRecord.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(borrowRecord.getDueDate(), today);
            double fine = overdueDays * FINE_PER_DAY;
            borrowRecord.setFineAmount(fine);
            borrowRecord.setIsOverdue(true);
            
            // Create fine record in payment service
            paymentServiceClient.createOverdueFine(
                borrowRecord.getUserId(),
                borrowRecord.getId(),
                borrowRecord.getBookIsbn(),
                borrowRecord.getDueDate()
            );
            
            log.info("Book returned late. Fine calculated: ${} for {} days. Fine record created in payment service.", 
                    fine, overdueDays);
        }
        
        // Update borrow record
        borrowRecord.setReturnDate(today);
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            borrowRecord.setNotes(borrowRecord.getNotes() + "; " + request.getNotes());
        }
        
        BorrowRecord savedBorrowRecord = borrowRecordRepository.save(borrowRecord);
        
        // Increase available copies in book service
        bookService.increaseAvailableCopies(request.getIsbn());
        
        log.info("Successfully processed return for user {} and book {}", 
                request.getUserId(), request.getIsbn());
        
        return mapToBorrowRecordResponse(savedBorrowRecord);
    }
    
    @Transactional
    public BorrowRecordResponse renewLoan(RenewLoanRequest request) {
        log.info("Processing renewal request for user {} and borrow record {}", 
                request.getUserId(), request.getBorrowRecordId());
        
        // Find the borrow record
        BorrowRecord borrowRecord = borrowRecordRepository.findById(request.getBorrowRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found"));
        
        // Validate that the borrow record belongs to the user
        if (!borrowRecord.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Borrow record does not belong to the specified user");
        }
        
        // Check if the book is still active
        if (borrowRecord.getStatus() != BorrowRecord.BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Cannot renew a loan that is not active");
        }
        
        // Check if book has reservations
        if (reservationService.hasActiveReservations(borrowRecord.getBookIsbn())) {
            throw new IllegalStateException("Cannot renew. This book is reserved by another student");
        }
        
        // Check renewal count (if we track it in the future)
        // For now, allow renewal if not overdue
        if (borrowRecord.getIsOverdue()) {
            throw new IllegalStateException("Cannot renew an overdue book. Please return and pay fine first");
        }
        
        // Extend due date
        LocalDate newDueDate = borrowRecord.getDueDate().plusDays(LOAN_PERIOD_DAYS);
        borrowRecord.setDueDate(newDueDate);
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.RENEWED);
        
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            borrowRecord.setNotes(borrowRecord.getNotes() + "; Renewed: " + request.getNotes());
        } else {
            borrowRecord.setNotes(borrowRecord.getNotes() + "; Renewed on " + LocalDate.now());
        }
        
        BorrowRecord savedBorrowRecord = borrowRecordRepository.save(borrowRecord);
        
        log.info("Successfully renewed loan for user {} and book {}", 
                request.getUserId(), borrowRecord.getBookIsbn());
        
        return mapToBorrowRecordResponse(savedBorrowRecord);
    }
    
    public Page<BorrowRecordResponse> getUserBorrowHistory(Long userId, Pageable pageable) {
        // Validate user
        UserValidationResponse userValidation = userValidationService.validateUser(userId);
        if (!userValidation.isSuccess()) {
            throw new IllegalArgumentException("Invalid user: " + userValidation.getMessage());
        }
        
        Page<BorrowRecord> borrowRecords = borrowRecordRepository.findByUserIdOrderByBorrowDateDesc(userId, pageable);
        return borrowRecords.map(this::mapToBorrowRecordResponse);
    }
    
    public Page<BorrowRecordResponse> getBookBorrowHistory(String isbn, Pageable pageable) {
        // Check if book exists
        if (bookService.getBookByIsbn(isbn) == null) {
            throw new IllegalArgumentException("Book not found with ISBN: " + isbn);
        }
        
        Page<BorrowRecord> borrowRecords = borrowRecordRepository.findByBookIsbnOrderByBorrowDateDesc(isbn, pageable);
        return borrowRecords.map(this::mapToBorrowRecordResponse);
    }
    
    public List<BorrowRecordResponse> getUserActiveBorrows(Long userId) {
        List<BorrowRecord> activeBorrows = borrowRecordRepository.findByUserIdAndStatus(
                userId, BorrowRecord.BorrowStatus.ACTIVE);
        return activeBorrows.stream()
                .map(this::mapToBorrowRecordResponse)
                .collect(Collectors.toList());
    }
    
    public List<BorrowRecordResponse> getOverdueBooks() {
        List<BorrowRecord> overdueBooks = borrowRecordRepository.findOverdueBooks(LocalDate.now());
        return overdueBooks.stream()
                .map(this::mapToBorrowRecordResponse)
                .collect(Collectors.toList());
    }
    
    public List<BorrowRecordResponse> getBooksDueSoon(int days) {
        LocalDate currentDate = LocalDate.now();
        LocalDate dueDate = currentDate.plusDays(days);
        
        List<BorrowRecord> booksDueSoon = borrowRecordRepository.findBooksDueSoon(currentDate, dueDate);
        return booksDueSoon.stream()
                .map(this::mapToBorrowRecordResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public BorrowRecordResponse markBookAsLost(MarkBookAsLostRequest request) {
        log.info("Processing mark as lost request for borrow record {}", request.getBorrowRecordId());
        
        // Find the borrow record
        BorrowRecord borrowRecord = borrowRecordRepository.findById(request.getBorrowRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found"));
        
        // Validate that the borrow record is active or overdue
        if (borrowRecord.getStatus() != BorrowRecord.BorrowStatus.ACTIVE && 
            borrowRecord.getStatus() != BorrowRecord.BorrowStatus.OVERDUE &&
            borrowRecord.getStatus() != BorrowRecord.BorrowStatus.RENEWED) {
            throw new IllegalStateException("Can only mark active or overdue books as lost");
        }
        
        // Update borrow record status to LOST
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.LOST);
        borrowRecord.setReturnDate(LocalDate.now()); // Mark as "returned" but lost
        
        // Add notes about who marked it as lost
        String lostNotes = "Marked as lost";
        if (request.getMarkedByUserId() != null) {
            UserValidationResponse userValidation = userValidationService.validateUser(request.getMarkedByUserId());
            if (userValidation.isSuccess()) {
                lostNotes += " by " + userValidation.getData().getUsername();
            }
        }
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            lostNotes += "; " + request.getNotes();
        }
        borrowRecord.setNotes(borrowRecord.getNotes() + "; " + lostNotes);
        
        BorrowRecord savedBorrowRecord = borrowRecordRepository.save(borrowRecord);
        
        // Create lost book fine in payment service
        try {
            paymentServiceClient.createLostBookFine(
                borrowRecord.getUserId(),
                borrowRecord.getId(),
                borrowRecord.getBookIsbn(),
                request.getReplacementCost() != null ? request.getReplacementCost() : 50.0 // Default $50 replacement cost
            );
            log.info("Created lost book fine record in payment service for loan ID: {}", borrowRecord.getId());
        } catch (Exception e) {
            log.error("Failed to create lost book fine record for loan ID: {}", borrowRecord.getId(), e);
        }
        
        // Increase available copies in book service (book is effectively returned but lost)
        bookService.increaseAvailableCopies(borrowRecord.getBookIsbn());
        
        log.info("Successfully marked book as lost for borrow record ID: {}", borrowRecord.getId());
        
        return mapToBorrowRecordResponse(savedBorrowRecord);
    }
    
    @Transactional
    public void updateOverdueStatus() {
        List<BorrowRecord> overdueBooks = borrowRecordRepository.findOverdueBooks(LocalDate.now());
        
        for (BorrowRecord borrowRecord : overdueBooks) {
            if (!borrowRecord.getIsOverdue()) {
                // Calculate fine
                LocalDate today = LocalDate.now();
                long overdueDays = ChronoUnit.DAYS.between(borrowRecord.getDueDate(), today);
                double fine = overdueDays * FINE_PER_DAY;
                
                borrowRecord.setIsOverdue(true);
                borrowRecord.setFineAmount(fine);
                borrowRecord.setStatus(BorrowRecord.BorrowStatus.OVERDUE);
                
                borrowRecordRepository.save(borrowRecord);
                
                // Create fine record in payment service for overdue books
                try {
                    paymentServiceClient.createOverdueFine(
                        borrowRecord.getUserId(),
                        borrowRecord.getId(),
                        borrowRecord.getBookIsbn(),
                        borrowRecord.getDueDate()
                    );
                    log.info("Created fine record in payment service for overdue loan ID: {}, Amount: ${}", 
                            borrowRecord.getId(), fine);
                } catch (Exception e) {
                    log.error("Failed to create fine record for overdue loan ID: {}", borrowRecord.getId(), e);
                }
                
                log.info("Updated overdue status for borrow record ID: {}, Fine: ${}", 
                        borrowRecord.getId(), fine);
            }
        }
    }
    
    public Page<BorrowRecordResponse> getAllBorrowRecords(Pageable pageable) {
        Page<BorrowRecord> borrowRecords = borrowRecordRepository.findAll(pageable);
        return borrowRecords.map(this::mapToBorrowRecordResponse);
    }
    
    private BorrowRecordResponse mapToBorrowRecordResponse(BorrowRecord borrowRecord) {
        BorrowRecordResponse response = BorrowRecordResponse.builder()
                .id(borrowRecord.getId())
                .userId(borrowRecord.getUserId())
                .bookIsbn(borrowRecord.getBookIsbn())
                .borrowDate(borrowRecord.getBorrowDate())
                .dueDate(borrowRecord.getDueDate())
                .returnDate(borrowRecord.getReturnDate())
                .status(borrowRecord.getStatus().name())
                .isOverdue(borrowRecord.getIsOverdue())
                .fineAmount(borrowRecord.getFineAmount())
                .notes(borrowRecord.getNotes())
                .createdAt(borrowRecord.getCreatedAt())
                .updatedAt(borrowRecord.getUpdatedAt())
                .build();
        
        // Fetch book details
        BookResponse book = bookService.getBookByIsbn(borrowRecord.getBookIsbn());
        response.setBook(book);
        
        return response;
    }
}