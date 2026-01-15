package com.internship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLeaveRequest {
    @NotNull(message = "start date is required")
    LocalDate startDate;
    @NotNull(message = "end date is required")
    LocalDate endDate;
}