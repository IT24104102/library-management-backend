package com.kuruneru.latereminders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private Long id;
    private Long userId;
    private String bookIsbn;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
    private Integer renewalCount;
    
    // Book information (if available from the response)
    private BookDto book;
    
    // User information (if available from the response)
    private UserDto user;
    
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status) || "RENEWED".equalsIgnoreCase(status);
    }
    
    public boolean isOverdue() {
        return isActive() && dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }
    
    public boolean isDueTomorrow() {
        if (!isActive() || dueDate == null) return false;
        
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime dueDateStart = dueDate.toLocalDate().atStartOfDay();
        LocalDateTime dueDateEnd = dueDate.toLocalDate().atTime(23, 59, 59);
        
        return !tomorrow.isBefore(dueDateStart) && !tomorrow.isAfter(dueDateEnd);
    }
}