package com.amarakeerthi.userservice.models;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Librarian extends User {

    private String employeeId;
    private String branch;
    private String workShift;
}
