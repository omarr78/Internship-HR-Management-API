package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "employee_payroll",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_employee_payroll_month",
                        columnNames = {"employee_id", "payroll_year", "payroll_month"}
                )
        }
)
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PAYROLL_YEAR", nullable = false, updatable = false)
    private Integer payrollYear;

    @Column(name = "PAYROLL_MONTH", nullable = false, updatable = false)
    private Integer payrollMonth;

    @Column(name = "GROSS_SALARY", nullable = false, updatable = false)
    private BigDecimal grossSalary;

    @Column(name = "BONUS", nullable = false, updatable = false)
    private BigDecimal bonus;

    @Column(name = "TAX_AMOUNT", nullable = false, updatable = false)
    private BigDecimal taxAmount;

    @Column(name = "INSURANCE_DEDUCTION", nullable = false, updatable = false)
    private BigDecimal insuranceDeduction;

    @Column(name = "LEAVES_DEDUCTION", nullable = false, updatable = false)
    private BigDecimal leavesDeduction;

    @Column(name = "NET_SALARY", nullable = false, updatable = false)
    private BigDecimal netSalary;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-payroll")
    private Employee employee;
}