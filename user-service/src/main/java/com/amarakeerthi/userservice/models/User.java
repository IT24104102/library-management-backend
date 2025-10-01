package com.amarakeerthi.userservice.models;

import com.amarakeerthi.userservice.constants.UserRole;
import com.amarakeerthi.userservice.constants.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String contactNumber;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private boolean mustChangePassword = true; // Force password change on first login
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime lastPasswordChange;
}
