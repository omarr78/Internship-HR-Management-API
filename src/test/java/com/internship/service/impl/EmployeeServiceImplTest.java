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
import org.junit.jupiter.api.BeforeEach;
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

    private Department department;
    private Team team;
    private Employee manager;
    private List<String> expertiseNames;
    private List<Expertise> expertises;
    private Expertise expertise1;
    private Expertise expertise2;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Department 1")
                .build();
        team = Team.builder()
                .id(1L)
                .name("Team 1")
                .build();
        manager = Employee.builder()
                .id(2L)
                .name("Manager")
                .build();
        expertiseNames = List.of("Java", "Spring Boot");
        expertise1 = Expertise.builder().name("Java").build();
        expertise2 = Expertise.builder().name("Spring Boot").build();
        expertises = List.of(expertise1, expertise2);
    }

    private CreateEmployeeRequest buildRequest(Long departmentId, Long teamId, Long managerId) {
        return CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2020, 6, 5))
                .gender(MALE)
                .departmentId(departmentId)
                .teamId(teamId)
                .managerId(managerId)
                .salary(2000)
                .expertises(expertiseNames)
                .build();
    }

    private CreateEmployeeResponse buildResponse(Employee employee) {
        return CreateEmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .dateOfBirth(employee.getDateOfBirth())
                .graduationDate(employee.getGraduationDate())
                .gender(employee.getGender())
                .departmentId(employee.getDepartment().getId())
                .teamId(employee.getTeam().getId())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .salary(employee.getSalary())
                .expertises(expertiseNames)
                .build();
    }

    @Test
    public void createEmployeeShouldReturnEmployeeWhenSuccess() {
        // Given an employee to create
        CreateEmployeeRequest request = buildRequest(department.getId(), team.getId(), manager.getId());
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
                .expertises(expertises)
                .build();

        CreateEmployeeResponse response = buildResponse(employee);

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
        // Given
        //  there is no department with this id = 10
        CreateEmployeeRequest request = buildRequest(10L, team.getId(), manager.getId());

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an DEPARTMENT_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    @Test
    public void createEmployeeShouldReturnTeamNotFoundWhenTeamNotFound() {
        // Given
        // there is no team with this id = 10
        CreateEmployeeRequest request = buildRequest(department.getId(), 10L, manager.getId());

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
        // there is no employee with this id = 10
        CreateEmployeeRequest request = buildRequest(department.getId(), team.getId(), 10L);

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
        // request has no managerId
        // employee has no manager
        CreateEmployeeRequest request = buildRequest(department.getId(), team.getId(), null);

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

        CreateEmployeeResponse response = buildResponse(employee);

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