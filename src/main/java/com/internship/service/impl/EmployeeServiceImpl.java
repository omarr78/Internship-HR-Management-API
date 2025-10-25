package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Team;
import com.internship.exception.ApiError;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.TeamRepository;
import com.internship.service.interfac.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.internship.exception.ApiError.*;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public Employee addEmployee(CreateEmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                        "Department not found with id: " + request.getDepartmentId()));

        Team team = teamRepository.findById(request.getTeamId()).
                orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                        "Team not found with id: " + request.getTeamId()));

        Employee manager = null;
        if(request.getManagerId() != null){
            manager = employeeRepository.findById(request.getManagerId()).
                    orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                            "Manager not found with id: " + request.getManagerId()));
        }

        Employee employee = employeeMapper.toEmployee(request,department,team,manager);
        return employeeRepository.save(employee);
    }
}
