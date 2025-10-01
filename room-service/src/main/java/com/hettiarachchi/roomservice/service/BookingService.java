package com.hettiarachchi.roomservice.service;

import com.hettiarachchi.roomservice.constants.BookingConstants;
import com.hettiarachchi.roomservice.dto.*;
import com.hettiarachchi.roomservice.entity.Booking;
import com.hettiarachchi.roomservice.entity.BookingStatus;
import com.hettiarachchi.roomservice.entity.Room;
import com.hettiarachchi.roomservice.exception.BookingConflictException;
import com.hettiarachchi.roomservice.exception.BusinessRuleException;
import com.hettiarachchi.roomservice.exception.InvalidBookingException;
import com.hettiarachchi.roomservice.exception.ResourceNotFoundException;
import com.hettiarachchi.roomservice.repository.BookingRepository;
import com.hettiarachchi.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationService notificationService;
    
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for user {} in room {} on {}", 
                request.getUserId(), request.getRoomId(), request.getBookingDate());
        
        // Validate the booking request
        validateBookingRequest(request);
        
        // Check for conflicts
        checkForConflicts(request);
        
        // Validate business rules
        validateBusinessRules(request);
        
        // Get user details from user service
        UserDto userDto = userServiceClient.getUserById(request.getUserId());
        if (userDto == null) {
            throw new ResourceNotFoundException("User not found with ID: " + request.getUserId());
        }
        
        // Validate user is active
        if (!userDto.isActive()) {
            throw new BusinessRuleException("Cannot create booking for inactive user: " + userDto.getName());
        }
        
        // Get room details
        Room room = roomRepository.findByIdAndIsActiveTrue(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));
        
        // Create booking entity
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUserId(request.getUserId());
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setStatus(BookingStatus.PENDING);
        
        // Cache user details
        booking.setUserName(userDto.getName());
        booking.setUserEmail(userDto.getEmail());
        booking.setUserRole(userDto.getRole());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send notification
        notificationService.sendBookingCreatedNotification(savedBooking);
        
        return convertToBookingResponse(savedBooking);
    }
    
    public BookingResponse getBookingById(Long bookingId) {
        log.debug("Fetching booking with ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        return convertToBookingResponse(booking);
    }
    
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        log.debug("Fetching bookings for user: {}", userId);
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingDateDescStartTimeDesc(userId);
        return bookings.stream()
            .map(this::convertToBookingResponse)
            .collect(Collectors.toList());
    }
    
    public Page<BookingResponse> getBookingsByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching paginated bookings for user: {}", userId);
        Page<Booking> bookings = bookingRepository.findByUserIdOrderByBookingDateDescStartTimeDesc(userId, pageable);
        return bookings.map(this::convertToBookingResponse);
    }
    
    public Page<BookingResponse> getPendingBookings(Pageable pageable) {
        log.debug("Fetching pending bookings");
        Page<Booking> bookings = bookingRepository.findByStatus(BookingStatus.PENDING, pageable);
        return bookings.map(this::convertToBookingResponse);
    }
    
    @Transactional
    public BookingResponse approveBooking(Long bookingId, Long librarianId) {
        log.info("Approving booking {} by librarian {}", bookingId, librarianId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingException("Only pending bookings can be approved");
        }
        
        // Final conflict check before approval
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            booking.getRoom().getId(), booking.getBookingDate(), 
            booking.getStartTime(), booking.getEndTime()
        );
        
        // Remove this booking from conflicts
        conflicts = conflicts.stream()
            .filter(b -> !b.getId().equals(bookingId))
            .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Cannot approve booking due to conflicts with other approved bookings");
        }
        
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(librarianId);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send notification
        notificationService.sendBookingApprovedNotification(savedBooking);
        
        return convertToBookingResponse(savedBooking);
    }
    
    @Transactional
    public BookingResponse rejectBooking(Long bookingId, Long librarianId, BookingApprovalRequest request) {
        log.info("Rejecting booking {} by librarian {}", bookingId, librarianId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingException("Only pending bookings can be rejected");
        }
        
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectedBy(librarianId);
        booking.setRejectionReason(request.getRejectionReason());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send notification
        notificationService.sendBookingRejectedNotification(savedBooking);
        
        return convertToBookingResponse(savedBooking);
    }
    
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        log.info("Cancelling booking {} by user {}", bookingId, userId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        
        if (!booking.getUserId().equals(userId)) {
            throw new InvalidBookingException("Users can only cancel their own bookings");
        }
        
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.APPROVED) {
            throw new InvalidBookingException("Cannot cancel booking with status: " + booking.getStatus());
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Send notification
        notificationService.sendBookingCancelledNotification(booking);
    }
    
    public Page<BookingResponse> getBookingsForReport(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Generating booking report from {} to {}", startDate, endDate);
        Page<Booking> bookings = bookingRepository.findBookingsForReport(startDate, endDate, pageable);
        return bookings.map(this::convertToBookingResponse);
    }
    
    public List<RoomResponse> getSuggestedAlternatives(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        log.debug("Finding alternative rooms for booking on {} from {} to {}", date, startTime, endTime);
        
        // Get the original room to match capacity and facilities
        Room originalRoom = roomRepository.findByIdAndIsActiveTrue(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        
        // Find rooms with similar or better capacity
        List<Room> similarRooms = roomRepository.findAvailableRoomsWithMinCapacity(originalRoom.getCapacity());
        
        return similarRooms.stream()
            .filter(room -> !room.getId().equals(roomId)) // Exclude the originally requested room
            .filter(room -> {
                // Check if this room is available for the requested time slot
                List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    room.getId(), date, startTime, endTime
                );
                return conflicts.isEmpty();
            })
            .map(room -> {
                RoomResponse response = new RoomResponse();
                response.setId(room.getId());
                response.setName(room.getName());
                response.setCapacity(room.getCapacity());
                response.setFacilities(room.getFacilities());
                response.setDescription(room.getDescription());
                response.setLocation(room.getLocation());
                response.setIsActive(room.getIsActive());
                return response;
            })
            .collect(Collectors.toList());
    }
    
    private void validateBookingRequest(BookingRequest request) {
        // Validate date is not in the past
        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new InvalidBookingException(BookingConstants.PAST_DATE_BOOKING);
        }
        
        // Validate date is not too far in the future
        if (request.getBookingDate().isAfter(LocalDate.now().plusDays(BookingConstants.ADVANCE_BOOKING_DAYS))) {
            throw new InvalidBookingException(BookingConstants.ADVANCE_BOOKING_LIMIT);
        }
        
        // Validate time slots
        if (request.getStartTime().isAfter(request.getEndTime()) || 
            request.getStartTime().equals(request.getEndTime())) {
            throw new InvalidBookingException(BookingConstants.INVALID_TIME_SLOT);
        }
        
        // Validate booking duration
        long durationHours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        if (durationHours > BookingConstants.MAX_DURATION_HOURS) {
            throw new InvalidBookingException(BookingConstants.DURATION_LIMIT_EXCEEDED);
        }
        
        // Validate business hours
        if (request.getStartTime().isBefore(LocalTime.of(BookingConstants.BUSINESS_START_HOUR, BookingConstants.BUSINESS_START_MINUTE)) || 
            request.getEndTime().isAfter(LocalTime.of(BookingConstants.BUSINESS_END_HOUR, BookingConstants.BUSINESS_END_MINUTE))) {
            throw new InvalidBookingException(BookingConstants.BUSINESS_HOURS_VIOLATION);
        }
    }
    
    private void checkForConflicts(BookingRequest request) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            request.getRoomId(), request.getBookingDate(), 
            request.getStartTime(), request.getEndTime()
        );
        
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(BookingConstants.BOOKING_CONFLICT);
        }
    }
    
    private void validateBusinessRules(BookingRequest request) {
        // Check daily booking limit
        Long dailyBookings = bookingRepository.countDailyBookingsByUser(
            request.getUserId(), request.getBookingDate()
        );
        if (dailyBookings >= BookingConstants.MAX_DAILY_BOOKINGS) {
            throw new InvalidBookingException(BookingConstants.DAILY_LIMIT_EXCEEDED);
        }
        
        // Check weekly booking limit
        LocalDate weekStart = request.getBookingDate().with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        Long weeklyBookings = bookingRepository.countWeeklyBookingsByUser(
            request.getUserId(), weekStart, weekEnd
        );
        if (weeklyBookings >= BookingConstants.MAX_WEEKLY_BOOKINGS) {
            throw new InvalidBookingException(BookingConstants.WEEKLY_LIMIT_EXCEEDED);
        }
    }
    
    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setRoomId(booking.getRoom().getId());
        response.setRoomName(booking.getRoom().getName());
        response.setUserId(booking.getUserId());
        response.setUserName(booking.getUserName());
        response.setUserEmail(booking.getUserEmail());
        response.setBookingDate(booking.getBookingDate());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setPurpose(booking.getPurpose());
        response.setStatus(booking.getStatus());
        response.setRejectionReason(booking.getRejectionReason());
        response.setApprovedBy(booking.getApprovedBy());
        response.setRejectedBy(booking.getRejectedBy());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        return response;
    }
}