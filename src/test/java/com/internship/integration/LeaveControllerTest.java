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
    private static final Long LONG_STANDING_EMPLOYEE_ID = 1L;
    private static final Long RECENTLY_JOINED_EMPLOYEE_ID = 2L;
    private static final int STANDARD_LEAVE_DAYS = 21;
    private static final int EXTENDED_LEAVE_DAYS = 30;

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

    private Leave buildLeave(LocalDate leaveDate, Employee employee, Boolean salaryDeducted) {
        return Leave.builder()
                .leaveDate(leaveDate)
                .salaryDeducted(salaryDeducted)
                .employee(employee)
                .build();
    }

    private CreateLeaveResponse buildLeaveResponse(LocalDate leaveDate, Long employeeId, Boolean salaryDeducted) {
        return CreateLeaveResponse.builder()
                .leaveDate(leaveDate)
                .salaryDeducted(salaryDeducted)
                .employeeId(employeeId)
                .build();
    }

    private List<Leave> addLeave(int leaveDays, Employee employee) {
        List<Leave> leaves = new ArrayList<>();
        LocalDate currentDate = LocalDate.of(2020, 1, 1);
        for (int i = 0; i < leaveDays; i++) {
            if (currentDate.getDayOfWeek() == DayOfWeek.FRIDAY
                    || currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                currentDate = currentDate.plusDays(1);
            }
            leaves.add(buildLeave(currentDate, employee, false));
            currentDate = currentDate.plusDays(1);
        }
        return leaves;
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeave_shouldSuccessAndReturnLeaveDetails() throws Exception {
        // employee with id 1 will record a leave for two days
        // from (Wed) 1 jan 2020 to (Thu) 2 jan 2020
        // this employee has just 2 leave days so no deduction
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 1, 2);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(from)
                .endDate(to)
                .employeeId(LONG_STANDING_EMPLOYEE_ID)
                .build();

        MvcResult result = mockMvc.perform(post("/api/leave")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Employee employee1 = employeeRepository.findById(LONG_STANDING_EMPLOYEE_ID).get();
        Leave leave1 = buildLeave(from, employee1, false); // 1 jan 2020
        Leave leave2 = buildLeave(to, employee1, false); // 2 jan 2020

        CreateLeaveResponse leaveResponse1 = buildLeaveResponse(from, employee1.getId(), false);
        CreateLeaveResponse leaveResponse2 = buildLeaveResponse(to, employee1.getId(), false);

        List<Leave> expectedLeaves = List.of(leave1, leave2);
        List<CreateLeaveResponse> expectedLeaveResponse = List.of(leaveResponse1, leaveResponse2);

        List<Leave> leaves = leaveRepository.findAll();

        List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

        // assertion on response
        Assertions.assertThat(response)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(expectedLeaveResponse);

        // assertion on database
        Assertions.assertThat(leaves)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(expectedLeaves);
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWithWeekends_shouldAddLeaveExcludingWeekends() throws Exception {
        // employee with id 1 will record a leave for three days including Fri, Sat
        // from (Thu) 2 jan 2020 to (Sat) 4 jan 2020
        // this is result record just Thu leave
        // this employee has just 3 leave days so no deduction
        LocalDate from = LocalDate.of(2020, 1, 2);
        LocalDate to = LocalDate.of(2020, 1, 4);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(from)
                .endDate(to)
                .employeeId(LONG_STANDING_EMPLOYEE_ID)
                .build();

        MvcResult result = mockMvc.perform(post("/api/leave")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Employee employee1 = employeeRepository.findById(LONG_STANDING_EMPLOYEE_ID).get();
        Leave thuLeave = buildLeave(from, employee1, false); // 2 jan 2020

        CreateLeaveResponse thuLeaveResponse =
                buildLeaveResponse(from, employee1.getId(), false); // 2 jan 2020

        List<Leave> expectedLeaves = List.of(thuLeave);
        List<CreateLeaveResponse> expectedLeaveResponse = List.of(thuLeaveResponse);

        List<Leave> leaves = leaveRepository.findAll();

        List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

        // assertion on response
        Assertions.assertThat(response)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(expectedLeaveResponse);

        // assertion on database
        Assertions.assertThat(leaves)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(expectedLeaves);
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWhenLeaveLimitExceededForRecentlyJoinedEmployee_ShouldAddDeductedLeave() throws Exception {
        // employee(recently joined employee) with id 2  will record a leave
        // from (Sun) 16 Feb 2020 to (Sun) 16 Feb 2020
        Employee employee = employeeRepository.findById(RECENTLY_JOINED_EMPLOYEE_ID).get();
        LocalDate from = LocalDate.of(2020, 2, 16);
        LocalDate to = LocalDate.of(2020, 2, 16);
        List<Leave> prevLeaves = addLeave(STANDARD_LEAVE_DAYS, employee);
        leaveRepository.saveAll(prevLeaves);
        // now employee has just 21 leave days so no deduction
        // if employee adds another leave it will be deducted leave

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(from)
                .endDate(to)
                .employeeId(RECENTLY_JOINED_EMPLOYEE_ID)
                .build();

        MvcResult result = mockMvc.perform(post("/api/leave")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Leave leave = buildLeave(from, employee, true); // 16 Feb 2020

        CreateLeaveResponse leaveResponse = buildLeaveResponse(from, employee.getId(), true);

        List<Leave> expectedLeaves = List.of(leave);
        List<CreateLeaveResponse> expectedLeaveResponse = List.of(leaveResponse);

        List<Leave> leaves = leaveRepository.findAll();

        List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

        // assertion on response
        Assertions.assertThat(response)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(expectedLeaveResponse);

        // assertion on database
        Assertions.assertThat(leaves)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsAll(expectedLeaves);
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWhenLeaveLimitExceededForLongStandingEmployee_ShouldAddDeductedLeave() throws Exception {
        // employee(Long Standing Employee) with id 2  will record a leave
        // from (Sun) 16 Feb 2020 to (Sun) 16 Feb 2020
        Employee employee = employeeRepository.findById(LONG_STANDING_EMPLOYEE_ID).get();
        LocalDate from = LocalDate.of(2020, 2, 16);
        LocalDate to = LocalDate.of(2020, 2, 16);
        List<Leave> prevLeaves = addLeave(EXTENDED_LEAVE_DAYS, employee);
        leaveRepository.saveAll(prevLeaves);
        // now employee has just 30 leave days so no deduction
        // if employee adds another leave it will be deducted leave

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(from)
                .endDate(to)
                .employeeId(LONG_STANDING_EMPLOYEE_ID)
                .build();

        MvcResult result = mockMvc.perform(post("/api/leave")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Leave leave = buildLeave(from, employee, true); // 16 Feb 2020

        CreateLeaveResponse leaveResponse = buildLeaveResponse(from, employee.getId(), true);

        List<Leave> expectedLeaves = List.of(leave);
        List<CreateLeaveResponse> expectedLeaveResponse = List.of(leaveResponse);

        List<Leave> leaves = leaveRepository.findAll();

        List<CreateLeaveResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CreateLeaveResponse.class));

        // assertion on response
        Assertions.assertThat(response)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .isEqualTo(expectedLeaveResponse);

        // assertion on database
        Assertions.assertThat(leaves)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsAll(expectedLeaves);
    }

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testAddLeaveWithNotFoundEmployee_shouldFailAndReturnEmployeeNotFound() throws Exception {
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 1, 2);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(from)
                .endDate(to)
                .employeeId(NON_EXISTENT_ID)
                .build();

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

    @Test
    @DataSet("dataset/create_leave.xml")
    public void testEmployeeAddsLeaveInTheSameDateTwice_shouldFailAndReturnIsConflict() throws Exception {
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 1, 1);

        // there is a leave saved in database for LONG_STANDING_EMPLOYEE_ID employee in 1 Jan 2020
        Employee employee = employeeRepository.findById(LONG_STANDING_EMPLOYEE_ID).get();
        leaveRepository.save(buildLeave(from, employee, false));

        // we're trying to post leave in the same date
        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(from)
                .endDate(to)
                .employeeId(LONG_STANDING_EMPLOYEE_ID)
                .build();

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
