package com.amarakeerthi.userservice.controllers;

import com.amarakeerthi.userservice.dto.*;
import com.amarakeerthi.userservice.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("User Service is running", "OK"));
    }

    // Create Librarian
    @PostMapping("/librarians")
    public ResponseEntity<ApiResponse<UserResponse>> createLibrarian(
            @Valid @RequestBody CreateLibrarianRequest request) {
        log.info("Creating librarian account for email: {}", request.getEmail());
        
        try {
            UserResponse userResponse = userService.createLibrarian(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Librarian account created successfully. Welcome email sent.", userResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating librarian account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create librarian account"));
        }
    }

    // Create Student
    @PostMapping("/students")
    public ResponseEntity<ApiResponse<UserResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        log.info("Creating student account for email: {}", request.getEmail());
        
        try {
            UserResponse userResponse = userService.createStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student account created successfully. Welcome email sent.", userResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating student account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create student account"));
        }
    }

    // Create Admin (Only for existing admins)
    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) {
        log.info("Creating admin account for email: {}", request.getEmail());
        
        try {
            UserResponse userResponse = userService.createAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin account created successfully.", userResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating admin account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create admin account"));
        }
    }

    // Get All Users with Pagination
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("Fetching all users with pagination");
        
        try {
            Page<UserResponse> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch users"));
        }
    }

    // Get User by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        
        try {
            UserResponse userResponse = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
        } catch (Exception e) {
            log.error("Error fetching user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"));
        }
    }

    // Get User by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        
        try {
            UserResponse userResponse = userService.getUserByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
        } catch (Exception e) {
            log.error("Error fetching user with email: {}", email, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"));
        }
    }

    // Update User
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        
        try {
            UserResponse userResponse = userService.updateUser(id, request);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update user"));
        }
    }

    // Deactivate User
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user with ID: {}", id);
        
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to deactivate user"));
        }
    }

    // Activate User
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        log.info("Activating user with ID: {}", id);
        
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
        } catch (Exception e) {
            log.error("Error activating user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to activate user"));
        }
    }

    // Change Password
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id, 
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for user with ID: {}", id);
        
        try {
            userService.changePassword(id, request);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error changing password for user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to change password"));
        }
    }
}
