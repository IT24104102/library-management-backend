package com.amarakeerthi.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateStudentRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @Min(value = 1, message = "Year of study must be at least 1")
    @Max(value = 8, message = "Year of study cannot exceed 8")
    private int yearOfStudy;
    
    private String contactNumber;
}
