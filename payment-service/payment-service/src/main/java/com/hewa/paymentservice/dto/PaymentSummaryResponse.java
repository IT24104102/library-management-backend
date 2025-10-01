package com.hewa.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {
    
    // Overall payment statistics
    private BigDecimal totalPaymentsAmount;
    private Long totalPaymentsCount;
    private BigDecimal totalPendingFines;
    private Long totalPendingFinesCount;
    private BigDecimal totalCompletedPayments;
    private Long totalCompletedPaymentsCount;
    
    // Payment method breakdown
    private Map<String, PaymentMethodSummary> paymentMethodBreakdown;
    
    // Fine type breakdown
    private Map<String, FineTypeSummary> fineTypeBreakdown;
    
    // Recent trends (comparing to previous period)
    private BigDecimal paymentTrend; // percentage change
    private BigDecimal fineTrend; // percentage change
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodSummary {
        private BigDecimal amount;
        private Long count;
        private BigDecimal percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FineTypeSummary {
        private BigDecimal totalAmount;
        private Long totalCount;
        private BigDecimal paidAmount;
        private Long paidCount;
        private BigDecimal pendingAmount;
        private Long pendingCount;
    }
}