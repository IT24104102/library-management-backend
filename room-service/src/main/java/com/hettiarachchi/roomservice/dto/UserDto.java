package com.hettiarachchi.roomservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    
    private Long id;
    
    @JsonProperty("fullName")
    private String name;
    
    private String email;
    
    @JsonProperty("contactNumber")
    private String contactNumber;
    
    private String role;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("mustChangePassword")
    private Boolean mustChangePassword;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("lastPasswordChange")
    private LocalDateTime lastPasswordChange;
    
    // Student-specific fields
    @JsonProperty("studentId")
    private String studentId;
    
    private String department;
    
    @JsonProperty("yearOfStudy")
    private Integer yearOfStudy;
    
    @JsonProperty("borrowLimit")
    private Integer borrowLimit;
    
    @JsonProperty("borrowedCount")
    private Integer borrowedCount;
    
    // Employee-specific fields (can be null for students)
    @JsonProperty("employeeId")
    private String employeeId;
    
    private String branch;
    
    @JsonProperty("workShift")
    private String workShift;
    
    // Convenience methods
    public Boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
    
    public Boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }
    
    public Boolean isLibrarian() {
        return "LIBRARIAN".equalsIgnoreCase(role);
    }
    
    public Boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}