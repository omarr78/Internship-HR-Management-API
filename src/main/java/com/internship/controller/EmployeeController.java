package com.internship.controller;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.SalaryDto;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.enums.GetEmployeeType;
import com.internship.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService service;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody @Valid CreateEmployeeRequest request) {
        EmployeeResponse employee = service.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> UpdateEmployee(@RequestBody @Valid final UpdateEmployeeRequest request,
                                                           @PathVariable final Long id) {
        EmployeeResponse response = service.modifyEmployee(request, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable final Long id) {
        EmployeeResponse response = service.getEmployee(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeResponse> DeleteEmployee(@PathVariable final Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<SalaryDto> getEmployeeSalary(@PathVariable final Long id) {
        SalaryDto salaryResponse = service.getEmployeeSalaryInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(salaryResponse);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) final Long managerId,
            @RequestParam(required = false) final Long teamId,
            @RequestParam(required = false) final String type) {
        List<EmployeeResponse> employeeResponses = List.of();
        GetEmployeeType requestType = service.convertToType(type);
        switch (requestType) {
            case RECURSIVE -> employeeResponses = service.getAllEmployeesUnderManager(managerId);
            case TEAM -> employeeResponses = service.getAllEmployeeUnderTeam(teamId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeResponses);
    }
}