package com.internship.mapper;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import org.springframework.stereotype.Component;


@Component
public class EmployeeMapper {
    public Employee toEmployee(CreateEmployeeRequest request,
                               Department department) {
        return Employee.builder()
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .graduationDate(request.getGraduationDate())
                .gender(request.getGender())
                .salary(request.getSalary())
                .department(department)
                .build();
    }

    public EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .dateOfBirth(employee.getDateOfBirth())
                .graduationDate(employee.getGraduationDate())
                .gender(employee.getGender())
                .salary(employee.getSalary())
                .departmentId(employee.getDepartment().getId())
                .build();
    }
}
