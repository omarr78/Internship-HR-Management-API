package com.internship.controller;

import com.internship.dto.CreateLeaveRequest;
import com.internship.dto.CreateLeaveResponse;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.LeaveRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;

    @PostMapping
    public ResponseEntity<List<CreateLeaveResponse>> createEmployee(
            @RequestBody @Valid final CreateLeaveRequest request
    ) {
        Long id = request.getEmployeeId();
        Employee employee = employeeRepository.findById(id).get();
        List<Leave> leaves = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            leaves.add(
                    Leave.builder()
                            .leaveDate(currentDate)
                            .salaryDeducted(false)
                            .employee(employee)
                            .build()
            );
            currentDate = currentDate.plusDays(1);
        }
        leaveRepository.saveAll(leaves);
        List<CreateLeaveResponse> response = new ArrayList<>();
        for (Leave leave : leaves) {
            response.add(
                    CreateLeaveResponse.builder()
                            .id(leave.getId())
                            .employeeId(id)
                            .leaveDate(leave.getLeaveDate())
                            .salaryDeducted(false)
                            .build()
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
