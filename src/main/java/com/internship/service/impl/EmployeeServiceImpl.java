package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Team;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.TeamRepository;
import com.internship.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.internship.exception.ApiError.*;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final TeamRepository teamRepository;

    @Override
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        if(!request.getDateOfBirth().isBefore(request.getGraduationDate())) {
            throw new BusinessException(INVALID_EMPLOYEE_DATES_EXCEPTION);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                        "Department not found with id: " + request.getDepartmentId()));

        Team team = teamRepository.findById(request.getTeamId()).
                orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                        "Team not found with id: " + request.getTeamId()));

        Employee employee = employeeMapper.toEmployee(request,department,team);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponse(savedEmployee);
    }
}
