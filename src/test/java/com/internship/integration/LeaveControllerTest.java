package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.CreateLeaveRequest;
import com.internship.dto.CreateLeaveResponse;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.LeaveRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class LeaveControllerTest {
    private static final Long NON_EXISTENT_ID = -1L;
    private static final Long EXISTENT_EMPLOYEE_ID = 1L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRepository leaveRepository;

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeavesRangeWith2Days_shouldSuccessAndReturnLeavesDetails() throws Exception {
        // employee with id 1 will record a leave for two days
        // from (Wed) 1 jan 2020 to (Thu) 2 jan 2020
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 1);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 2);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();
        
        // capture database state before the api call
        List<Leave> leavesBefore = leaveRepository.findAll();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            MvcResult result = mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            CreateLeaveResponse leaveResponse1 = new CreateLeaveResponse(requestedLeavesStartDate,
                    EXISTENT_EMPLOYEE_ID);
            CreateLeaveResponse leaveResponse2 = new CreateLeaveResponse(requestedLeavesEndDate,
                    EXISTENT_EMPLOYEE_ID);

            List<CreateLeaveResponse> expectedLeaveResponse = List.of(leaveResponse1, leaveResponse2);

            List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaveResponse);

            Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE_ID).get();

            Leave leave1 = new Leave(requestedLeavesStartDate, employee); // 1 jan 2020
            Leave leave2 = new Leave(requestedLeavesEndDate, employee); // 2 jan 2020

            List<Leave> expectedLeaves = List.of(leave1, leave2);
            // capture database state after the api call
            List<Leave> leavesAfter = leaveRepository.findAll();

            // assertion on database
            Assertions.assertThat(leavesAfter)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsAll(leavesBefore)
                    .containsAll(expectedLeaves)
                    .hasSize(leavesBefore.size() + expectedLeaves.size());
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAdd3Leaves1InWorkDayAnd2InWeekend_shouldAddLeavesExcludingWeekends() throws Exception {
        // employee with id 1 will record a leave for three days including Fri, Sat
        // from (Thu) 2 jan 2020 to (Sat) 4 jan 2020
        // this is result record just Thu leave
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 2);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 4);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        // capture database state before the api call
        List<Leave> leavesBefore = leaveRepository.findAll();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            MvcResult result = mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

            CreateLeaveResponse thuLeaveResponse =
                    new CreateLeaveResponse(requestedLeavesStartDate, EXISTENT_EMPLOYEE_ID); // 2 jan 2020

            List<CreateLeaveResponse> expectedLeaveResponse = List.of(thuLeaveResponse);

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaveResponse);

            Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE_ID).get();
            Leave thuLeave = new Leave(requestedLeavesStartDate, employee); // 2 jan 2020

            List<Leave> expectedLeaves = List.of(thuLeave);
            // capture database state after the api call
            List<Leave> leavesAfter = leaveRepository.findAll();

            // assertion on database
            Assertions.assertThat(leavesAfter)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsAll(leavesBefore)
                    .containsAll(expectedLeaves)
                    .hasSize(leavesBefore.size() + expectedLeaves.size());
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWithNotFoundEmployee_shouldFailAndReturnEmployeeNotFound() throws Exception {
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 1);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 2);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(NON_EXISTENT_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("Employee not found with id: " + NON_EXISTENT_ID)));
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testEmployeeAddsLeaveInTheSameDateTwice_shouldFailAndReturnIsConflict() throws Exception {
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 1);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 1);

        // there is a leave saved in database for LONG_STANDING_EMPLOYEE_ID employee in 1 Jan 2020
        Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE_ID).get();
        leaveRepository.save(new Leave(requestedLeavesStartDate, employee));

        // we're trying to post leave in the same date
        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("This employee already has a leave recorded for the specified date")));
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWithStartDateAfterEndDate_shouldFailsAndReturnBadRequest() throws Exception {
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 2);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 1);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("start date must be before or equal to end date")));
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWithStartDateInThePreviousMonth_shouldFailAndReturnBadRequest() throws Exception {
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 1);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 1);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 2, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("start date must be at least in the same current month")));
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWithEndDateNotInCurrentYear_shouldFailsAndReturnBadRequest() throws Exception {
        LocalDate requestedLeavesStartDate = LocalDate.of(2021, 1, 1);
        LocalDate requestedLeavesEndDate = LocalDate.of(2021, 1, 1);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("end date must be in the same current year")));
        }
    }
}
