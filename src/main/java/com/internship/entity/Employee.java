package com.internship.entity;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private LocalDate graduationDate;
    private String gender;
    private float salary;
}

