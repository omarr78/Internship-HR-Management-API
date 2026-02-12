package com.internship.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RaiseSalaryRequest {
    @Positive
    private BigDecimal amount;
    private String reason;
}
