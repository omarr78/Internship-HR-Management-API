package com.internship.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateSalaryRequest {
    @NotNull
    @Min(value = 0, message = "salary must be greater than or equal to 0")
    private BigDecimal grossSalary;
}
