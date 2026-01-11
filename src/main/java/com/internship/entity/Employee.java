package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.internship.enums.Degree;
import com.internship.enums.Gender;
import com.internship.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static com.internship.exception.ApiError.INVALID_EMPLOYEE_DATES_EXCEPTION;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EMPLOYEES")
public class Employee {
    private static final int MAX_DIFFERENCE_YEARS = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "NATIONAL_ID", nullable = false, unique = true)
    private String nationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "DEGREE", nullable = false)
    private Degree degree;

    @Column(name = "PAST_EXPERIENCE_YEAR", nullable = false)
    private Integer pastExperienceYear;

    @Column(name = "JOINED_DATE", nullable = false)
    private LocalDate joinedDate;

    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "GRADUATION_DATE", nullable = false)
    private LocalDate graduationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false)
    private Gender gender;

    @Column(name = "GROSS_SALARY", nullable = false)
    private float grossSalary;

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

    @ManyToMany
    @JoinTable(
            name = "EMPLOYEE_EXPERTISE",
            joinColumns = @JoinColumn(name = "EMPLOYEE_ID"),
            inverseJoinColumns = @JoinColumn(name = "EXPERTISE_ID")
    )
    private List<Expertise> expertises;

    @PrePersist
    @PreUpdate
    private void validateGraduationAndBirthDate() {
        if (dateOfBirth != null && graduationDate != null) {
            int years = Period.between(dateOfBirth, graduationDate).getYears();
            if (years < MAX_DIFFERENCE_YEARS) {
                throw new BusinessException(INVALID_EMPLOYEE_DATES_EXCEPTION);
            }
        }
    }
}

