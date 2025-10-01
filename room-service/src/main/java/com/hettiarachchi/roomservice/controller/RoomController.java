package com.hettiarachchi.roomservice.controller;

import com.hettiarachchi.roomservice.dto.ApiResponse;
import com.hettiarachchi.roomservice.dto.RoomRequest;
import com.hettiarachchi.roomservice.dto.RoomResponse;
import com.hettiarachchi.roomservice.entity.Room;
import com.hettiarachchi.roomservice.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {
    
    private final RoomService roomService;
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Room Service is running", "OK"));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        log.info("Fetching all available rooms");
        
        try {
            List<RoomResponse> rooms = roomService.getAllAvailableRooms();
            return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", rooms));
        } catch (Exception e) {
            log.error("Error fetching rooms", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to fetch rooms"));
        }
    }
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching available rooms for date: {}", date);
        
        try {
            List<RoomResponse> rooms = roomService.getAvailableRoomsForDate(date);
            return ResponseEntity.ok(ApiResponse.success("Available rooms retrieved successfully", rooms));
        } catch (Exception e) {
            log.error("Error fetching available rooms for date: {}", date, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to fetch available rooms"));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        log.info("Fetching room with ID: {}", id);
        
        try {
            RoomResponse room = roomService.getRoomById(id);
            return ResponseEntity.ok(ApiResponse.success("Room retrieved successfully", room));
        } catch (Exception e) {
            log.error("Error fetching room with ID: {}", id, e);
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Room not found"));
        }
    }
    
    @GetMapping("/by-capacity")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByCapacity(
            @RequestParam Integer minCapacity) {
        log.info("Fetching rooms with minimum capacity: {}", minCapacity);
        
        try {
            List<RoomResponse> rooms = roomService.getRoomsByCapacity(minCapacity);
            return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", rooms));
        } catch (Exception e) {
            log.error("Error fetching rooms by capacity: {}", minCapacity, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to fetch rooms by capacity"));
        }
    }
    
    @GetMapping("/by-facilities")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByFacilities(
            @RequestParam List<String> facilities) {
        log.info("Fetching rooms with facilities: {}", facilities);
        
        try {
            List<RoomResponse> rooms = roomService.getRoomsByFacilities(facilities);
            return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", rooms));
        } catch (Exception e) {
            log.error("Error fetching rooms by facilities: {}", facilities, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to fetch rooms by facilities"));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        log.info("Creating new room: {}", roomRequest.getName());
        
        try {
            // Convert RoomRequest to Room entity
            Room room = new Room();
            room.setName(roomRequest.getName());
            room.setCapacity(roomRequest.getCapacity());
            room.setFacilities(roomRequest.getFacilities());
            room.setDescription(roomRequest.getDescription());
            room.setLocation(roomRequest.getLocation());
            room.setIsActive(roomRequest.getIsActive() != null ? roomRequest.getIsActive() : true);
            
            Room createdRoom = roomService.createRoom(room);
            
            // Convert back to response
            RoomResponse response = new RoomResponse();
            response.setId(createdRoom.getId());
            response.setName(createdRoom.getName());
            response.setCapacity(createdRoom.getCapacity());
            response.setFacilities(createdRoom.getFacilities());
            response.setDescription(createdRoom.getDescription());
            response.setLocation(createdRoom.getLocation());
            response.setIsActive(createdRoom.getIsActive());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", response));
        } catch (Exception e) {
            log.error("Error creating room: {}", roomRequest.getName(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to create room: " + e.getMessage()));
        }
    }
}