package com.internship.unit.service;

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
import com.internship.service.EmployeeService;
import com.internship.service.ExpertiseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.internship.enums.Gender.MALE;
import static com.internship.exception.ApiError.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    private static final Long NON_EXISTENT_ID = -1L;
    private static final Long EMPLOYEE_ID = 1L;
    private static final Long MANAGER_ID = 10L;
    private static final Long EXPERTISE_ID1 = 1L;
    private static final Long EXPERTISE_ID2 = 2L;
    private static final String EMPTY_STRING = "";
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService service;
    @Mock
    private ExpertiseService expertiseService;
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
        request.setDepartmentId(NON_EXISTENT_ID); // there is no department with this id
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
        request.setTeamId(NON_EXISTENT_ID); // there is no team with this id
        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
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
        request.setManagerId(NON_EXISTENT_ID); // there is no employee with this id
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
        employee.setId(EMPLOYEE_ID);
        employee.setDepartment(department);
        employee.setTeam(team);
        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(EMPLOYEE_ID);
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
    public void testAddEmployeeWithExistingManager_shouldSucceedAndReturnEmployeeInfo() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L
        Employee manager = buildEmployee();
        manager.setId(MANAGER_ID);
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());
        request.setManagerId(manager.getId());
        Employee employee = buildEmployee();
        employee.setId(EMPLOYEE_ID);
        employee.setDepartment(department);
        employee.setTeam(team);
        employee.setManager(manager);
        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(EMPLOYEE_ID);
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
        assertEquals(response.getManagerId(), employeeResponse.getManagerId());
    }

    @Test
    public void testAddEmployeeWithExpertise_shouldSucceedAndReturnEmployeeInfo() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L
        Team team = buildTeam(); // create team with id = 1L
        Expertise expertise1 = buildExpertise("Java");
        expertise1.setId(EXPERTISE_ID1);
        Expertise expertise2 = buildExpertise("Spring boot");
        expertise2.setId(EXPERTISE_ID2);
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());
        request.setTeamId(team.getId());
        request.setExpertises(List.of(expertise1.getName(), expertise2.getName()));
        Employee employee = buildEmployee();
        employee.setId(EMPLOYEE_ID);
        employee.setDepartment(department);
        employee.setTeam(team);
        employee.setExpertises(List.of(expertise1, expertise2));
        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(EMPLOYEE_ID);
        employeeResponse.setDepartmentId(department.getId());
        employeeResponse.setTeamId(team.getId());
        employeeResponse.setExpertises(List.of(expertise1.getName(), expertise2.getName()));
        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(teamRepository.findById(request.getTeamId()))
                .thenReturn(Optional.of(team));
        when(expertiseService.getExpertises(request.getExpertises()))
                .thenReturn(List.of(expertise1, expertise2));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(mapper.toEmployee(request, department, team, null, List.of(expertise1, expertise2))).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(employeeResponse);
        // action
        EmployeeResponse response = service.addEmployee(request);
        // then
        assertNotNull(response);
        assertEquals(response.getExpertises(), employeeResponse.getExpertises());
    }
}
