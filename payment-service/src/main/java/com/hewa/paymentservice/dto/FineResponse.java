package com.hewa.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineResponse {
    private Long id;
    private Long userId;
    private Long borrowRecordId;
    private String bookIsbn;
    private String type;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;
    private LocalDateTime waivedDate;
    private Long waivedBy;
    private String waiverReason;
    private String description;
    private String notes;
    
    // Additional fields for frontend display
    private String bookTitle;
    private String userName;
    private String waivedByName;
}