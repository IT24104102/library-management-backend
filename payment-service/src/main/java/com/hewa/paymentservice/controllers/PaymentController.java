package com.hewa.paymentservice.controllers;

import com.hewa.paymentservice.dto.*;
import com.hewa.paymentservice.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    // Create a fine (called from borrow-service)
    @PostMapping("/fines")
    public ResponseEntity<ApiResponse<FineResponse>> createFine(@Valid @RequestBody CreateFineRequest request) {
        try {
            FineResponse fine = paymentService.createFine(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Fine created successfully", fine));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating fine", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create fine: " + e.getMessage()));
        }
    }
    
    // Process payment for a fine
    @PostMapping("/fines/pay")
    public ResponseEntity<ApiResponse<FineResponse>> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        try {
            FineResponse fine = paymentService.processPayment(request);
            return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", fine));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process payment: " + e.getMessage()));
        }
    }
    
    // Waive a fine (librarian only)
    @PostMapping("/fines/waive")
    public ResponseEntity<ApiResponse<FineResponse>> waiveFine(@Valid @RequestBody WaiveFineRequest request) {
        try {
            FineResponse fine = paymentService.waiveFine(request);
            return ResponseEntity.ok(ApiResponse.success("Fine waived successfully", fine));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error waiving fine", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to waive fine: " + e.getMessage()));
        }
    }
    
    // Get user's fines with pagination
    @GetMapping("/fines/user/{userId}")
    public ResponseEntity<ApiResponse<Page<FineResponse>>> getUserFines(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<FineResponse> fines = paymentService.getUserFines(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success(fines));
        } catch (Exception e) {
            log.error("Error fetching user fines", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch user fines: " + e.getMessage()));
        }
    }
    
    // Get user's pending fines
    @GetMapping("/fines/user/{userId}/pending")
    public ResponseEntity<ApiResponse<List<FineResponse>>> getUserPendingFines(@PathVariable Long userId) {
        try {
            List<FineResponse> pendingFines = paymentService.getUserPendingFines(userId);
            return ResponseEntity.ok(ApiResponse.success(pendingFines));
        } catch (Exception e) {
            log.error("Error fetching user pending fines", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch pending fines: " + e.getMessage()));
        }
    }
    
    // Get user's total outstanding fines
    @GetMapping("/fines/user/{userId}/outstanding")
    public ResponseEntity<ApiResponse<BigDecimal>> getUserOutstandingFines(@PathVariable Long userId) {
        try {
            BigDecimal outstandingAmount = paymentService.getUserOutstandingFines(userId);
            return ResponseEntity.ok(ApiResponse.success(outstandingAmount));
        } catch (Exception e) {
            log.error("Error fetching user outstanding fines", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch outstanding fines: " + e.getMessage()));
        }
    }
    
    // Check if user has pending fines
    @GetMapping("/user/{userId}/has-pending")
    public ResponseEntity<ApiResponse<Boolean>> hasUserPendingFines(@PathVariable Long userId) {
        try {
            boolean hasPendingFines = paymentService.hasUserPendingFines(userId);
            return ResponseEntity.ok(ApiResponse.success(hasPendingFines));
        } catch (Exception e) {
            log.error("Error checking user pending fines", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check pending fines: " + e.getMessage()));
        }
    }
    
    // Get all fines (admin/librarian)
    @GetMapping("/fines")
    public ResponseEntity<ApiResponse<Page<FineResponse>>> getAllFines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<FineResponse> fines = paymentService.getAllFines(pageable);
            return ResponseEntity.ok(ApiResponse.success(fines));
        } catch (Exception e) {
            log.error("Error fetching all fines", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch fines: " + e.getMessage()));
        }
    }
    
    // Get fines by status
    @GetMapping("/fines/status/{status}")
    public ResponseEntity<ApiResponse<Page<FineResponse>>> getFinesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            Page<FineResponse> fines = paymentService.getFinesByStatus(status, pageable);
            return ResponseEntity.ok(ApiResponse.success(fines));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status: " + status));
        } catch (Exception e) {
            log.error("Error fetching fines by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch fines: " + e.getMessage()));
        }
    }
    
    // Get fine by ID
    @GetMapping("/fines/{fineId}")
    public ResponseEntity<ApiResponse<FineResponse>> getFineById(@PathVariable Long fineId) {
        try {
            FineResponse fine = paymentService.getFineById(fineId);
            return ResponseEntity.ok(ApiResponse.success(fine));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching fine by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch fine: " + e.getMessage()));
        }
    }
    
    // Get payment summaries (for loans page statistics)
    @GetMapping("/payments/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getPaymentSummary() {
        try {
            PaymentSummaryResponse summary = paymentService.getPaymentSummary();
            return ResponseEntity.ok(ApiResponse.success("Payment summary retrieved successfully", summary));
        } catch (Exception e) {
            log.error("Error fetching payment summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch payment summary: " + e.getMessage()));
        }
    }
    
    // Get payment summaries by date range
    @GetMapping("/payments/summary/range")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getPaymentSummaryByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            PaymentSummaryResponse summary = paymentService.getPaymentSummaryByDateRange(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Payment summary retrieved successfully", summary));
        } catch (Exception e) {
            log.error("Error fetching payment summary by date range", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch payment summary: " + e.getMessage()));
        }
    }
}