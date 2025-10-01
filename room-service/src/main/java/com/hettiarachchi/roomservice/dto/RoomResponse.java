package com.hettiarachchi.roomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    
    private Long id;
    private String name;
    private Integer capacity;
    private List<String> facilities;
    private String description;
    private String location;
    private Boolean isActive;
    private List<TimeSlot> availableSlots;
}