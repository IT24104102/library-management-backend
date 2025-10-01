package com.hettiarachchi.roomservice.controller;

import com.hettiarachchi.roomservice.dto.*;
import com.hettiarachchi.roomservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        log.info("Creating booking request for user {} in room {} on {}", 
                request.getUserId(), request.getRoomId(), request.getBookingDate());
        
        try {
            BookingResponse booking = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking request created successfully", booking));
        } catch (Exception e) {
            log.error("Error creating booking", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        log.info("Fetching booking with ID: {}", id);
        
        try {
            BookingResponse booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", booking));
        } catch (Exception e) {
            log.error("Error fetching booking with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Booking not found"));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByUser(@PathVariable Long userId) {
        log.info("Fetching bookings for user: {}", userId);
        
        try {
            List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", bookings));
        } catch (Exception e) {
            log.error("Error fetching bookings for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch user bookings"));
        }
    }
    
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookingsByUserPaginated(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "bookingDate") Pageable pageable) {
        log.info("Fetching paginated bookings for user: {}", userId);
        
        try {
            Page<BookingResponse> bookings = bookingService.getBookingsByUserId(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", bookings));
        } catch (Exception e) {
            log.error("Error fetching paginated bookings for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch user bookings"));
        }
    }
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getPendingBookings(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("Fetching pending bookings");
        
        try {
            Page<BookingResponse> bookings = bookingService.getPendingBookings(pageable);
            return ResponseEntity.ok(ApiResponse.success("Pending bookings retrieved successfully", bookings));
        } catch (Exception e) {
            log.error("Error fetching pending bookings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch pending bookings"));
        }
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(
            @PathVariable Long id,
            @RequestParam Long librarianId) {
        log.info("Approving booking {} by librarian {}", id, librarianId);
        
        try {
            BookingResponse booking = bookingService.approveBooking(id, librarianId);
            return ResponseEntity.ok(ApiResponse.success("Booking approved successfully", booking));
        } catch (Exception e) {
            log.error("Error approving booking: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
            @PathVariable Long id,
            @RequestParam Long librarianId,
            @Valid @RequestBody BookingApprovalRequest request) {
        log.info("Rejecting booking {} by librarian {}", id, librarianId);
        
        try {
            BookingResponse booking = bookingService.rejectBooking(id, librarianId, request);
            return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", booking));
        } catch (Exception e) {
            log.error("Error rejecting booking: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @RequestParam Long userId) {
        log.info("Cancelling booking {} by user {}", id, userId);
        
        try {
            bookingService.cancelBooking(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully"));
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/alternatives")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getSuggestedAlternatives(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        
        log.info("Finding alternatives for room {} on {} from {} to {}", roomId, date, startTime, endTime);
        
        try {
            List<RoomResponse> alternatives = bookingService.getSuggestedAlternatives(roomId, date, startTime, endTime);
            return ResponseEntity.ok(ApiResponse.success("Alternative rooms retrieved successfully", alternatives));
        } catch (Exception e) {
            log.error("Error finding alternatives", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to find alternative rooms"));
        }
    }
}