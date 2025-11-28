package com.internship.service;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.SalaryDto;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.internship.exception.ApiError.*;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final float TAX_REMINDER = 0.85f;
    private static final int INSURANCE_AMOUNT = 500;
    private static final int MAX_DIFFERENCE_YEARS = 20;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final TeamRepository teamRepository;
    private final ExpertiseService expertiseService;

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
            List<String> expertiseNames = removeEmptyNames(request.getExpertises());
            expertiseService.createNotFoundExpertise(expertiseNames);
            expertises = expertiseService.getExpertises(expertiseNames);
        }
        Employee employee = employeeMapper.toEmployee(request, department, team, manager, expertises);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponse(savedEmployee);
    }

    @Transactional
    public EmployeeResponse modifyEmployee(UpdateEmployeeRequest request, Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGraduationDate() != null) {
            employee.setGraduationDate(request.getGraduationDate());
        }
        // the graduation date must be after the date of birth on at least MAX_DIFFERENCE_YEARS
        if (employee.getGraduationDate().getYear() - employee.getDateOfBirth().getYear() < MAX_DIFFERENCE_YEARS) {
            throw new BusinessException(INVALID_EMPLOYEE_DATES_EXCEPTION);
        }
        if (request.getGender() != null) {
            employee.setGender(request.getGender());
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                            "Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId()).
                    orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                            "Team not found with id: " + request.getTeamId()));
            employee.setTeam(team);
        }
        if (request.getManagerId() != null) {
            Optional<Long> managerId = request.getManagerId();
            if (managerId.isPresent()) {
                if (managerId.get().equals(id)) throw new BusinessException(SELF_MANAGEMENT);
                Employee manager = employeeRepository.findById(managerId.get()).
                        orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                                "Manager not found with id: " + request.getManagerId()));
                employee.setManager(manager);
            } else {
                employee.setManager(null);
            }
        }
        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        if (request.getExpertises() != null) {
            // remove Empty
            List<String> expertiseNames = removeEmptyNames(request.getExpertises());
            // create expertise if it is not found in database
            expertiseService.createNotFoundExpertise(expertiseNames);
            // get all expertise
            List<Expertise> expertises = expertiseService.getExpertises(expertiseNames);
            // set employee Expertise
            employee.setExpertises(expertises);
        }
        employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    private List<String> removeEmptyNames(List<String> expertiseNames) {
        return expertiseNames.stream().filter(name -> !name.isEmpty()).toList();
    }

    @Transactional
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        Employee manager = employee.getManager();
        if (manager == null) // if the employee has no manager then it can't be deleted
            throw new BusinessException(INVALID_EMPLOYEE_REMOVAL);
        employeeRepository.reassignManager(employee.getId(), manager.getId());
        employeeRepository.delete(employee);
    }

    public SalaryDto getEmployeeSalaryInfo(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id).
                orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        float netSalary = employee.getSalary() * TAX_REMINDER - INSURANCE_AMOUNT;
        // prevent negative salaries
        if (netSalary < 0) {
            throw new BusinessException(NEGATIVE_SALARY);
        }
        return SalaryDto.builder().salary(netSalary).build();
    }
}
