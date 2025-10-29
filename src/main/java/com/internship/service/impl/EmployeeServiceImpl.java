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
import com.internship.service.interfac.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.internship.exception.ApiError.*;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final ExpertiseRepository expertiseRepository;

    @Override
    @Transactional
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
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
            for (String expertise : request.getExpertises()) {
                Optional<Expertise> optional = expertiseRepository.findExpertiseByName(expertise);
                if (optional.isPresent()) {
                    expertises.add(optional.get());
                } else {
                    Expertise exp = Expertise.builder().name(expertise).build();
                    Expertise savedExpertise = expertiseRepository.save(exp);
                    expertises.add(savedExpertise);
                }
            }
        }

        Employee employee = employeeMapper.toEmployee(request, department, team, manager, expertises);
        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
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

        if (request.getGender() != null && !request.getGender().equals(employee.getGender())) {
            employee.setGender(request.getGender());
        }

        if (request.getDepartmentId() != null && !request.getDepartmentId().equals(employee.getDepartment().getId())) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                            "Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }

        if (request.getTeamId() != null && !request.getTeamId().equals(employee.getTeam().getId())) {
            Team team = teamRepository.findById(request.getTeamId()).
                    orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                            "Team not found with id: " + request.getTeamId()));
            employee.setTeam(team);
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            // check SELF_MANAGEMENT Error
            if (request.getManagerId().equals(id)) {
                throw new BusinessException(SELF_MANAGEMENT);
            }
            manager = employeeRepository.findById(request.getManagerId()).
                    orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                            "Manager not found with id: " + request.getManagerId()));
        }
        employee.setManager(manager);

        if (request.getSalary() != 0.0f) {
            employee.setSalary(request.getSalary());
        }

        if (request.getExpertises() != null) {
            List<Expertise> expertises = new ArrayList<>();
            for (String expertise : request.getExpertises()) {
                Optional<Expertise> optional = expertiseRepository.findExpertiseByName(expertise);
                if (optional.isPresent()) {
                    expertises.add(optional.get());
                } else {
                    Expertise exp = Expertise.builder().name(expertise).build();
                    Expertise savedExpertise = expertiseRepository.save(exp);
                    expertises.add(savedExpertise);
                }
            }
            employee.setExpertises(expertises);
        }
        employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public void removeEmployee(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        if (employee.getSubordinates() != null && !employee.getSubordinates().isEmpty()) {
            if (employee.getManager() != null) {
                Employee manager = employee.getManager();
                for (Employee subordinate : employee.getSubordinates()) {
                    subordinate.setManager(manager);
                }
            } else {
                throw new BusinessException(INVALID_EMPLOYEE_REMOVAL);
            }
        }
        employeeRepository.delete(employee);
    }

    @Override
    public EmployeeResponse getEmployee(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        return employeeMapper.toResponse(employee);
    }

    @Override
    public float getEmployeeSalaryInfo(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        return employee.getSalary() * 0.85F - 500F;
    }

    @Override
    public List<EmployeeResponse> getAllEmployeesUnderManager(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        List<Employee> employees = getEmployeesByBfs(employee);
        return employees.stream().map(employeeMapper::toResponse).toList();
    }

    private List<Employee> getEmployeesByBfs(Employee employee) {
        List<Employee> employees = new ArrayList<>();
        Set<Long> visitedEmployeeIds = new HashSet<>();
        Queue<Employee> queue = new LinkedList<>();
        queue.add(employee);
        while (!queue.isEmpty()) {
            Employee mngr = queue.poll();
            if(!mngr.getId().equals(employee.getId())) {
                employees.add(mngr);
            }
            if (mngr.getSubordinates() != null && !mngr.getSubordinates().isEmpty()) {
                for (Employee sub : mngr.getSubordinates()) {
                    if (!visitedEmployeeIds.contains(sub.getId())) {
                        visitedEmployeeIds.add(sub.getId());
                        queue.add(sub);
                    }
                }
            }
        }
        return employees;
    }
}
