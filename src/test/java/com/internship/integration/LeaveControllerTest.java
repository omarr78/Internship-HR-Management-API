package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.CreateLeaveRequest;
import com.internship.dto.CreateLeaveResponse;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.ExpertiseRepository;
import com.internship.repository.LeaveRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class LeaveControllerTest {
    private static final Long EXISTENT_EMPLOYEE1_ID = 1L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExpertiseRepository expertiseRepository;
    @Autowired
    private LeaveRepository leaveRepository;

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeave_shouldSuccessAndReturnLeaveDetails() throws Exception {
        // employee with id 1 will record a leave for two days
        // from (Wed) 14 jan 2026 to (Thu) 15 jan 2026
        // this employee has just 2 leave days so no deduction
        LocalDate from = LocalDate.of(2026, 1, 14);
        LocalDate to = LocalDate.of(2026, 1, 15);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(from)
                .endDate(to)
                .build();

        MvcResult result = mockMvc.perform(post("/api/leave")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Employee employee1 = employeeRepository.findById(EXISTENT_EMPLOYEE1_ID).get();

        Leave leave1 = Leave.builder()
                .employee(employee1)
                .leaveDate(from) // 14 jan 2026
                .build();

        Leave leave2 = Leave.builder()
                .employee(employee1)
                .leaveDate(to) // 15 jan 2026
                .build();

        List<Leave> expectedLeaves = List.of(leave1, leave2);
        List<Leave> leaves = leaveRepository.findAll();

        CreateLeaveResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), CreateLeaveResponse.class);

        // assertion against response
        Assertions.assertThat(response.getLeaves())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsAll(expectedLeaves);

        // assertion against database
        Assertions.assertThat(leaves)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsAll(expectedLeaves);
    }

}
