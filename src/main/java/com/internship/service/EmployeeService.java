package com.internship.service;

import com.internship.dto.*;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.TeamRepository;
import com.internship.validation.aspect.ValidateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.internship.exception.ApiError.*;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final int MIN_YEARS_FOR_EXTRA_LEAVE = 10;
    private static final int STANDARD_LEAVE_DAYS = 21;
    private static final int EXTENDED_LEAVE_DAYS = 30;
    private static final float TAX_REMINDER = 0.85f;
    private static final int INSURANCE_AMOUNT = 500;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final TeamRepository teamRepository;
    private final ExpertiseService expertiseService;

    @Transactional
    @ValidateRequest
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                        "Department not found with id: " + request.getDepartmentId()));
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                        "Team not found with id: " + request.getTeamId()));
        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
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
        return employeeMapper.toResponse(savedEmployee,
                calculateYearsOfExperience(employee.getPastExperienceYear(), employee.getJoinedDate()),
                getTheNumberOfLeaveDays(employee.getJoinedDate()));
    }

    @Transactional
    @ValidateRequest
    public EmployeeResponse modifyEmployee(UpdateEmployeeRequest request, Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));

        Department department = employee.getDepartment();
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                            "Department not found with id: " + request.getDepartmentId()));
        }

        Team team = employee.getTeam();
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                            "Team not found with id: " + request.getTeamId()));
        }

        Employee manager = employee.getManager();
        if (request.getManagerId() != null) {
            Optional<Long> managerId = request.getManagerId();
            if (managerId.isPresent()) {
                if (managerId.get().equals(id)) {
                    throw new BusinessException(SELF_MANAGEMENT);
                }
                manager = employeeRepository.findById(managerId.get())
                        .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                                "Manager not found with id: " + request.getManagerId()));
            } else {
                manager = null;
            }
        }

        List<Expertise> expertises = employee.getExpertises();
        if (request.getExpertises() != null) {
            // remove Empty
            List<String> expertiseNames = removeEmptyNames(request.getExpertises());
            // create expertise if it is not found in database
            expertiseService.createNotFoundExpertise(expertiseNames);
            // get all expertise
            expertises = expertiseService.getExpertises(expertiseNames);
        }
        Employee updatedEmployee =
                buildUpdatedEmployeeFromOldEmployee(employee, request, department, team, manager, expertises);
        Employee savedEmployee = employeeRepository.saveAndFlush(updatedEmployee);
        return employeeMapper.toResponse(savedEmployee,
                calculateYearsOfExperience(employee.getPastExperienceYear(), employee.getJoinedDate()),
                getTheNumberOfLeaveDays(employee.getJoinedDate()));
    }

    private List<String> removeEmptyNames(List<String> expertiseNames) {
        return expertiseNames.stream().filter(name -> !name.isEmpty()).toList();
    }

    @Transactional
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        return employeeMapper.toResponse(employee,
                calculateYearsOfExperience(employee.getPastExperienceYear(), employee.getJoinedDate()),
                getTheNumberOfLeaveDays(employee.getJoinedDate()));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        Employee manager = employee.getManager();
        if (manager == null) // if the employee has no manager then it can't be deleted
        {
            throw new BusinessException(INVALID_EMPLOYEE_REMOVAL);
        }
        employeeRepository.reassignManager(employee.getId(), manager.getId());
        employeeRepository.delete(employee);
    }

    public SalaryDto getEmployeeSalaryInfo(Long id) {
        // check employee with id exists
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + id));
        float netSalary = employee.getGrossSalary() * TAX_REMINDER - INSURANCE_AMOUNT;
        // prevent negative salaries
        if (netSalary < 0) {
            throw new BusinessException(NEGATIVE_SALARY);
        }
        return SalaryDto.builder()
                .grossSalary(employee.getGrossSalary())
                .netSalary(netSalary).build();
    }

    public List<EmployeeResponse> getEmployeesUnderManagerRecursively(Long managerId) {
        // check employee with id exists
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + managerId));

        List<EmployeeDtoInterface> employeesUnderManager = employeeRepository.getAllEmployeesUnderManager(managerId);
        return employeesUnderManager.stream()
                .map(employee ->
                        employeeMapper.fromInterfaceToResponse(employee,
                                calculateYearsOfExperience(employee.getPastExperienceYear(), employee.getJoinedDate()),
                                getTheNumberOfLeaveDays(employee.getJoinedDate()))).toList();
    }

    public List<EmployeeResponse> getDirectSubordinates(Long managerId) {
        // check employee with id exists
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                        "Employee not found with id: " + managerId));

        return employeeRepository.findByManagerId(managerId).stream().map(employee ->
                employeeMapper.toResponse(employee,
                        calculateYearsOfExperience(employee.getPastExperienceYear(), employee.getJoinedDate()),
                        getTheNumberOfLeaveDays(employee.getJoinedDate()))).toList();
    }

    public Employee buildUpdatedEmployeeFromOldEmployee(Employee employee, UpdateEmployeeRequest request,
                                                        Department department, Team team,
                                                        Employee manager, List<Expertise> expertises) {
        return Employee.builder()
                .id(employee.getId())
                .firstName(request.getFirstName() != null ? request.getFirstName() : employee.getFirstName())
                .lastName(request.getLastName() != null ? request.getLastName() : employee.getLastName())
                .nationalId(request.getNationalId() != null ? request.getNationalId() : employee.getNationalId())
                .degree(request.getDegree() != null ? request.getDegree() : employee.getDegree())
                .pastExperienceYear(request.getPastExperienceYear() != null
                        ? request.getPastExperienceYear() : employee.getPastExperienceYear())
                .joinedDate(request.getJoinedDate() != null ? request.getJoinedDate() : employee.getJoinedDate())
                .dateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth() : employee.getDateOfBirth())
                .graduationDate(request.getGraduationDate() != null
                        ? request.getGraduationDate() : employee.getGraduationDate())
                .gender(request.getGender() != null ? request.getGender() : employee.getGender())
                .grossSalary(request.getGrossSalary() != null ? request.getGrossSalary() : employee.getGrossSalary())
                .department(department)
                .team(team)
                .manager(manager)
                .expertises(expertises)
                .build();
    }

    public int calculateYearsOfExperience(int pastExperience, LocalDate joinedDate) {
        int currentYear = LocalDate.now().getYear();
        int joinedYear = joinedDate.getYear();
        return pastExperience + (currentYear - joinedYear);
    }

    public int getTheNumberOfLeaveDays(LocalDate joinedDate) {
        int currentYear = LocalDate.now().getYear();
        int joinedYear = joinedDate.getYear();
        return currentYear - joinedYear >= MIN_YEARS_FOR_EXTRA_LEAVE
                ? EXTENDED_LEAVE_DAYS : STANDARD_LEAVE_DAYS;
    }
}
