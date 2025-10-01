package com.amarakeerthi.userservice.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String email;
    private String contactNumber;
    
    // Librarian-specific fields
    private String employeeId;
    private String branch;
    private String workShift;
    
    // Student-specific fields
    private String studentId;
    private String department;
    private Integer yearOfStudy;
}