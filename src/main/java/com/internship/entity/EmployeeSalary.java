package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "EMPLOYEE_SALARIES")
public class EmployeeSalary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CREATION_DATE", nullable = false, updatable = false)
    @CreatedDate
    private LocalDate creationDate;

    @Column(name = "GROSS_SALARY", nullable = false, updatable = false)
    private BigDecimal grossSalary;

    @Column(name = "REASON", nullable = false, updatable = false)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false, updatable = false)
    @JsonBackReference("employee-salary")
    private Employee employee;
}
