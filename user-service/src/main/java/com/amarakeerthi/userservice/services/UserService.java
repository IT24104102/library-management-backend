package com.amarakeerthi.userservice.services;

import com.amarakeerthi.userservice.constants.UserRole;
import com.amarakeerthi.userservice.constants.UserStatus;
import com.amarakeerthi.userservice.dto.*;
import com.amarakeerthi.userservice.exception.UserNotFoundException;
import com.amarakeerthi.userservice.models.Admin;
import com.amarakeerthi.userservice.models.Librarian;
import com.amarakeerthi.userservice.models.Student;
import com.amarakeerthi.userservice.models.User;
import com.amarakeerthi.userservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public UserResponse createLibrarian(CreateLibrarianRequest request) {
        log.info("Creating librarian account for email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        // Generate temporary password
        String temporaryPassword = emailService.generateTemporaryPassword();
        
        // Create librarian
        Librarian librarian = new Librarian();
        librarian.setFullName(request.getFullName());
        librarian.setEmail(request.getEmail());
        librarian.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        librarian.setContactNumber(request.getContactNumber());
        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setStatus(UserStatus.ACTIVE);
        librarian.setEmployeeId(request.getEmployeeId());
        librarian.setBranch(request.getBranch());
        librarian.setWorkShift(request.getWorkShift());
        
        User savedUser = userRepository.save(librarian);
        
        // Send welcome email
        boolean emailSent = emailService.sendWelcomeEmail(
            request.getEmail(), 
            request.getFullName(), 
            temporaryPassword, 
            "Librarian"
        );
        
        if (!emailSent) {
            log.warn("Welcome email failed for user: {}", request.getEmail());
        }
        
        log.info("Librarian account created successfully with ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }
    
    public UserResponse createStudent(CreateStudentRequest request) {
        log.info("Creating student account for email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        // Generate temporary password
        String temporaryPassword = emailService.generateTemporaryPassword();
        
        // Create student
        Student student = new Student();
        student.setFullName(request.getFullName());
        student.setEmail(request.getEmail());
        student.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        student.setContactNumber(request.getContactNumber());
        student.setRole(UserRole.STUDENT);
        student.setStatus(UserStatus.ACTIVE);
        student.setStudentId(request.getStudentId());
        student.setDepartment(request.getDepartment());
        student.setYearOfStudy(request.getYearOfStudy());
        
        User savedUser = userRepository.save(student);
        
        // Send welcome email
        boolean emailSent = emailService.sendWelcomeEmail(
            request.getEmail(), 
            request.getFullName(), 
            temporaryPassword, 
            "Student"
        );
        
        if (!emailSent) {
            log.warn("Welcome email failed for user: {}", request.getEmail());
        }
        
        log.info("Student account created successfully with ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }
    
    public UserResponse createAdmin(CreateAdminRequest request) {
        log.info("Creating admin account for email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        // Create admin
        Admin admin = new Admin();
        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setContactNumber(request.getContactNumber());
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setMustChangePassword(false); // Admin sets their own password
        admin.setLastPasswordChange(java.time.LocalDateTime.now());
        admin.setAdminId(request.getAdminId());
        admin.setDepartment(request.getDepartment());
        admin.setPermissions(request.getPermissions() != null ? request.getPermissions() : "ALL");
        
        User savedUser = userRepository.save(admin);
        
        log.info("Admin account created successfully with ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }
    

    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
            .map(this::convertToUserResponse);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return convertToUserResponse(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return convertToUserResponse(user);
    }
    
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        // Check if new email already exists (if email is being changed)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        // Update common fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getContactNumber() != null) {
            user.setContactNumber(request.getContactNumber());
        }
        
        // Update role-specific fields
        if (user instanceof Librarian librarian) {
            if (request.getEmployeeId() != null) {
                librarian.setEmployeeId(request.getEmployeeId());
            }
            if (request.getBranch() != null) {
                librarian.setBranch(request.getBranch());
            }
            if (request.getWorkShift() != null) {
                librarian.setWorkShift(request.getWorkShift());
            }
        } else if (user instanceof Student student) {
            if (request.getStudentId() != null) {
                student.setStudentId(request.getStudentId());
            }
            if (request.getDepartment() != null) {
                student.setDepartment(request.getDepartment());
            }
            if (request.getYearOfStudy() != null) {
                student.setYearOfStudy(request.getYearOfStudy());
            }
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", id);
        return convertToUserResponse(updatedUser);
    }
    
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
        
        log.info("User deactivated successfully with ID: {}", id);
    }
    
    public void activateUser(Long id) {
        log.info("Activating user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("User activated successfully with ID: {}", id);
    }
    
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // User has changed password
        user.setLastPasswordChange(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Password changed successfully for user ID: {}", userId);
    }
    
    public void changePasswordFirstTime(Long userId, FirstTimePasswordChangeRequest request) {
        log.info("First-time password change for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Check if user must change password
        if (!user.isMustChangePassword()) {
            throw new IllegalArgumentException("User is not required to change password");
        }
        
        // Verify current password (temporary password)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        // Validate new password strength (basic validation)
        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        
        // Update password and remove the requirement to change password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setLastPasswordChange(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        log.info("First-time password changed successfully for user ID: {}", userId);
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setContactNumber(user.getContactNumber());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setMustChangePassword(user.isMustChangePassword());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastPasswordChange(user.getLastPasswordChange());
        
        // Set role-specific fields
        if (user instanceof Admin admin) {
            // For admin, we can use employeeId field to store adminId
            response.setEmployeeId(admin.getAdminId());
            response.setDepartment(admin.getDepartment());
            response.setPermissions(admin.getPermissions());
        } else if (user instanceof Librarian librarian) {
            response.setEmployeeId(librarian.getEmployeeId());
            response.setBranch(librarian.getBranch());
            response.setWorkShift(librarian.getWorkShift());
        } else if (user instanceof Student student) {
            response.setStudentId(student.getStudentId());
            response.setDepartment(student.getDepartment());
            response.setYearOfStudy(student.getYearOfStudy());
            response.setBorrowLimit(student.getBorrowLimit());
            response.setBorrowedCount(student.getBorrowedCount());
        }
        
        return response;
    }
}
