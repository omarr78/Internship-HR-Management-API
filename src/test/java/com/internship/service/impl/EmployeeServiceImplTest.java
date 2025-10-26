package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.CreateEmployeeResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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
    private ExpertiseRepository expertiseRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl service;

    @Test
    public void createEmployeeShouldReturnEmployeeWhenSuccess() {
        // Given an employee to create
        Department department = Department.builder()
                .id(1L)
                .name("Department 1").build();

        Team team = Team.builder()
                .id(1L)
                .name("Team 1").build();

        Employee manager = Employee.builder()
                .id(2L).build();

        Expertise expertise1 = Expertise.builder()
                .name("Java")
                .build();

        Expertise expertise2 = Expertise.builder()
                .name("Spring Boot")
                .build();

        Employee employee = Employee.builder()
                .id(1L)
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .department(department)
                .team(team)
                .manager(manager)
                .salary(2000)
                .expertises(List.of(expertise1, expertise2))
                .build();

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .managerId(2L)
                .salary(2000)
                .expertises(List.of("Java","Spring Boot"))
                .build();

        CreateEmployeeResponse response = CreateEmployeeResponse.builder()
                .id(1L)
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .managerId(2L)
                .salary(2000)
                .expertises(List.of("Java","Spring Boot"))
                .build();

        // Given
        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.of(manager));
        when(expertiseRepository.findExpertiseByName(any(String.class)))
                .thenReturn(Optional.of(expertise1))
                .thenReturn(Optional.of(expertise2));

        when(employeeMapper.toEmployee(request, department, team, manager,
                Arrays.asList(expertise1, expertise2))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);


        when(employeeMapper.toResponse(employee)).thenReturn(response);

        // When
        CreateEmployeeResponse res = service.addEmployee(request);

        // Then
        assertNotNull(res);
        assertEquals(res.getId(), employee.getId());
        assertEquals(res.getName(), employee.getName());
        assertEquals(res.getDateOfBirth(), employee.getDateOfBirth());
        assertEquals(res.getGraduationDate(), employee.getGraduationDate());
        assertEquals(res.getGender(), employee.getGender());
        assertEquals(res.getDepartmentId(), employee.getDepartment().getId());
        assertEquals(res.getTeamId(), employee.getTeam().getId());
        assertEquals(res.getManagerId(), employee.getManager().getId());
        assertEquals(res.getSalary(), employee.getSalary());
    }

    @Test
    public void createEmployeeShouldReturnDepartmentNotFoundWhenDepartmentNotFound() {
        CreateEmployeeRequest request;
        request = CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .managerId(2L)
                .salary(2000)
                .expertises(List.of("Java","Spring Boot"))
                .build();

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
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .managerId(2L)
                .salary(2000)
                .expertises(List.of("Java","Spring Boot"))
                .build();

        Department department = Department.builder()
                .id(1L)
                .name("Department 1").build();

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

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .managerId(2L)
                .salary(2000)
                .expertises(List.of("Java","Spring Boot"))
                .build();

        Department department = Department.builder()
                .id(1L)
                .name("Department 1").build();

        Team team = Team.builder()
                .id(1L)
                .name("Team 1").build();


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
        Department department = Department.builder()
                .id(1L)
                .name("Department 1").build();

        Team team = Team.builder()
                .id(1L)
                .name("Team 1").build();

        Expertise expertise1 = Expertise.builder()
                .name("Java")
                .build();

        Expertise expertise2 = Expertise.builder()
                .name("Spring Boot")
                .build();

        Employee employee = Employee.builder()
                .id(1L)
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .department(department)
                .team(team)
                .salary(2000)
                .expertises(List.of(expertise1, expertise2))
                .build();

        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .salary(2000)
                .expertises(List.of("Java","Spring Boot"))
                .build();

        CreateEmployeeResponse response = CreateEmployeeResponse.builder()
                .id(1L)
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(1L)
                .teamId(1L)
                .salary(2000)
                .expertises(List.of("Java","Spring Boot"))
                .build();

        request.setManagerId(null); // request has no managerId
        employee.setManager(null); // employee has no manager

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));

        when(expertiseRepository.findExpertiseByName(any(String.class)))
                .thenReturn(Optional.of(expertise1))
                .thenReturn(Optional.of(expertise2));

        when(employeeMapper.toEmployee(request, department, team, null,
                Arrays.asList(expertise1, expertise2))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        // When
        CreateEmployeeResponse res = service.addEmployee(request);

        // Then
        assertNotNull(res);
        assertEquals(res.getId(), employee.getId());
        assertEquals(res.getName(), employee.getName());
        assertNull(res.getManagerId());
    }
}