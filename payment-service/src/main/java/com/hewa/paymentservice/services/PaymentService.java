package com.hewa.paymentservice.services;

import com.hewa.paymentservice.dto.*;
import com.hewa.paymentservice.entities.Fine;
import com.hewa.paymentservice.entities.Payment;
import com.hewa.paymentservice.repositories.FineRepository;
import com.hewa.paymentservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final FineRepository fineRepository;
    private final PaymentRepository paymentRepository;
    
    @Transactional
    public FineResponse createFine(CreateFineRequest request) {
        log.info("Creating fine for user {} and borrow record {}", request.getUserId(), request.getBorrowRecordId());
        
        // Check if fine already exists for this borrow record
        if (fineRepository.findByBorrowRecordId(request.getBorrowRecordId()).isPresent()) {
            throw new IllegalArgumentException("Fine already exists for this borrow record");
        }
        
        Fine fine = Fine.builder()
                .userId(request.getUserId())
                .borrowRecordId(request.getBorrowRecordId())
                .bookIsbn(request.getBookIsbn())
                .type(Fine.FineType.valueOf(request.getType().toUpperCase()))
                .amount(request.getAmount())
                .status(Fine.FineStatus.PENDING)
                .description(request.getDescription())
                .notes(request.getNotes())
                .build();
        
        Fine savedFine = fineRepository.save(fine);
        log.info("Successfully created fine with ID {} for user {}", savedFine.getId(), request.getUserId());
        
        return mapToFineResponse(savedFine);
    }
    
    @Transactional
    public FineResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for fine {} by user {}", request.getFineId(), request.getUserId());
        
        Fine fine = fineRepository.findById(request.getFineId())
                .orElseThrow(() -> new IllegalArgumentException("Fine not found"));
        
        // Validate that the fine belongs to the user
        if (!fine.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Fine does not belong to the specified user");
        }
        
        // Check if fine is already paid or waived
        if (fine.getStatus() != Fine.FineStatus.PENDING) {
            throw new IllegalArgumentException("Fine is already " + fine.getStatus().toString().toLowerCase());
        }
        
        // Validate payment amount
        if (request.getAmount().compareTo(fine.getAmount()) != 0) {
            throw new IllegalArgumentException("Payment amount does not match fine amount");
        }
        
        // Create payment record
        Payment payment = Payment.builder()
                .fineId(request.getFineId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .method(Payment.PaymentMethod.valueOf(request.getMethod().toUpperCase()))
                .status(Payment.PaymentStatus.COMPLETED)
                .transactionId(request.getTransactionId() != null ? request.getTransactionId() : generateTransactionId())
                .gatewayReference(request.getGatewayReference())
                .processedBy(request.getProcessedBy())
                .notes(request.getNotes())
                .build();
        
        paymentRepository.save(payment);
        
        // Update fine status
        fine.setStatus(Fine.FineStatus.PAID);
        fine.setPaidDate(LocalDateTime.now());
        Fine updatedFine = fineRepository.save(fine);
        
        log.info("Successfully processed payment for fine {} by user {}", request.getFineId(), request.getUserId());
        
        return mapToFineResponse(updatedFine);
    }
    
    @Transactional
    public FineResponse waiveFine(WaiveFineRequest request) {
        log.info("Waiving fine {} by librarian {}", request.getFineId(), request.getWaivedBy());
        
        Fine fine = fineRepository.findById(request.getFineId())
                .orElseThrow(() -> new IllegalArgumentException("Fine not found"));
        
        // Check if fine is already paid or waived
        if (fine.getStatus() != Fine.FineStatus.PENDING) {
            throw new IllegalArgumentException("Fine is already " + fine.getStatus().toString().toLowerCase());
        }
        
        // Update fine status
        fine.setStatus(Fine.FineStatus.WAIVED);
        fine.setWaivedDate(LocalDateTime.now());
        fine.setWaivedBy(request.getWaivedBy());
        fine.setWaiverReason(request.getReason());
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            fine.setNotes(fine.getNotes() != null ? 
                fine.getNotes() + "\nWaiver notes: " + request.getNotes() : 
                "Waiver notes: " + request.getNotes());
        }
        
        Fine updatedFine = fineRepository.save(fine);
        
        log.info("Successfully waived fine {} by librarian {}", request.getFineId(), request.getWaivedBy());
        
        return mapToFineResponse(updatedFine);
    }
    
    public Page<FineResponse> getUserFines(Long userId, Pageable pageable) {
        Page<Fine> fines = fineRepository.findByUserIdOrderByCreatedDateDesc(userId, pageable);
        return fines.map(this::mapToFineResponse);
    }
    
    public List<FineResponse> getUserPendingFines(Long userId) {
        List<Fine> pendingFines = fineRepository.findByUserIdAndStatusOrderByCreatedDateDesc(userId, Fine.FineStatus.PENDING);
        return pendingFines.stream()
                .map(this::mapToFineResponse)
                .collect(Collectors.toList());
    }
    
    public BigDecimal getUserOutstandingFines(Long userId) {
        return fineRepository.getTotalOutstandingFinesByUserId(userId);
    }
    
    public Page<FineResponse> getAllFines(Pageable pageable) {
        Page<Fine> fines = fineRepository.findAll(pageable);
        return fines.map(this::mapToFineResponse);
    }
    
    public Page<FineResponse> getFinesByStatus(String status, Pageable pageable) {
        Fine.FineStatus fineStatus = Fine.FineStatus.valueOf(status.toUpperCase());
        Page<Fine> fines = fineRepository.findByStatusOrderByCreatedDateDesc(fineStatus, pageable);
        return fines.map(this::mapToFineResponse);
    }
    
    public FineResponse getFineById(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found"));
        return mapToFineResponse(fine);
    }
    
    public boolean hasUserPendingFines(Long userId) {
        return fineRepository.existsByUserIdAndStatus(userId, Fine.FineStatus.PENDING);
    }
    
    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    private FineResponse mapToFineResponse(Fine fine) {
        return FineResponse.builder()
                .id(fine.getId())
                .userId(fine.getUserId())
                .borrowRecordId(fine.getBorrowRecordId())
                .bookIsbn(fine.getBookIsbn())
                .type(fine.getType().toString())
                .amount(fine.getAmount())
                .status(fine.getStatus().toString())
                .createdDate(fine.getCreatedDate())
                .dueDate(fine.getDueDate())
                .paidDate(fine.getPaidDate())
                .waivedDate(fine.getWaivedDate())
                .waivedBy(fine.getWaivedBy())
                .waiverReason(fine.getWaiverReason())
                .description(fine.getDescription())
                .notes(fine.getNotes())
                .build();
    }
    
    // Get payment summary for loans page
    public PaymentSummaryResponse getPaymentSummary() {
        log.info("Generating payment summary");
        
        // Get all payments and fines statistics
        List<Payment> allPayments = paymentRepository.findAll();
        List<Fine> allFines = fineRepository.findAll();
        
        return buildPaymentSummary(allPayments, allFines);
    }
    
    // Get payment summary by date range
    public PaymentSummaryResponse getPaymentSummaryByDateRange(String startDate, String endDate) {
        log.info("Generating payment summary for date range: {} to {}", startDate, endDate);
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(start, end);
        List<Fine> fines = fineRepository.findByCreatedDateBetween(start, end);
        
        return buildPaymentSummary(payments, fines);
    }
    
    private PaymentSummaryResponse buildPaymentSummary(List<Payment> payments, List<Fine> fines) {
        // Calculate payment statistics
        BigDecimal totalPaymentsAmount = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long totalPaymentsCount = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .count();
        
        // Calculate fine statistics
        BigDecimal totalPendingFines = fines.stream()
                .filter(f -> f.getStatus() == Fine.FineStatus.PENDING)
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long totalPendingFinesCount = fines.stream()
                .filter(f -> f.getStatus() == Fine.FineStatus.PENDING)
                .count();
        
        // Calculate payment method breakdown
        Map<String, PaymentSummaryResponse.PaymentMethodSummary> paymentMethodBreakdown = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        p -> p.getMethod().toString(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    BigDecimal amount = list.stream()
                                            .map(Payment::getAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    long count = list.size();
                                    BigDecimal percentage = totalPaymentsAmount.compareTo(BigDecimal.ZERO) > 0 
                                            ? amount.divide(totalPaymentsAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                                            : BigDecimal.ZERO;
                                    
                                    return PaymentSummaryResponse.PaymentMethodSummary.builder()
                                            .amount(amount)
                                            .count(count)
                                            .percentage(percentage)
                                            .build();
                                }
                        )
                ));
        
        // Calculate fine type breakdown
        Map<String, PaymentSummaryResponse.FineTypeSummary> fineTypeBreakdown = fines.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getType().toString(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    BigDecimal totalAmount = list.stream()
                                            .map(Fine::getAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    long totalCount = list.size();
                                    
                                    BigDecimal paidAmount = list.stream()
                                            .filter(f -> f.getStatus() == Fine.FineStatus.PAID)
                                            .map(Fine::getAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    long paidCount = list.stream()
                                            .filter(f -> f.getStatus() == Fine.FineStatus.PAID)
                                            .count();
                                    
                                    BigDecimal pendingAmount = list.stream()
                                            .filter(f -> f.getStatus() == Fine.FineStatus.PENDING)
                                            .map(Fine::getAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    long pendingCount = list.stream()
                                            .filter(f -> f.getStatus() == Fine.FineStatus.PENDING)
                                            .count();
                                    
                                    return PaymentSummaryResponse.FineTypeSummary.builder()
                                            .totalAmount(totalAmount)
                                            .totalCount(totalCount)
                                            .paidAmount(paidAmount)
                                            .paidCount(paidCount)
                                            .pendingAmount(pendingAmount)
                                            .pendingCount(pendingCount)
                                            .build();
                                }
                        )
                ));
        
        return PaymentSummaryResponse.builder()
                .totalPaymentsAmount(totalPaymentsAmount)
                .totalPaymentsCount(totalPaymentsCount)
                .totalPendingFines(totalPendingFines)
                .totalPendingFinesCount(totalPendingFinesCount)
                .totalCompletedPayments(totalPaymentsAmount)
                .totalCompletedPaymentsCount(totalPaymentsCount)
                .paymentMethodBreakdown(paymentMethodBreakdown)
                .fineTypeBreakdown(fineTypeBreakdown)
                .paymentTrend(BigDecimal.ZERO) // TODO: Calculate actual trend
                .fineTrend(BigDecimal.ZERO) // TODO: Calculate actual trend
                .build();
    }
}