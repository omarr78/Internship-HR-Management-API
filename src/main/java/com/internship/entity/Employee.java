package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.internship.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EMPLOYEES")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "GRADUATION_DATE", nullable = false)
    private LocalDate graduationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false)
    private Gender gender;

    @Column(name = "SALARY", nullable = false)
    private float salary;

    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID", nullable = false)
    @JsonBackReference("department-employees")
    private Department department;
}

