package com.internship.controller;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.entity.Employee;
import com.internship.service.interfac.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @PostMapping()
    public ResponseEntity<Employee> CreateEmployee(
            @RequestBody final CreateEmployeeRequest request) {
        Employee employee = service.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }
}
