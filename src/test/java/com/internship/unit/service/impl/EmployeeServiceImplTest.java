package com.internship.unit.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Employee;
import com.internship.repository.EmployeeRepository;
import com.internship.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

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

    private CreateEmployeeRequest buildCreateEmployeeRequest() {
        return CreateEmployeeRequest.builder()
                .name("Omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .salary(2000)
                .build();
    }

    @Test
    public void testCreateEmployee_ShouldReturnEmployee() {
        // Given
        CreateEmployeeRequest request = buildCreateEmployeeRequest();

        EmployeeResponse employeeResponse = EmployeeResponse.builder()
                .id(1L)
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .graduationDate(request.getGraduationDate())
                .build();

        Employee employee = Employee.builder()
                .id(1L)
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .graduationDate(request.getGraduationDate())
                .build();

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
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
    }
}
