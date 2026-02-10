package com.internship.mapper;

import com.internship.dto.SalaryResponse;
import com.internship.entity.EmployeeSalary;
import org.springframework.stereotype.Component;

@Component
public class EmployeeSalaryMapper {
    public SalaryResponse toResponse(EmployeeSalary employeeSalary) {
        return SalaryResponse.builder()
                .id(employeeSalary.getId())
                .creationDate(employeeSalary.getCreationDate())
                .grossSalary(employeeSalary.getGrossSalary())
                .reason(employeeSalary.getReason())
                .employeeId(employeeSalary.getEmployee().getId())
                .build();
    }
}
