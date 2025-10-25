package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Team;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.internship.enums.Gender.MALE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl service;

    private CreateEmployeeRequest request;
    private Employee employee;
    private Employee manager;
    private Department department;
    private Team team;

    @BeforeEach
    void init() {

        department = Department.builder()
                .id(1L)
                .name("Department 1").build();

        team = Team.builder()
                .id(1L)
                .name("Team 1").build();

        manager = Employee.builder()
                .id(2L).build();

        employee = Employee.builder()
                .id(1L)
                .name("Ali")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .department(department)
                .team(team)
                .manager(manager)
                .salary(2000)
                .build();

        request = CreateEmployeeRequest.builder()
                .name("Ali")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .managerId(2L)
                .salary(2000)
                .build();
    }

    @Test
    public void createEmployeeShouldReturnEmployeeWhenSuccess() {
        // Given
        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.of(manager));
        when(employeeMapper.toEmployee(request, department, team, manager))
                .thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        Employee emp = service.addEmployee(request);

        // Then
        assertNotNull(emp);
        assertEquals(emp.getId(), employee.getId());
        assertEquals(emp.getName(), employee.getName());
        assertEquals(emp.getDateOfBirth(), employee.getDateOfBirth());
        assertEquals(emp.getGraduationDate(), employee.getGraduationDate());
        assertEquals(emp.getGender(), employee.getGender());
        assertEquals(emp.getDepartment(), employee.getDepartment());
        assertEquals(emp.getTeam(), employee.getTeam());
        assertEquals(emp.getManager(), employee.getManager());
        assertEquals(emp.getSalary(), employee.getSalary());
    }

    @Test
    public void createEmployeeShouldReturnDepartmentNotFoundWhenDepartmentNotFound() {
        // Given
        request.setDepartmentId(10L); // there is no department with this id

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an DEPARTMENT_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    @Test
    public void createEmployeeShouldReturnTeamNotFoundWhenTeamNotFound() {
        // Given
        request.setTeamId(10L); // there is no team with this id

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw a TEAM_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    @Test
    public void createEmployeeShouldReturnEmployeeNotFoundWhenManagerNotFound() {
        // Given
        request.setManagerId(10L); // there is no employee with this id

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an EMPLOYEE_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    // when manager id is null, that means that employee not have manager
    @Test
    public void createEmployeeShouldReturnEmployeeWithoutManagerWhenSuccess() {
        // Given
        request.setManagerId(null); // request has no managerId
        employee.setManager(null); // employee has no manager

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeMapper.toEmployee(request, department, team, null))
                .thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        Employee emp = service.addEmployee(request);

        // Then
        assertNotNull(emp);
        assertEquals(employee.getId(), emp.getId());
        assertEquals(employee.getName(), emp.getName());
        assertNull(emp.getManager());
    }
}