package com.internship.mapper;

import com.internship.dto.CreateLeaveResponse;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LeaveMapper {
    public Leave toEntity(LocalDate date, Boolean salaryDeducted, Employee employee) {
        return Leave.builder()
                .leaveDate(date)
                .salaryDeducted(salaryDeducted)
                .employee(employee)
                .build();
    }

    public CreateLeaveResponse toResponse(Leave leave) {
        return CreateLeaveResponse.builder()
                .id(leave.getId())
                .leaveDate(leave.getLeaveDate())
                .salaryDeducted(leave.isSalaryDeducted())
                .employeeId(leave.getEmployee().getId())
                .build();
    }
}