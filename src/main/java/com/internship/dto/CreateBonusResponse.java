package com.internship.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBonusResponse {
    private Long id;
    private LocalDate bonusDate;
    private BigDecimal amount;
    private Long employeeId;

    public CreateBonusResponse(LocalDate bonusDate, BigDecimal amount, Long employeeId) {
        this.bonusDate = bonusDate;
        this.amount = amount;
        this.employeeId = employeeId;
    }
}
