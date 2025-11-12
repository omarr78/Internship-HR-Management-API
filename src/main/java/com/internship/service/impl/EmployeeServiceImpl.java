package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.ExpertiseRepository;
import com.internship.repository.TeamRepository;
import com.internship.service.EmployeeService;
import com.internship.service.ExpertiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.internship.exception.ApiError.*;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final TeamRepository teamRepository;
    private final ExpertiseService ExpertiseService;
    private final ExpertiseService expertiseService;

    @Override
    @Transactional
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        // the graduation date must be after the date of birth on at least 20 years
        if (request.getGraduationDate().getYear() - request.getDateOfBirth().getYear() < 20) {
            throw new BusinessException(INVALID_EMPLOYEE_DATES_EXCEPTION);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                        "Department not found with id: " + request.getDepartmentId()));

        Team team = teamRepository.findById(request.getTeamId()).
                orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                        "Team not found with id: " + request.getTeamId()));

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId()).
                    orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                            "Manager not found with id: " + request.getManagerId()));
        }

        List<Expertise> expertises = new ArrayList<>();
        if (request.getExpertises() != null) {
            expertises = expertiseService.getExpertises(request.getExpertises());
        }
        Employee employee = employeeMapper.toEmployee(request, department, team, manager, expertises);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    public EmployeeResponse modifyEmployee(UpdateEmployeeRequest request, Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(employee.getName())) {
            employee.setName(request.getName());
        }

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().equals(employee.getDateOfBirth())) {
            employee.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getGraduationDate() != null && !request.getGraduationDate().equals(employee.getGraduationDate())) {
            employee.setGraduationDate(request.getGraduationDate());
        }

        // the graduation date must be after the date of birth on at least 20 years
        if (employee.getGraduationDate().getYear() - employee.getDateOfBirth().getYear() < 20) {
            throw new BusinessException(INVALID_EMPLOYEE_DATES_EXCEPTION);
        }

        if (request.getGender() != null && !request.getGender().equals(employee.getGender())) {
            employee.setGender(request.getGender());
        }

        if (request.getDepartmentId() != null) { // if a user enters the same departmentId, then we check if the department still exists
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                            "Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }

        if (request.getTeamId() != null) { // if a user enters the same teamId, then we check if the team still exists
            Team team = teamRepository.findById(request.getTeamId()).
                    orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                            "Team not found with id: " + request.getTeamId()));
            employee.setTeam(team);
        }

        if (request.getManagerId() != null) {
            Optional<Long> managerId = request.getManagerId();

            if(managerId.isPresent()) {
                if(managerId.get().equals(id)) throw new BusinessException(SELF_MANAGEMENT);
                Employee manager = employeeRepository.findById(managerId.get()).
                        orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                                "Manager not found with id: " + request.getManagerId()));
                employee.setManager(manager);
            }
            else{
                employee.setManager(null);
            }
        }

        if (request.getSalary() != 0.0f) {
            employee.setSalary(request.getSalary());
        }

        if (request.getExpertises() != null) {
            List<Expertise> expertises = expertiseService.getExpertises(request.getExpertises());
            employee.setExpertises(expertises);
        }

        employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

}
