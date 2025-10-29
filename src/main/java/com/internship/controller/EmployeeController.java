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
import java.util.List;
import java.util.Map;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeResponse> DeleteEmployee(@PathVariable final Long id) {
        service.removeEmployee(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable final Long id) {
        EmployeeResponse response = service.getEmployee(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<Map<String,Float>> getEmployeeSalary(@PathVariable final Long id) {
        float salary = service.getEmployeeSalaryInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("salary", salary));
    }

    @GetMapping("/under-manager/{managerId}")
    public ResponseEntity<List<EmployeeResponse>> getUnderManager(@PathVariable final Long managerId) {
        List<EmployeeResponse> employeeResponses = service.getAllEmployeesUnderManager(managerId);
        return ResponseEntity.status(HttpStatus.OK).body(employeeResponses);
    }

}
