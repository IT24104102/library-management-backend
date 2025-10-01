package com.disanayake.borrowservice.services;

import com.disanayake.borrowservice.dto.*;
import com.disanayake.borrowservice.entities.Reservation;
import com.disanayake.borrowservice.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final UserValidationService userValidationService;
    private final BookService bookService;
    
    private static final int MAX_RESERVATIONS_PER_USER = 5;
    private static final int RESERVATION_EXPIRY_DAYS = 7;
    
    // Student reserves a book
    public ReservationResponse reserveBook(ReserveBookRequest request) {
        log.info("Processing reservation request for user {} and book {}", request.getUserId(), request.getIsbn());
        
        // Validate user
        UserValidationResponse userValidation = userValidationService.validateUser(request.getUserId());
        if (!userValidation.isSuccess()) {
            throw new IllegalArgumentException("User validation failed: " + userValidation.getMessage());
        }
        
        // Only students can make reservations
        if (!"STUDENT".equals(userValidation.getData().getRole())) {
            throw new IllegalArgumentException("Only students can make reservations");
        }
        
        // Check if user account is active
        if (!"ACTIVE".equals(userValidation.getData().getStatus())) {
            throw new IllegalArgumentException("User account is not active");
        }
        
        // Validate book
        BookResponse book = bookService.getBookByIsbn(request.getIsbn());
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ISBN: " + request.getIsbn());
        }
        
        // Check if user already has an active reservation for this book
        if (reservationRepository.findByUserIdAndBookIsbnAndStatus(
                request.getUserId(), request.getIsbn(), Reservation.ReservationStatus.ACTIVE).isPresent()) {
            throw new IllegalStateException("User already has an active reservation for this book");
        }
        
        // Check reservation limit
        long activeReservations = reservationRepository.countByUserIdAndStatus(
                request.getUserId(), Reservation.ReservationStatus.ACTIVE);
        if (activeReservations >= MAX_RESERVATIONS_PER_USER) {
            throw new IllegalStateException("User has reached maximum reservation limit of " + MAX_RESERVATIONS_PER_USER);
        }
        
        // Create reservation
        Reservation reservation = Reservation.builder()
                .userId(request.getUserId())
                .bookIsbn(request.getIsbn())
                .reservationDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(RESERVATION_EXPIRY_DAYS))
                .status(Reservation.ReservationStatus.ACTIVE)
                .notes(request.getNotes())
                .build();
        
        reservation = reservationRepository.save(reservation);
        
        log.info("Reservation created successfully with ID: {}", reservation.getId());
        return mapToReservationResponse(reservation, book);
    }
    
    // Get user's reservations
    public Page<ReservationResponse> getUserReservations(Long userId, Pageable pageable) {
        // Validate user
        UserValidationResponse userValidation = userValidationService.validateUser(userId);
        if (!userValidation.isSuccess()) {
            throw new IllegalArgumentException("User validation failed: " + userValidation.getMessage());
        }
        
        Page<Reservation> reservations = reservationRepository.findByUserIdOrderByReservationDateDesc(userId, pageable);
        
        return reservations.map(reservation -> {
            BookResponse book = bookService.getBookByIsbn(reservation.getBookIsbn());
            return mapToReservationResponse(reservation, book);
        });
    }
    
    // Get user's active reservations
    public List<ReservationResponse> getUserActiveReservations(Long userId) {
        // Validate user
        UserValidationResponse userValidation = userValidationService.validateUser(userId);
        if (!userValidation.isSuccess()) {
            throw new IllegalArgumentException("User validation failed: " + userValidation.getMessage());
        }
        
        List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(userId, Reservation.ReservationStatus.ACTIVE);
        
        return reservations.stream()
                .map(reservation -> {
                    BookResponse book = bookService.getBookByIsbn(reservation.getBookIsbn());
                    return mapToReservationResponse(reservation, book);
                })
                .collect(Collectors.toList());
    }
    
    // Get book's reservations (for librarians)
    public List<ReservationResponse> getBookReservations(String isbn) {
        BookResponse book = bookService.getBookByIsbn(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ISBN: " + isbn);
        }
        
        List<Reservation> reservations = reservationRepository.findActiveReservationsForBook(isbn);
        
        return reservations.stream()
                .map(reservation -> mapToReservationResponse(reservation, book))
                .collect(Collectors.toList());
    }
    
    // Cancel reservation
    public ReservationResponse cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        
        // Check if the reservation belongs to the user
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Reservation does not belong to the user");
        }
        
        // Check if reservation can be cancelled
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Only active reservations can be cancelled");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation = reservationRepository.save(reservation);
        
        BookResponse book = bookService.getBookByIsbn(reservation.getBookIsbn());
        log.info("Reservation {} cancelled successfully", reservationId);
        
        return mapToReservationResponse(reservation, book);
    }
    
    // Fulfill reservation (convert to loan) - called by loan creation process
    public void fulfillReservation(Long userId, String isbn) {
        List<Reservation> activeReservations = reservationRepository.findByUserIdAndStatus(userId, Reservation.ReservationStatus.ACTIVE);
        
        for (Reservation reservation : activeReservations) {
            if (reservation.getBookIsbn().equals(isbn)) {
                reservation.setStatus(Reservation.ReservationStatus.FULFILLED);
                reservationRepository.save(reservation);
                log.info("Reservation {} fulfilled for user {} and book {}", reservation.getId(), userId, isbn);
                break;
            }
        }
    }
    
    // Update expired reservations
    public void updateExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDate.now());
        
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            log.info("Reservation {} expired for user {} and book {}", 
                    reservation.getId(), reservation.getUserId(), reservation.getBookIsbn());
        }
        
        log.info("Updated {} expired reservations", expiredReservations.size());
    }
    
    // Get next user in reservation queue for a book
    public Long getNextUserInQueue(String isbn) {
        List<Reservation> activeReservations = reservationRepository.findActiveReservationsForBook(isbn);
        
        if (!activeReservations.isEmpty()) {
            return activeReservations.get(0).getUserId(); // First in queue
        }
        
        return null;
    }
    
    // Check if book has active reservations
    public boolean hasActiveReservations(String isbn) {
        return reservationRepository.countByBookIsbnAndStatus(isbn, Reservation.ReservationStatus.ACTIVE) > 0;
    }
    
    // Get all reservations (for admin/librarian)
    public Page<ReservationResponse> getAllReservations(Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findAll(pageable);
        
        return reservations.map(reservation -> {
            BookResponse book = bookService.getBookByIsbn(reservation.getBookIsbn());
            return mapToReservationResponse(reservation, book);
        });
    }
    
    private ReservationResponse mapToReservationResponse(Reservation reservation, BookResponse book) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .bookIsbn(reservation.getBookIsbn())
                .book(book)
                .reservationDate(reservation.getReservationDate())
                .expiryDate(reservation.getExpiryDate())
                .status(reservation.getStatus().name())
                .notes(reservation.getNotes())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}