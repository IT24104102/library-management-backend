package com.hettiarachchi.roomservice.controller;

import com.hettiarachchi.roomservice.dto.ApiResponse;
import com.hettiarachchi.roomservice.dto.BookingResponse;
import com.hettiarachchi.roomservice.service.BookingService;
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

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final BookingService bookingService;
    
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookingsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        
        log.info("Generating bookings report from {} to {}", startDate, endDate);
        
        try {
            // Convert LocalDate to LocalDateTime for the start and end of the day
            LocalDateTime startDateTime = startDate.atTime(LocalTime.MIN);
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            Page<BookingResponse> bookings = bookingService.getBookingsForReport(startDateTime, endDateTime, pageable);
            return ResponseEntity.ok(ApiResponse.success("Bookings report generated successfully", bookings));
        } catch (Exception e) {
            log.error("Error generating bookings report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to generate bookings report"));
        }
    }
}