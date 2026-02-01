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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.internship.exception.ApiError.DUPLICATE_LEAVE_EXCEPTION;
import static com.internship.exception.ApiError.EMPLOYEE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final LeaveMapper leaveMapper;

    @Transactional
    @ValidateLeaveDates
    public List<CreateLeaveResponse> addLeave(CreateLeaveRequest request) {
        Long id = request.getEmployeeId();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        List<Leave> leaves = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            if (currentDate.getDayOfWeek() != DayOfWeek.FRIDAY && currentDate.getDayOfWeek() != DayOfWeek.SATURDAY) {
                Leave newLeave = leaveMapper.toEntity(currentDate, employee);
                leaves.add(newLeave);
            }
            currentDate = currentDate.plusDays(1);
        }
        try {
            leaveRepository.saveAll(leaves);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(DUPLICATE_LEAVE_EXCEPTION);
        }
        return leaves.stream().map(leaveMapper::toResponse).toList();
    }
}
