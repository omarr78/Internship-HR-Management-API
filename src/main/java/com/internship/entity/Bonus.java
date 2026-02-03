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
@Table(name = "BONUSES")
public class Bonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "BONUS_DATE", nullable = false)
    private LocalDate bonusDate;

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    @JsonBackReference("employee-bonuses")
    private Employee employee;

    public Bonus(LocalDate bonusDate, BigDecimal amount, Employee employee) {
        this.bonusDate = bonusDate;
        this.amount = amount;
        this.employee = employee;
    }
}