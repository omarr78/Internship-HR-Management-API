package com.internship.controller;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.repository.EmployeeRepository;
import com.internship.service.impl.EmployeeServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;

import static com.internship.enums.Gender.MALE;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeServiceImpl service;

    @PostMapping()
    public ResponseEntity<EmployeeResponse> CreateEmployee(
            @RequestBody @Valid final CreateEmployeeRequest request) {
        EmployeeResponse employee = service.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> UpdateEmployee(@RequestBody @Valid final UpdateEmployeeRequest request,
                                                           @PathVariable final Long id) {
        EmployeeResponse response = service.modifyEmployee(request, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
