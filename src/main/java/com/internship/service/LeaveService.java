package com.internship.service;

import com.internship.dto.CreateLeaveRequest;
import com.internship.dto.CreateLeaveResponse;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.exception.BusinessException;
import com.internship.mapper.LeaveMapper;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.LeaveRepository;
import com.internship.validation.aspect.ValidateLeaveDates;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.internship.exception.ApiError.EMPLOYEE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;
    private final LeaveMapper leaveMapper;

    public int getLeaveCountByYear(Long empId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        return leaveRepository.countByEmployeeIdAndLeaveDateBetween(empId, startOfYear, endOfYear);
    }

    @Transactional
    @ValidateLeaveDates
    public List<CreateLeaveResponse> addLeave(CreateLeaveRequest request) {
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
                Leave newLeave = leaveMapper.toEntity(currentDate, leaveDays > maxNumberOfLeave, employee);
                leaves.add(newLeave);
            }
            currentDate = currentDate.plusDays(1);
        }
        leaveRepository.saveAll(leaves);
        return leaves.stream().map(leaveMapper::toResponse).toList();
    }
}
