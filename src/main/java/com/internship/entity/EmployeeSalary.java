package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EMPLOYEE_SALARIES")
public class EmployeeSalary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DATE", nullable = false)
    private LocalDate date;

    @Column(name = "GROSS_SALARY", nullable = false)
    private BigDecimal grossSalary;

    @Column(name = "REASON", nullable = false)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    @JsonBackReference("employee-salary")
    private Employee employee;
}
