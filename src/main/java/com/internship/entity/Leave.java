package com.internship.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "leaves",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_leaves",
                        columnNames = {"employee_id", "leave_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Column(name = "is_salary_deducted", nullable = false)
    private boolean salaryDeducted;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-leaves")
    private Employee employee;
}