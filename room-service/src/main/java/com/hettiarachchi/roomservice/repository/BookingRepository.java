package com.hettiarachchi.roomservice.repository;

import com.hettiarachchi.roomservice.entity.Booking;
import com.hettiarachchi.roomservice.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserIdOrderByBookingDateDescStartTimeDesc(Long userId);
    
    Page<Booking> findByUserIdOrderByBookingDateDescStartTimeDesc(Long userId, Pageable pageable);
    
    List<Booking> findByStatus(BookingStatus status);
    
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.bookingDate = :date " +
           "AND b.status IN ('PENDING', 'APPROVED') " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId, 
                                        @Param("date") LocalDate date,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId " +
           "AND b.bookingDate = :date AND b.status IN ('PENDING', 'APPROVED')")
    Long countDailyBookingsByUser(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId " +
           "AND b.bookingDate BETWEEN :startDate AND :endDate " +
           "AND b.status IN ('PENDING', 'APPROVED')")
    Long countWeeklyBookingsByUser(@Param("userId") Long userId, 
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.bookingDate = :date " +
           "AND b.status IN ('PENDING', 'APPROVED') ORDER BY b.startTime")
    List<Booking> findBookingsByRoomAndDate(@Param("roomId") Long roomId, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findBookingsForReport(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status " +
           "AND b.createdAt BETWEEN :startDate AND :endDate")
    Long countBookingsByStatusAndDateRange(@Param("status") BookingStatus status,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}