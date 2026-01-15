package com.internship.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLeaveResponse {
    private Long id;
    private LocalDate leaveDate;
    private boolean salaryDeducted;
    private Long employeeId;
}