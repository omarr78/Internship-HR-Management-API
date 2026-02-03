package com.internship.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalaryDto {
    BigDecimal grossSalary;
    BigDecimal netSalary;
}