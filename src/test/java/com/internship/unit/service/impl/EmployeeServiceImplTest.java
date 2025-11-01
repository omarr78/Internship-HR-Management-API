package com.internship.unit.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.service.impl.EmployeeServiceImpl;
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
public class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl service;

    @Mock
    private EmployeeMapper mapper;

    @Mock
    private DepartmentRepository departmentRepository;

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

    @Test
    public void testAddEmployeeWithNotFoundDepartment_shouldFail() {
        // Given
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(1L); // there is no department with this id

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.empty());

        // When & Then - should throw an DEPARTMENT_NOT_FOUND
        assertThrows(BusinessException.class, () -> service.addEmployee(request));
    }

    @Test
    public void testAddEmployeeWithExistingDepartment_shouldSucceedAndReturnEmployeeInfo() {
        // Given
        Department department = buildDepartment(); // create department with id = 1L

        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(department.getId());

        Employee employee = buildEmployee();
        employee.setId(1L);
        employee.setDepartment(department);

        EmployeeResponse employeeResponse = buildEmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setDepartmentId(department.getId());

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(mapper.toEmployee(request,department,null)).thenReturn(employee);
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
    }
}
