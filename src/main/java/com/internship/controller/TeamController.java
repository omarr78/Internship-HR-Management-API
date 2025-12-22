package com.internship.controller;

import com.internship.dto.EmployeeResponse;
import com.internship.entity.Employee;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @GetMapping("{id}/members")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesUnderTeam(@PathVariable final Long id) {
        List<Employee> employeeResponses = employeeRepository.findByTeamId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeResponses.stream().map(employeeMapper::toResponse).toList());
    }
}
