package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Employee;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.EmployeeRepository;
import com.internship.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        Employee employee = employeeMapper.toEmployee(request);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponse(savedEmployee);
    }

}
