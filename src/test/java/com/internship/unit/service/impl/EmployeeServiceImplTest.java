package com.internship.unit.service.impl;

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
import com.internship.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.internship.enums.Gender.MALE;
import static com.internship.exception.ApiError.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl service;

    @Mock
    private EmployeeMapper mapper;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    private CreateEmployeeRequest buildCreateEmployeeRequest() {
        return CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .salary(2000)
                .build();
    }

    private Employee buildEmployee() {
        return Employee.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .salary(2000)
                .build();
    }

    private EmployeeResponse buildEmployeeResponse() {
        return EmployeeResponse.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .salary(2000)
                .build();
    }

    private Department buildDepartment() {
        return Department.builder()
                .id(1L)
                .name("Department 1")
                .build();
    }

    private Team buildTeam() {
        return Team.builder()
                .id(1L)
                .name("Team 1")
                .build();
    }

    @Test
    public void testAddEmployeeWithGraduationDateNotAfterBirthDate_shouldFail() {
        // Given
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        // birth of date must be before graduation date at least 20 years
        // if birth of date after or equal graduation date should fail
        request.setGraduationDate(LocalDate.of(2005, 6, 5));
        request.setDateOfBirth(LocalDate.of(2007, 6, 5));

        // When & Then - should throw an INVALID_EMPLOYEE_DATES_EXCEPTION
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.addEmployee(request));
        assertEquals(INVALID_EMPLOYEE_DATES_EXCEPTION, exception.getApiError());
    }

    @Test
    public void testAddEmployeeWithGraduationDateAfterBirthDateButLessThan20Years_shouldFail() {
        // Given
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        // birth of date must be before graduation date at least 20 years
        // if birth of date after or equal graduation date should fail
        request.setGraduationDate(LocalDate.of(2005, 6, 5));
        request.setDateOfBirth(LocalDate.of(2024, 6, 5));

        // When & Then - should throw an INVALID_EMPLOYEE_DATES_EXCEPTION
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.addEmployee(request));
        assertEquals(INVALID_EMPLOYEE_DATES_EXCEPTION, exception.getApiError());
    }

    @Test
    public void testAddEmployeeWithNotFoundDepartment_shouldFail() {
        // Given
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(1L); // there is no department with this id

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an DEPARTMENT_NOT_FOUND
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.addEmployee(request));
        assertEquals(DEPARTMENT_NOT_FOUND, exception.getApiError());
    }

    @Test
    public void testAddEmployeeWithNotFoundTeam_shouldFail() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L

        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(1L); // there is no team with this id

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.empty());
        // When & Then - should throw an TEAM_NOT_FOUND
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.addEmployee(request));
        assertEquals(TEAM_NOT_FOUND, exception.getApiError());
    }

    @Test
    public void testAddEmployeeWithNotFoundManager_shouldFail() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L

        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());
        request.setManagerId(10L); // there is no employee with this id

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an EMPLOYEE_NOT_FOUND
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.addEmployee(request));
        assertEquals(EMPLOYEE_NOT_FOUND, exception.getApiError());
    }

    @Test
    public void testAddEmployeeWithExistingDepartmentAndTeam_shouldSucceedAndReturnEmployeeInfo() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L

        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());

        Employee employee = buildEmployee();
        employee.setId(1L);
        employee.setDepartment(department);
        employee.setTeam(team);

        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setDepartmentId(department.getId());
        employeeResponse.setTeamId(team.getId());

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(mapper.toEmployee(request,department,team,null)).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(employeeResponse);

        // action
        EmployeeResponse response = service.addEmployee(request);
        // then
        assertNotNull(response);
        assertEquals(response.getId(), employeeResponse.getId());
        assertEquals(response.getName(), employeeResponse.getName());
        assertEquals(response.getDateOfBirth(), employeeResponse.getDateOfBirth());
        assertEquals(response.getGraduationDate(), employeeResponse.getGraduationDate());
        assertEquals(response.getGender(), employeeResponse.getGender());
        assertEquals(response.getSalary(), employeeResponse.getSalary());
        assertEquals(response.getDepartmentId(), employeeResponse.getDepartmentId());
        assertEquals(response.getTeamId(), employeeResponse.getTeamId());
    }

    @Test
    public void testAddEmployeeWithExistingDepartmentAndTeamAndManager_shouldSucceedAndReturnEmployeeInfo() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L
        Employee manager = buildEmployee();
        manager.setId(10L);


        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());
        request.setManagerId(manager.getId());

        Employee employee = buildEmployee();
        employee.setId(1L);
        employee.setDepartment(department);
        employee.setTeam(team);
        employee.setManager(manager);

        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setDepartmentId(department.getId());
        employeeResponse.setTeamId(team.getId());
        employeeResponse.setManagerId(manager.getId());

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.of(manager));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(mapper.toEmployee(request,department,team,manager)).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(employeeResponse);

        // action
        EmployeeResponse response = service.addEmployee(request);
        // then
        assertNotNull(response);
        assertEquals(response.getId(), employeeResponse.getId());
        assertEquals(response.getName(), employeeResponse.getName());
        assertEquals(response.getDateOfBirth(), employeeResponse.getDateOfBirth());
        assertEquals(response.getGraduationDate(), employeeResponse.getGraduationDate());
        assertEquals(response.getGender(), employeeResponse.getGender());
        assertEquals(response.getSalary(), employeeResponse.getSalary());
        assertEquals(response.getDepartmentId(), employeeResponse.getDepartmentId());
        assertEquals(response.getTeamId(), employeeResponse.getTeamId());
        assertEquals(response.getManagerId(), employeeResponse.getManagerId());
    }
}
