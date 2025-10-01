package com.amarakeerthi.userservice.repositories;

import com.amarakeerthi.userservice.constants.UserRole;
import com.amarakeerthi.userservice.constants.UserStatus;
import com.amarakeerthi.userservice.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    Page<User> findByRole(UserRole role, Pageable pageable);
    
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:name%")
    List<User> findByFullNameContaining(String name);
    
    long countByRole(UserRole role);
    
    long countByStatus(UserStatus status);
}
