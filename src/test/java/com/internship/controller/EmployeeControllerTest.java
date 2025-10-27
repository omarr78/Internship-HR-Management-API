package com.internship.controller;

import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static com.internship.enums.Gender.MALE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeServiceImpl service;

    CreateEmployeeRequest request = CreateEmployeeRequest.builder()
            .name("Omar")
            .dateOfBirth(LocalDate.of(1999, 10, 5))
            .graduationDate(LocalDate.of(2020, 6, 5))
            .gender(MALE)
            .departmentId(1L)
            .teamId(1L)
            .managerId(2L)
            .salary(2000)
            .expertises(Arrays.asList("Java", "Spring boot"))
            .build();

    EmployeeResponse response = EmployeeResponse.builder()
            .id(1L)
            .name("Omar")
            .dateOfBirth(LocalDate.of(1999, 10, 5))
            .graduationDate(LocalDate.of(2020, 6, 5))
            .gender(MALE)
            .departmentId(1L)
            .teamId(1L)
            .managerId(2L)
            .salary(2000)
            .expertises(Arrays.asList("Java", "Spring boot"))
            .build();

    // Test: Create Employee
    @Test
    public void testCreateEmployee() throws Exception {

        // When addEmployee is called, return the employee
        when(service.addEmployee(any(CreateEmployeeRequest.class))).thenReturn(response);

        // Perform the POST request and assert the response
        mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    // Test: update Employee
    @Test
    public void testUpdateEmployeeShouldReturnEmployeeWhenSuccess() throws Exception {

        // When updateEmployee is called, return the employee
        when(service.updateEmployee(any(UpdateEmployeeRequest.class))).thenReturn(response);

        // Perform the PUT request and assert the response
        // there is an employee exists with this id and also all resources exists
        mockMvc.perform(put("/api/employees/1")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    // Test: update Employee and the id of employee not found
    @Test
    public void testUpdateEmployeeShouldReturnNotFoundWhenResourceNotFound() throws Exception {

        // When updateEmployee is called, return the employee
        when(service.updateEmployee(any(UpdateEmployeeRequest.class))).thenReturn(response);

        // Perform the PUT request and assert the response
        // there is no employee exists with this id or resources not exists
        mockMvc.perform(put("/api/employees/10")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}


