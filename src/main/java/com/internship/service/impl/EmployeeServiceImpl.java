package com.internship.service.impl;

import com.internship.entity.Employee;
import com.internship.repository.EmployeeRepository;
import com.internship.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee addEmployee(Employee request) {
        return employeeRepository.save(request);
    }

}
