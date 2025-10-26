package com.internship.controller;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.CreateEmployeeResponse;
import com.internship.service.impl.EmployeeServiceImpl;
import com.internship.service.interfac.EmployeeService;
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

    @PostMapping()
    public ResponseEntity<CreateEmployeeResponse> CreateEmployee(
            @RequestBody final CreateEmployeeRequest request) {
        CreateEmployeeResponse employee = service.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }
}
