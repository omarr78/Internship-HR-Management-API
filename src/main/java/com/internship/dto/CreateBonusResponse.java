package com.internship.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBonusResponse {
    private Long id;
    private LocalDate bonusDate;
    private Long employeeId;

    public CreateBonusResponse(LocalDate bonusDate, Long employeeId) {
        this.bonusDate = bonusDate;
        this.employeeId = employeeId;
    }
}
