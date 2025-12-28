package com.internship.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalaryDto {
    float grossSalary;
    float netSalary;
}