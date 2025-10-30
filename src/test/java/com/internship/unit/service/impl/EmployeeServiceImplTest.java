package com.internship.unit.service.impl;

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

    private Employee buildEmployee() {
        return Employee.builder()
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
        Employee employeeRequest = buildEmployee();
        Employee employeeResponse = buildEmployee();
        employeeResponse.setId(1L);

        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeResponse);
        // action
        Employee response = service.addEmployee(employeeRequest);
        // then
        assertNotNull(response);
        assertEquals(employeeResponse.getId(), response.getId());
        assertEquals(employeeResponse.getName(), response.getName());
        assertEquals(employeeResponse.getDateOfBirth(), response.getDateOfBirth());
        assertEquals(employeeResponse.getGraduationDate(), response.getGraduationDate());
        assertEquals(employeeResponse.getGender(), response.getGender());
        assertEquals(employeeResponse.getSalary(), response.getSalary());
    }
}
