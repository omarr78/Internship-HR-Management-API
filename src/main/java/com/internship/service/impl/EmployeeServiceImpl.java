package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Employee;
import com.internship.repository.EmployeeRepository;
import com.internship.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();

        employee.setName(request.getName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGraduationDate(request.getGraduationDate());
        employee.setGender(request.getGender());
        employee.setSalary(request.getSalary());

        Employee savedEmployee = employeeRepository.save(employee);

        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setId(savedEmployee.getId());
        employeeResponse.setName(savedEmployee.getName());
        employeeResponse.setDateOfBirth(savedEmployee.getDateOfBirth());
        employeeResponse.setGraduationDate(savedEmployee.getGraduationDate());
        employeeResponse.setGender(savedEmployee.getGender());
        employeeResponse.setSalary(savedEmployee.getSalary());

        return employeeResponse;
    }

}
