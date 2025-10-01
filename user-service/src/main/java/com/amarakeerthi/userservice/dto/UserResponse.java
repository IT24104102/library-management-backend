package com.amarakeerthi.userservice.dto;

import com.amarakeerthi.userservice.constants.UserRole;
import com.amarakeerthi.userservice.constants.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String contactNumber;
    private UserRole role;
    private UserStatus status;
    private boolean mustChangePassword;
    private LocalDateTime createdAt;
    private LocalDateTime lastPasswordChange;
    
    // Role-specific fields
    private String employeeId;  // For Librarian/Admin (adminId for Admin)
    private String branch;      // For Librarian
    private String workShift;   // For Librarian
    
    private String studentId;   // For Student
    private String department;  // For Student/Admin
    private Integer yearOfStudy; // For Student
    private Integer borrowLimit; // For Student
    private Integer borrowedCount; // For Student
    
    private String permissions; // For Admin
}