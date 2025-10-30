package com.internship.controller;

import com.internship.entity.Employee;
import com.internship.repository.EmployeeRepository;
import com.internship.service.impl.EmployeeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeServiceImpl service;

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employeeRequest) {
        Employee employee = service.addEmployee(employeeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }
}