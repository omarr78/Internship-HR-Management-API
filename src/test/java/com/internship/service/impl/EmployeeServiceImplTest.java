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

    private CreateEmployeeRequest buildCreateRequest(Long departmentId, Long teamId, Long managerId) {
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

    private UpdateEmployeeRequest buildUpdateRequest(Long departmentId, Long teamId, Long managerId) {
        return UpdateEmployeeRequest.builder()
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

    private EmployeeResponse buildResponse(Employee employee) {
        return EmployeeResponse.builder()
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

    // Test: Create Employee
    @Test
    public void createEmployeeShouldReturnEmployeeWhenSuccess() {
        // Given an employee to create
        CreateEmployeeRequest request = buildCreateRequest(department.getId(), team.getId(), manager.getId());
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

        EmployeeResponse response = buildResponse(employee);

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
        EmployeeResponse res = service.addEmployee(request);

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

    // Test: Create Employee
    @Test
    public void createEmployeeShouldReturnDepartmentNotFoundWhenDepartmentNotFound() {
        // Given
        //  there is no department with this id = 10
        CreateEmployeeRequest request = buildCreateRequest(10L, team.getId(), manager.getId());

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an DEPARTMENT_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    // Test: Create Employee
    @Test
    public void createEmployeeShouldReturnTeamNotFoundWhenTeamNotFound() {
        // Given
        // there is no team with this id = 10
        CreateEmployeeRequest request = buildCreateRequest(department.getId(), 10L, manager.getId());

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw a TEAM_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    // Test: Create Employee
    @Test
    public void createEmployeeShouldReturnEmployeeNotFoundWhenManagerNotFound() {
        // Given
        // there is no employee with this id = 10
        CreateEmployeeRequest request = buildCreateRequest(department.getId(), team.getId(), 10L);

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an EMPLOYEE_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    // Test: Create Employee
    // when manager id is null, that means that employee not have manager
    @Test
    public void createEmployeeShouldReturnEmployeeWithoutManagerWhenSuccess() {
        // Given
        // request has no managerId
        // employee has no manager
        CreateEmployeeRequest request = buildCreateRequest(department.getId(), team.getId(), null);

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

        EmployeeResponse response = buildResponse(employee);

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
        EmployeeResponse res = service.addEmployee(request);

        // Then
        assertNotNull(res);
        assertEquals(res.getId(), employee.getId());
        assertEquals(res.getName(), employee.getName());
        assertNull(res.getManagerId());
    }

    // Test: update Employee
    @Test
    public void UpdateEmployeeShouldReturnEmployeeWhenSuccess() {
        // Given an employee to create
        Department department1 = Department.builder()
                .id(2L)
                .name("Department 2")
                .build();
        Team team1 = Team.builder()
                .id(2L)
                .name("Team 2")
                .build();

        Long id = 1L;
        UpdateEmployeeRequest request = buildUpdateRequest(department.getId(), team.getId(), manager.getId());

        // in a request we update name,date of birth, graduation date, salary, team, department
        request.setName("Ahmed");
        request.setDateOfBirth(LocalDate.of(2001, 12, 26));
        request.setGraduationDate(LocalDate.of(2023, 6, 5));
        request.setSalary(3000);
        request.setDepartmentId(department1.getId());
        request.setTeamId(team1.getId());

        // not updated employee
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

        // updated employee
        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .name("Ahmed")
                .dateOfBirth(LocalDate.of(2001, 12, 26))
                .graduationDate(LocalDate.of(2023, 6, 5))
                .gender(MALE)
                .department(department1)
                .team(team1)
                .manager(manager)
                .salary(3000)
                .expertises(expertises)
                .build();

        EmployeeResponse response = buildResponse(updatedEmployee);

        // Given
        when(employeeRepository.findById(id))
                .thenReturn(Optional.of(employee));
        when(departmentRepository.findById(2L))
                .thenReturn(Optional.of(department1));
        when(teamRepository.findById(2L))
                .thenReturn(Optional.of(team1));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.of(manager));
        when(expertiseRepository.findExpertiseByName(any(String.class)))
                .thenReturn(Optional.of(expertise1))
                .thenReturn(Optional.of(expertise2));

        when(employeeMapper.toResponse(employee)).thenReturn(response);

        // When
        EmployeeResponse res = service.modifyEmployee(request, 1L);

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

    // Test: update Employee
    @Test
    public void updateEmployeeShouldReturnEmployeeNotFoundWhenEmployeeWithGivenIdNotFound() {
        // Given
        UpdateEmployeeRequest request = buildUpdateRequest(department.getId(), team.getId(), manager.getId());

        // When & Then - should throw an EMPLOYEE_NOT_FOUND
        // there is no employee with id = 10
        when(employeeRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.modifyEmployee(request, 10L));
    }

    // Test: update Employee
    @Test
    public void updateEmployeeShouldReturnDepartmentNotFoundWhenDepartmentNotFound() {
        // Given
        // there is no department with this id = 10
        UpdateEmployeeRequest request = buildUpdateRequest(10L, team.getId(), manager.getId());

        // not updated employee
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

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an DEPARTMENT_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.modifyEmployee(request, 1L));
    }

    // Test: update Employee
    @Test
    public void updateEmployeeShouldReturnTeamNotFoundWhenTeamNotFound() {
        // Given
        // there is no team with this id = 10
        UpdateEmployeeRequest request = buildUpdateRequest(department.getId(), 10L, manager.getId());

        // not updated employee
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

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw a TEAM_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.modifyEmployee(request, 1L));
    }

    // Test: update Employee
    @Test
    public void updateEmployeeShouldReturnEmployeeNotFoundWhenManagerNotFound() {
        // Given
        // there is no employee with this id = 10
        UpdateEmployeeRequest request = buildUpdateRequest(department.getId(), team.getId(), 10L);

        // not updated employee
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

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an EMPLOYEE_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.modifyEmployee(request, 1L));
    }

    // Test: update Employee
    // when employee has self management
    @Test
    public void updateEmployeeShouldReturnEmployeeWithoutManagerWhenSuccess() {
        // Given
        UpdateEmployeeRequest request = buildUpdateRequest(department.getId(), team.getId(), manager.getId());
        // given the same id that employee has
        request.setManagerId(1L);
        // not updated employee
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

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        // When & Then - should throw a SELF_MANAGEMENT exception
        assertThrows(BusinessException.class, () -> service.modifyEmployee(request, 1L));
    }

    // Test: Delete Employee
    @Test
    public void deleteEmployeeShouldReturnEmployeeNotFoundExceptionWhenEmployeeNotFound() {
        // Given
        Long employeeId = 1L;

        // When
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());
        // Then
        assertThrows(BusinessException.class, () -> service.removeEmployee(employeeId));
    }

    // Test: Delete employee and employee has no subordinates then it will delete it directly
    @Test
    public void deleteEmployeeShouldDeleteItDirectlyWhenEmployeeHasNoSubordinates() {
        // Given
        Long employeeId = 1L;

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
                .subordinates(List.of())
                .expertises(expertises)
                .build();

        // When
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));
        // Then
        service.removeEmployee(employeeId);
    }

    // Test: Delete employee and employee has subordinates and has no manager
    @Test
    public void deleteEmployeeShouldReturnInvalidEmployeeRemovalWhenManagerHasNoManager() {
        // Given
        // mangerEmployee --> {subordinateEmployee1,subordinateEmployee2}
        Long subordinateEmployeeId1 = 1L;
        Long subordinateEmployeeId2 = 2L;
        Long mangerEmployeeId = 3L;

        Employee mangerEmployee = Employee.builder()
                .id(mangerEmployeeId)
                .name("Omar")
                .manager(null) // has no manager
                .build();

        Employee subordinateEmployee1 = Employee.builder()
                .id(subordinateEmployeeId1)
                .name("Ahmed")
                .manager(mangerEmployee)
                .build();

        Employee subordinateEmployee2 = Employee.builder()
                .id(subordinateEmployeeId2)
                .name("Mahmoud")
                .manager(mangerEmployee)
                .build();

        // set two employee as Subordinates to manager
        mangerEmployee.setSubordinates(List.of(subordinateEmployee1, subordinateEmployee2));

        when(employeeRepository.findById(mangerEmployeeId))
                .thenReturn(Optional.of(mangerEmployee));

        // When & Then
        assertThrows(BusinessException.class, () -> service.removeEmployee(mangerEmployeeId));
    }

    // Test: Delete employee and employee has subordinates and has manager
    @Test
    public void deleteManagerEmployeeShouldMoveAllSubordinateToHisManagerThenDelete() {
        // Given
        // managerOfManagerEmployee -->  mangerEmployee --> {subordinateEmployee1,subordinateEmployee2}
        Long subordinateEmployeeId1 = 1L;
        Long subordinateEmployeeId2 = 2L;
        Long mangerEmployeeId = 3L;
        Long managerOfManagerEmployeeId = 4L;

        Employee managerOfManagerEmployee = Employee.builder()
                .id(managerOfManagerEmployeeId)
                .name("Omar")
                .build();

        Employee mangerEmployee = Employee.builder()
                .id(mangerEmployeeId)
                .name("Mostafa")
                .manager(managerOfManagerEmployee) // manager has manager
                .build();

        Employee subordinateEmployee1 = Employee.builder()
                .id(subordinateEmployeeId1)
                .name("Ahmed")
                .manager(mangerEmployee)
                .build();

        Employee subordinateEmployee2 = Employee.builder()
                .id(subordinateEmployeeId2)
                .name("Mahmoud")
                .manager(mangerEmployee)
                .build();

        managerOfManagerEmployee.setSubordinates(List.of(mangerEmployee));
        mangerEmployee.setSubordinates(List.of(subordinateEmployee1, subordinateEmployee2));

        when(employeeRepository.findById(mangerEmployeeId))
                .thenReturn(Optional.of(mangerEmployee));
        // When
        service.removeEmployee(mangerEmployeeId);

        // Then
        assertEquals(subordinateEmployee1.getManager(), managerOfManagerEmployee);
        assertEquals(subordinateEmployee2.getManager(), managerOfManagerEmployee);
    }

    // Test: Get employee and should success and return employee info
    @Test
    public void getEmployeeShouldReturnEmployeeInfoWhenSuccess() {
        // Given
        Long employeeId = 1L;

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

        EmployeeResponse response = buildResponse(employee);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);
        // when
        EmployeeResponse res = service.getEmployee(employeeId);
        // then

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

    // Test: Get Employee
    @Test
    public void getEmployeeShouldReturnEmployeeNotFoundExceptionWhenEmployeeNotFound() {
        // Given
        Long employeeId = 1L;

        // When
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());
        // Then
        assertThrows(BusinessException.class, () -> service.getEmployee(employeeId));
    }
}