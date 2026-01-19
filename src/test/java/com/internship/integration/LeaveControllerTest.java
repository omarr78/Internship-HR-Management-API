package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.CreateLeaveRequest;
import com.internship.dto.CreateLeaveResponse;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.exception.ErrorCode;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class LeaveControllerTest {
    private static final Long NON_EXISTENT_ID = -1L;
    private static final Long EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID = 1L;
    private static final Long EMPLOYEE_WITH_21_DAYS_OF_VACATION_ID = 2L;
    private static final int STANDARD_LEAVE_DAYS = 21;
    private static final int EXTENDED_LEAVE_DAYS = 30;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRepository leaveRepository;

    private List<Leave> generateLeavesFrom2020(int leaveDays, Employee employee) {
        List<Leave> leaves = new ArrayList<>();
        LocalDate currentDate = LocalDate.of(2020, 1, 1);
        for (int i = 0; i < leaveDays; i++) {
            if (currentDate.getDayOfWeek() == DayOfWeek.FRIDAY
                    || currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                currentDate = currentDate.plusDays(1);
            }
            leaves.add(new Leave(currentDate, false, employee));
            currentDate = currentDate.plusDays(1);
        }
        return leaves;
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeavesRangeWith2Days_shouldSuccessAndReturnLeavesDetailsAndNoDeduction() throws Exception {
        // employee with id 1 will record a leave for two days
        // from (Wed) 1 jan 2020 to (Thu) 2 jan 2020
        // this employee has just 2 leave days so no deduction
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 1);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 2);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            MvcResult result = mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            CreateLeaveResponse leaveResponse1 = new CreateLeaveResponse(requestedLeavesStartDate,
                    false, EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID);
            CreateLeaveResponse leaveResponse2 = new CreateLeaveResponse(requestedLeavesEndDate,
                    false, EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID);

            List<CreateLeaveResponse> expectedLeaveResponse = List.of(leaveResponse1, leaveResponse2);

            List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaveResponse);

            Employee employee = employeeRepository.findById(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID).get();

            Leave leave1 = new Leave(requestedLeavesStartDate, false, employee); // 1 jan 2020
            Leave leave2 = new Leave(requestedLeavesEndDate, false, employee); // 2 jan 2020

            List<Leave> expectedLeaves = List.of(leave1, leave2);

            List<Leave> leaves = leaveRepository.findAll();

            // assertion on database
            Assertions.assertThat(leaves)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaves);
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAdd3Leaves1InWorkDayAnd2InWeekend_shouldAddLeavesExcludingWeekendsWithNoDeduction() throws Exception {
        // employee with id 1 will record a leave for three days including Fri, Sat
        // from (Thu) 2 jan 2020 to (Sat) 4 jan 2020
        // this is result record just Thu leave
        // this employee has just 3 leave days so no deduction
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 2);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 4);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID)
                .build();

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
                    new CreateLeaveResponse(requestedLeavesStartDate, false,
                            EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID); // 2 jan 2020

            List<CreateLeaveResponse> expectedLeaveResponse = List.of(thuLeaveResponse);

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaveResponse);

            Employee employee = employeeRepository.findById(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID).get();
            Leave thuLeave = new Leave(requestedLeavesStartDate, false, employee); // 2 jan 2020

            List<Leave> expectedLeaves = List.of(thuLeave);

            List<Leave> leaves = leaveRepository.findAll();

            // assertion on database
            Assertions.assertThat(leaves)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaves);
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAdd1LeaveForRecentlyJoinedEmployeeThatConsumedAllStandardLeaveDays_ShouldAddAndDeductLeave() throws Exception {
        // employee(recently joined employee) with id 2  will record a leave
        // from (Sun) 16 Feb 2020 to (Sun) 16 Feb 2020
        Employee employee = employeeRepository.findById(EMPLOYEE_WITH_21_DAYS_OF_VACATION_ID).get();
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 2, 16);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 2, 16);
        List<Leave> prevLeaves = generateLeavesFrom2020(STANDARD_LEAVE_DAYS, employee);
        leaveRepository.saveAll(prevLeaves);
        // now employee has just 21 leave days so no deduction
        // if employee adds another leave it will be deducted leave

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EMPLOYEE_WITH_21_DAYS_OF_VACATION_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 2, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            MvcResult result = mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            CreateLeaveResponse leaveResponse =
                    new CreateLeaveResponse(requestedLeavesStartDate, true,
                            EMPLOYEE_WITH_21_DAYS_OF_VACATION_ID);

            List<CreateLeaveResponse> expectedLeaveResponse = List.of(leaveResponse);

            List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaveResponse);

            Leave leave = new Leave(requestedLeavesStartDate, true, employee); // 16 Feb 2020

            List<Leave> expectedLeaves = List.of(leave);

            List<Leave> leaves = leaveRepository.findAll();

            // assertion on database
            Assertions.assertThat(leaves)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsAll(expectedLeaves);
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAdd1LeaveWhenLeaveLimitExceededForLongStandingEmployee_ShouldAddDeductedLeave() throws Exception {
        // employee(Long Standing Employee) with id 2  will record a leave
        // from (Sun) 16 Feb 2020 to (Sun) 16 Feb 2020
        Employee employee = employeeRepository.findById(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID).get();
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 2, 16);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 2, 16);
        List<Leave> prevLeaves = generateLeavesFrom2020(EXTENDED_LEAVE_DAYS, employee);
        leaveRepository.saveAll(prevLeaves);
        // now employee has just 30 leave days so no deduction
        // if employee adds another leave it will be deducted leave

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 2, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            MvcResult result = mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            CreateLeaveResponse leaveResponse =
                    new CreateLeaveResponse(requestedLeavesStartDate, true,
                            EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID);

            List<CreateLeaveResponse> expectedLeaveResponse = List.of(leaveResponse);

            List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .isEqualTo(expectedLeaveResponse);

            Leave leave = new Leave(requestedLeavesStartDate, true, employee); // 16 Feb 2020

            List<Leave> expectedLeaves = List.of(leave);

            List<Leave> leaves = leaveRepository.findAll();

            // assertion on database
            Assertions.assertThat(leaves)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsAll(expectedLeaves);
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
                    .andExpect(result -> {
                        String json = result.getResponse().getContentAsString();
                        ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                        assertEquals("Employee not found with id: " + NON_EXISTENT_ID, error.getErrorMessage());
                    });
        }
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testEmployeeAddsLeaveInTheSameDateTwice_shouldFailAndReturnIsConflict() throws Exception {
        LocalDate requestedLeavesStartDate = LocalDate.of(2020, 1, 1);
        LocalDate requestedLeavesEndDate = LocalDate.of(2020, 1, 1);

        // there is a leave saved in database for LONG_STANDING_EMPLOYEE_ID employee in 1 Jan 2020
        Employee employee = employeeRepository.findById(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID).get();
        leaveRepository.save(new Leave(requestedLeavesStartDate, false, employee));

        // we're trying to post leave in the same date
        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(requestedLeavesStartDate)
                .endDate(requestedLeavesEndDate)
                .employeeId(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(result -> {
                        String json = result.getResponse().getContentAsString();
                        ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                        assertEquals("This employee already has a leave recorded for the specified date",
                                error.getErrorMessage());
                    });
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
                .employeeId(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> {
                        String json = result.getResponse().getContentAsString();
                        ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                        assertEquals("start date must be before or equal to end date",
                                error.getErrorMessage());
                    });
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
                .employeeId(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 2, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> {
                        String json = result.getResponse().getContentAsString();
                        ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                        assertEquals("start date must be in the same current month",
                                error.getErrorMessage());
                    });
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
                .employeeId(EMPLOYEE_WITH_30_DAYS_OF_VACATION_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/leave")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> {
                        String json = result.getResponse().getContentAsString();
                        ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                        assertEquals("end date must be in the same current year",
                                error.getErrorMessage());
                    });
        }
    }
}
