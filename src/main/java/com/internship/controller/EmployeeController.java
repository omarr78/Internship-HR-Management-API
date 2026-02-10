package com.internship.controller;

import com.internship.dto.*;
import com.internship.entity.Employee;
import com.internship.entity.EmployeeSalary;
import com.internship.exception.BusinessException;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.EmployeeSalaryRepository;
import com.internship.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.internship.enums.SalaryReason.SALARY_UPDATED;
import static com.internship.exception.ApiError.EMPLOYEE_NOT_FOUND;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService service;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody @Valid CreateEmployeeRequest request) {
        EmployeeResponse employee = service.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@RequestBody @Valid final UpdateEmployeeRequest request,
                                                           @PathVariable final Long id) {
        EmployeeResponse response = service.modifyEmployee(request, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}/salary")
    public ResponseEntity<SalaryResponse> updateSalary(@RequestBody @Valid final UpdateSalaryRequest request,
                                                       @PathVariable final Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        // insert employee salary in employee-salaries table
        EmployeeSalary employeeSalary = EmployeeSalary.builder()
                .grossSalary(request.getGrossSalary())
                .reason(SALARY_UPDATED.getMessage())
                .employee(employee)
                .build();

        EmployeeSalary savedEmployeeSalary = employeeSalaryRepository.save(employeeSalary);
        SalaryResponse response = SalaryResponse.builder()
                .id(savedEmployeeSalary.getId())
                .creationDate(savedEmployeeSalary.getCreationDate())
                .grossSalary(savedEmployeeSalary.getGrossSalary())
                .reason(savedEmployeeSalary.getReason())
                .employeeId(savedEmployeeSalary.getEmployee().getId())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable final Long id) {
        EmployeeResponse response = service.getEmployee(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeResponse> deleteEmployee(@PathVariable final Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<SalaryDto> getEmployeeSalary(@PathVariable final Long id) {
        SalaryDto salaryResponse = service.getEmployeeSalaryInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(salaryResponse);
    }

    @GetMapping("/{managerId}/hierarchy")
    public ResponseEntity<List<EmployeeResponse>> getAllSubordinatesHierarchy(@PathVariable final Long managerId) {
        List<EmployeeResponse> employeeResponses = service.getEmployeesUnderManagerRecursively(managerId);
        return ResponseEntity.status(HttpStatus.OK).body(employeeResponses);
    }

    @GetMapping("/{managerId}/subordinates")
    public ResponseEntity<List<EmployeeResponse>> getDirectSubordinates(@PathVariable final Long managerId) {
        List<EmployeeResponse> employeeResponses = service.getDirectSubordinates(managerId);
        return ResponseEntity.status(HttpStatus.OK).body(employeeResponses);
    }
}