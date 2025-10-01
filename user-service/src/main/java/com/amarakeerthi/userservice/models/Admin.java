package com.amarakeerthi.userservice.models;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    private String adminId;
    private String department;
    private String permissions; // Could be JSON string or comma-separated values
}