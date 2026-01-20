package com.internship.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBonusRequest {
    private LocalDate bonusDate;

    @NotNull(message = "bonus amount is required")
    @Positive
    private Float amount;

    @NotNull(message = "employeeId is required")
    private Long employeeId;
}