package com.amarakeerthi.userservice.models;

import jakarta.persistence.Entity;
import lombok.*;

import static com.amarakeerthi.userservice.constants.LibraryConstants.MAX_BOOKS_RESERVED;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User {

    private String studentId;
    private String department;
    private int yearOfStudy;
    private int borrowLimit = MAX_BOOKS_RESERVED;
    private int borrowedCount = 0;
}
