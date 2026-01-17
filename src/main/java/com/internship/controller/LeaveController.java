package com.internship.controller;

import com.internship.dto.CreateLeaveRequest;
import com.internship.dto.CreateLeaveResponse;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.exception.BusinessException;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.LeaveRepository;
import com.internship.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.internship.exception.ApiError.EMPLOYEE_NOT_FOUND;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;

    public int getLeaveCountByYear(Long empId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        return leaveRepository.countByEmployeeIdAndLeaveDateBetween(empId, startOfYear, endOfYear);
    }

    @PostMapping
    public ResponseEntity<List<CreateLeaveResponse>> createLeave(
            @RequestBody @Valid final CreateLeaveRequest request
    ) {
        Long id = request.getEmployeeId();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        int leaveDays = getLeaveCountByYear(employee.getId(), request.getStartDate().getYear());
        int maxNumberOfLeave = employeeService.getTheNumberOfLeaveDays(employee.getJoinedDate());
        List<Leave> leaves = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            if (currentDate.getDayOfWeek() != DayOfWeek.FRIDAY && currentDate.getDayOfWeek() != DayOfWeek.SATURDAY) {
                leaveDays++;
                leaves.add(
                        Leave.builder()
                                .leaveDate(currentDate)
                                .salaryDeducted(leaveDays > maxNumberOfLeave)
                                .employee(employee)
                                .build()
                );
            }
            currentDate = currentDate.plusDays(1);
        }
        leaveRepository.saveAll(leaves);
        List<CreateLeaveResponse> response = new ArrayList<>();
        for (Leave leave : leaves) {
            if (leave.getLeaveDate().getDayOfWeek() != DayOfWeek.FRIDAY
                    && leave.getLeaveDate().getDayOfWeek() != DayOfWeek.SATURDAY) {
                response.add(
                        CreateLeaveResponse.builder()
                                .id(leave.getId())
                                .employeeId(id)
                                .leaveDate(leave.getLeaveDate())
                                .salaryDeducted(leave.isSalaryDeducted())
                                .build()
                );
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
