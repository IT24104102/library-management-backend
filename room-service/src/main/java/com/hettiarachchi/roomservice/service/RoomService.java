package com.hettiarachchi.roomservice.service;

import com.hettiarachchi.roomservice.constants.BookingConstants;
import com.hettiarachchi.roomservice.dto.RoomResponse;
import com.hettiarachchi.roomservice.dto.TimeSlot;
import com.hettiarachchi.roomservice.entity.Booking;
import com.hettiarachchi.roomservice.entity.Room;
import com.hettiarachchi.roomservice.exception.ResourceNotFoundException;
import com.hettiarachchi.roomservice.repository.BookingRepository;
import com.hettiarachchi.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    
    // Business hours from constants
    private static final LocalTime BUSINESS_START = LocalTime.of(BookingConstants.BUSINESS_START_HOUR, BookingConstants.BUSINESS_START_MINUTE);
    private static final LocalTime BUSINESS_END = LocalTime.of(BookingConstants.BUSINESS_END_HOUR, BookingConstants.BUSINESS_END_MINUTE);
    
    public List<RoomResponse> getAllAvailableRooms() {
        log.debug("Fetching all available rooms");
        List<Room> rooms = roomRepository.findByIsActiveTrue();
        return rooms.stream()
            .map(this::convertToRoomResponse)
            .collect(Collectors.toList());
    }
    
    public List<RoomResponse> getAvailableRoomsForDate(LocalDate date) {
        log.debug("Fetching available rooms for date: {}", date);
        List<Room> rooms = roomRepository.findByIsActiveTrue();
        return rooms.stream()
            .map(room -> convertToRoomResponseWithAvailability(room, date))
            .collect(Collectors.toList());
    }
    
    public RoomResponse getRoomById(Long roomId) {
        log.debug("Fetching room with ID: {}", roomId);
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        return convertToRoomResponse(room);
    }
    
    public List<RoomResponse> getRoomsByCapacity(Integer minCapacity) {
        log.debug("Fetching rooms with minimum capacity: {}", minCapacity);
        List<Room> rooms = roomRepository.findAvailableRoomsWithMinCapacity(minCapacity);
        return rooms.stream()
            .map(this::convertToRoomResponse)
            .collect(Collectors.toList());
    }
    
    public List<RoomResponse> getRoomsByFacilities(List<String> facilities) {
        log.debug("Fetching rooms with facilities: {}", facilities);
        List<Room> rooms = roomRepository.findRoomsByFacilities(facilities);
        return rooms.stream()
            .map(this::convertToRoomResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public Room createRoom(Room room) {
        log.info("Creating new room: {}", room.getName());
        return roomRepository.save(room);
    }
    
    @Transactional
    public Room updateRoom(Long roomId, Room roomDetails) {
        log.info("Updating room with ID: {}", roomId);
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        
        room.setName(roomDetails.getName());
        room.setCapacity(roomDetails.getCapacity());
        room.setFacilities(roomDetails.getFacilities());
        room.setDescription(roomDetails.getDescription());
        room.setLocation(roomDetails.getLocation());
        room.setIsActive(roomDetails.getIsActive());
        
        return roomRepository.save(room);
    }
    
    @Transactional
    public void deactivateRoom(Long roomId) {
        log.info("Deactivating room with ID: {}", roomId);
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        room.setIsActive(false);
        roomRepository.save(room);
    }
    
    public List<TimeSlot> getAvailableTimeSlots(Long roomId, LocalDate date) {
        log.debug("Getting available time slots for room {} on date {}", roomId, date);
        
        // Get existing bookings for the room on the specified date
        List<Booking> existingBookings = bookingRepository.findBookingsByRoomAndDate(roomId, date);
        
        // Generate all possible time slots
        List<TimeSlot> allSlots = generateTimeSlots();
        
        // Mark slots as unavailable if they conflict with existing bookings
        return allSlots.stream()
            .map(slot -> {
                boolean isAvailable = existingBookings.stream()
                    .noneMatch(booking -> 
                        slot.getStartTime().isBefore(booking.getEndTime()) &&
                        slot.getEndTime().isAfter(booking.getStartTime())
                    );
                slot.setIsAvailable(isAvailable);
                return slot;
            })
            .collect(Collectors.toList());
    }
    
    private List<TimeSlot> generateTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = BUSINESS_START;
        
        while (current.isBefore(BUSINESS_END)) {
            LocalTime slotEnd = current.plusMinutes(BookingConstants.SLOT_DURATION_MINUTES);
            if (slotEnd.isAfter(BUSINESS_END)) {
                break;
            }
            slots.add(new TimeSlot(current, slotEnd, true));
            current = slotEnd;
        }
        
        return slots;
    }
    
    private RoomResponse convertToRoomResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setCapacity(room.getCapacity());
        response.setFacilities(room.getFacilities());
        response.setDescription(room.getDescription());
        response.setLocation(room.getLocation());
        response.setIsActive(room.getIsActive());
        return response;
    }
    
    private RoomResponse convertToRoomResponseWithAvailability(Room room, LocalDate date) {
        RoomResponse response = convertToRoomResponse(room);
        List<TimeSlot> availableSlots = getAvailableTimeSlots(room.getId(), date);
        response.setAvailableSlots(availableSlots);
        return response;
    }
}