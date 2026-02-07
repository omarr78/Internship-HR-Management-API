package com.internship.mapper;

import com.internship.entity.Employee;
import com.internship.entity.EmployeeSalary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmployeeSalaryMapper {
    public EmployeeSalary toEntity(BigDecimal grossSalary, String reason, Employee employee) {
        return EmployeeSalary.builder()
                .grossSalary(grossSalary)
                .reason(reason)
                .employee(employee)
                .build();
    }
}
