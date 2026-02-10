package com.internship.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalaryResponse {
    private Long id;
    private LocalDateTime creationDate;
    private BigDecimal grossSalary;
    private String reason;
    private Long employeeId;
}