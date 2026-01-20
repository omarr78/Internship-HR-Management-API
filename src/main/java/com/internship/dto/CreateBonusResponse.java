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
    private Float amount;
    private Long employeeId;

    public CreateBonusResponse(LocalDate bonusDate, Float amount, Long employeeId) {
        this.bonusDate = bonusDate;
        this.amount = amount;
        this.employeeId = employeeId;
    }
}
