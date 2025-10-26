package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.internship.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID", nullable = false)
    @JsonBackReference("department-employees")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID", nullable = false)
    @JsonBackReference("team-employees")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "MANAGER_ID")
    @JsonBackReference("manager-subordinates")
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    @JsonManagedReference("manager-subordinates")
    private List<Employee> subordinates;

    @Column(name = "SALARY", nullable = false)
    private float salary;

    @ManyToMany
    @JoinTable(
            name = "EMPLOYEE_EXPERTISE",
            joinColumns = @JoinColumn(name = "EMPLOYEE_ID"),
            inverseJoinColumns = @JoinColumn(name = "EXPERTISE_ID")
    )
    private List<Expertise> expertises;
}
