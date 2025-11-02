package com.internship.unit.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
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
import com.internship.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.internship.enums.Gender.MALE;
import static com.internship.exception.ApiError.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImplTest.class);
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

    @Mock
    private ExpertiseRepository expertiseRepository;

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

    private Expertise buildExpertise(String expertiseName) {
        return Expertise.builder()
                .name(expertiseName)
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
        when(mapper.toEmployee(request, department, team, null, List.of())).thenReturn(employee);
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
        when(mapper.toEmployee(request, department, team, manager, List.of())).thenReturn(employee);
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

    @Test
    public void testAddEmployeeWithExistingDepartmentAndTeamAndManagerAndExistingExpertise_shouldSucceedAndReturnEmployeeInfo() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L
        Employee manager = buildEmployee();
        manager.setId(10L);
        Expertise expertise1 = buildExpertise("Java");
        expertise1.setId(1L);
        Expertise expertise2 = buildExpertise("Spring boot");
        expertise2.setId(2L);

        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());
        request.setManagerId(manager.getId());
        request.setExpertises(List.of(expertise1.getName(), expertise2.getName()));

        Employee employee = buildEmployee();
        employee.setId(1L);
        employee.setDepartment(department);
        employee.setTeam(team);
        employee.setManager(manager);
        employee.setExpertises(List.of(expertise1, expertise2));

        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setDepartmentId(department.getId());
        employeeResponse.setTeamId(team.getId());
        employeeResponse.setManagerId(manager.getId());
        employeeResponse.setExpertises(List.of(expertise1.getName(), expertise2.getName()));

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.findById(request.getManagerId()))
                .thenReturn(Optional.of(manager));
        when(expertiseRepository.findExpertiseByName(any(String.class)))
                .thenReturn(Optional.of(expertise1))
                .thenReturn(Optional.of(expertise2));

        lenient().when(expertiseRepository.save(any(Expertise.class)))
                .thenReturn(expertise1)
                .thenReturn(expertise2);


        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);


        when(mapper.toEmployee(request, department, team, manager, List.of(expertise1, expertise2))).thenReturn(employee);
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
        assertEquals(response.getExpertises(), employeeResponse.getExpertises());
    }

    @Test
    public void testAddEmployeeWithExistingDepartmentAndTeamAndNotExistingExpertise_shouldSucceedAndReturnEmployeeInfo() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L
        Expertise expertise1 = buildExpertise("Java");
        expertise1.setId(1L);
        Expertise expertise2 = buildExpertise("Spring boot");
        expertise2.setId(2L);


        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());
        request.setExpertises(List.of("Java", "Spring boot"));

        Employee employee = buildEmployee();
        employee.setId(1L);
        employee.setDepartment(department);
        employee.setTeam(team);
        employee.setExpertises(List.of(expertise1, expertise2));

        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setDepartmentId(department.getId());
        employeeResponse.setTeamId(team.getId());
        employeeResponse.setExpertises(List.of("Java", "Spring boot"));

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(expertiseRepository.findExpertiseByName(any(String.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        lenient().when(expertiseRepository.save(any(Expertise.class)))
                .thenReturn(expertise1)
                .thenReturn(expertise2);

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);


        when(mapper.toEmployee(request, department, team, null, List.of(expertise1, expertise2))).thenReturn(employee);
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
        assertEquals(response.getExpertises(), employeeResponse.getExpertises());
    }

    @Test
    public void testAddEmployeeWithExistingDepartmentAndTeamWithEmptyExpertiseName_shouldSucceedAndReturnEmployeeInfoWithoutExpertise() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L

        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());
        request.setExpertises(List.of("", "")); // empty expertise

        Employee employee = buildEmployee();
        employee.setId(1L);
        employee.setDepartment(department);
        employee.setTeam(team);

        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setDepartmentId(department.getId());
        employeeResponse.setTeamId(team.getId());
        employeeResponse.setExpertises(List.of()); // empty expertise

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(mapper.toEmployee(request, department, team, null, List.of())).thenReturn(employee);
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
        assertEquals(response.getExpertises(), employeeResponse.getExpertises());
    }
}
