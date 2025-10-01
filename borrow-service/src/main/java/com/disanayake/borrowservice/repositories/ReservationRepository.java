package com.disanayake.borrowservice.repositories;

import com.disanayake.borrowservice.entities.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Find all reservations for a specific user
    Page<Reservation> findByUserIdOrderByReservationDateDesc(Long userId, Pageable pageable);
    
    // Find active reservations for a specific user
    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    
    // Find active reservations for a specific book
    List<Reservation> findByBookIsbnAndStatusOrderByReservationDateAsc(String bookIsbn, Reservation.ReservationStatus status);
    
    // Find active reservation for a specific user and book
    Optional<Reservation> findByUserIdAndBookIsbnAndStatus(Long userId, String bookIsbn, Reservation.ReservationStatus status);
    
    // Find all active reservations for a book
    @Query("SELECT r FROM Reservation r WHERE r.bookIsbn = :isbn AND r.status = 'ACTIVE' ORDER BY r.reservationDate ASC")
    List<Reservation> findActiveReservationsForBook(@Param("isbn") String isbn);
    
    // Find expired reservations
    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.expiryDate < :currentDate")
    List<Reservation> findExpiredReservations(@Param("currentDate") LocalDate currentDate);
    
    // Count active reservations for a user
    long countByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    
    // Count active reservations for a book
    long countByBookIsbnAndStatus(String bookIsbn, Reservation.ReservationStatus status);
    
    // Find all reservations for a book
    Page<Reservation> findByBookIsbnOrderByReservationDateDesc(String bookIsbn, Pageable pageable);
    
    // Find reservations by status
    Page<Reservation> findByStatusOrderByReservationDateDesc(Reservation.ReservationStatus status, Pageable pageable);
}