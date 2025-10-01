package com.hettiarachchi.roomservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    
    @NotBlank(message = "Room name is required")
    private String name;
    
    @NotNull(message = "Room capacity is required")
    @Min(value = 1, message = "Room capacity must be at least 1")
    private Integer capacity;
    
    private List<String> facilities;
    
    private String description;
    
    @NotBlank(message = "Room location is required")
    private String location;
    
    private Boolean isActive = true;
}