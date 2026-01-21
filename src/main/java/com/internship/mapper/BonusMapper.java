package com.internship.mapper;

import com.internship.dto.CreateBonusResponse;
import com.internship.entity.Bonus;
import com.internship.entity.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BonusMapper {
    public Bonus toEntity(LocalDate date, Float amount, Employee employee) {
        return Bonus.builder()
                .bonusDate(date)
                .amount(amount)
                .employee(employee)
                .build();
    }

    public CreateBonusResponse toResponse(Bonus bonus) {
        return CreateBonusResponse.builder()
                .id(bonus.getId())
                .bonusDate(bonus.getBonusDate())
                .amount(bonus.getAmount())
                .employeeId(bonus.getEmployee().getId())
                .build();
    }
}
