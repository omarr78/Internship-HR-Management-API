package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.CreateBonusRequest;
import com.internship.dto.CreateBonusResponse;
import com.internship.entity.Bonus;
import com.internship.entity.Employee;
import com.internship.repository.BonusRepository;
import com.internship.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class BonusControllerTest {
    static final BigDecimal POSITIVE_AMOUNT = BigDecimal.valueOf(1000);
    static final BigDecimal NEGATIVE_AMOUNT = BigDecimal.valueOf(-500);
    static final BigDecimal ZERO_AMOUNT = BigDecimal.valueOf(0);
    private static final Long NON_EXISTENT_ID = -1L;
    private static final Long EXISTENT_EMPLOYEE_ID = 1L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private BonusRepository bonusRepository;

    @Test
    @DataSet("dataset/create_bonus.xml")
    public void testAddBonusWithoutEnteringDate_shouldSuccessAndReturnBonusDetailsWithDateOfToday() throws Exception {
        // we will create bonus for employee with id 1 and its amount is 1000 and date not entered
        CreateBonusRequest request = CreateBonusRequest.builder()
                .amount(POSITIVE_AMOUNT)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        // capture database state before the api call
        List<Bonus> bonusesBefore = bonusRepository.findAll();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            MvcResult result = mockMvc.perform(post("/api/bonus")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            CreateBonusResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                    CreateBonusResponse.class);

            CreateBonusResponse expectedBonusResponse = new CreateBonusResponse(mockedToday, POSITIVE_AMOUNT,
                    EXISTENT_EMPLOYEE_ID);

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expectedBonusResponse);

            Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE_ID).get();

            Bonus expectedBonus = new Bonus(mockedToday, POSITIVE_AMOUNT, employee);
            // capture database state after the api call
            List<Bonus> bonusesAfter = bonusRepository.findAll();

            // assertion on database
            Assertions.assertThat(bonusesAfter)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsAll(bonusesBefore)
                    .contains(expectedBonus)
                    .hasSize(bonusesBefore.size() + 1);
        }
    }

    @Test
    @DataSet("dataset/create_bonus.xml")
    public void testAddBonusWithEnteringDate_shouldSuccessAndReturnBonusDetailsWithEnteredDate() throws Exception {
        // we will create bonus for employee with id 1 and its amount is 1000 and date 5 Jan 2020
        LocalDate requestedBonusDate = LocalDate.of(2020, 1, 5);
        CreateBonusRequest request = CreateBonusRequest.builder()
                .amount(POSITIVE_AMOUNT)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .bonusDate(requestedBonusDate)
                .build();

        // capture database state before the api call
        List<Bonus> bonusesBefore = bonusRepository.findAll();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            MvcResult result = mockMvc.perform(post("/api/bonus")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            CreateBonusResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                    CreateBonusResponse.class);

            CreateBonusResponse expectedBonusResponse = new CreateBonusResponse(requestedBonusDate, POSITIVE_AMOUNT,
                    EXISTENT_EMPLOYEE_ID);

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expectedBonusResponse);

            Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE_ID).get();

            Bonus expectedBonus = new Bonus(requestedBonusDate, POSITIVE_AMOUNT, employee);
            // capture database state after the api call
            List<Bonus> bonusesAfter = bonusRepository.findAll();

            // assertion on database
            Assertions.assertThat(bonusesAfter)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsAll(bonusesBefore)
                    .contains(expectedBonus)
                    .hasSize(bonusesBefore.size() + 1);
        }
    }

    @Test
    @DataSet("dataset/create_bonus.xml")
    public void testAddBonusWithNegativeAmount_shouldFailAndShouldReturnBadRequest() throws Exception {
        // we will create bonus for employee with id 1 and its amount is NEGATIVE_AMOUNT
        CreateBonusRequest request = CreateBonusRequest.builder()
                .amount(NEGATIVE_AMOUNT)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/bonus")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("must be greater than 0")));
        }
    }

    @Test
    @DataSet("dataset/create_bonus.xml")
    public void testAddBonusWithZeroAmount_shouldFailAndShouldReturnBadRequest() throws Exception {
        // we will create bonus for employee with id 1 and its amount is NEGATIVE_AMOUNT
        CreateBonusRequest request = CreateBonusRequest.builder()
                .amount(ZERO_AMOUNT)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/bonus")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("must be greater than 0")));
        }
    }

    @Test
    @DataSet("dataset/create_bonus.xml")
    public void testAddBonusWithDateInThePreviousMonth_shouldFailAndReturnBadRequest() throws Exception {
        // we will create bonus for employee with id 1 and its amount is 1000 and date 1 Jan 2020
        // and the current date is 1 Feb 2020
        LocalDate requestedBonusDate = LocalDate.of(2020, 1, 1);
        CreateBonusRequest request = CreateBonusRequest.builder()
                .amount(POSITIVE_AMOUNT)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .bonusDate(requestedBonusDate)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 2, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/bonus")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("date must be at least in the same current month")));
        }
    }

    @Test
    @DataSet("dataset/create_bonus.xml")
    public void testAddBonusWithDateNotInCurrentYear_shouldFailsAndReturnBadRequest() throws Exception {
        // we will create bonus for employee with id 1 and its amount is 1000 and date 1 Jan 2021
        // and the current date is 1 Jan 2020
        LocalDate requestedBonusDate = LocalDate.of(2021, 1, 1);

        CreateBonusRequest request = CreateBonusRequest.builder()
                .amount(POSITIVE_AMOUNT)
                .employeeId(EXISTENT_EMPLOYEE_ID)
                .bonusDate(requestedBonusDate)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/bonus")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("date must be in the same current year")));
        }
    }

    @Test
    @DataSet("dataset/create_bonus.xml")
    public void testAddBonusWithNotFoundEmployee_shouldFailAndReturnEmployeeNotFound() throws Exception {
        CreateBonusRequest request = CreateBonusRequest.builder()
                .amount(POSITIVE_AMOUNT)
                .employeeId(NON_EXISTENT_ID)
                .build();

        final LocalDate mockedToday = LocalDate.of(2020, 1, 1);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(mockedToday);
            mockMvc.perform(post("/api/bonus")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                            .contains("Employee not found with id: " + NON_EXISTENT_ID)));
        }
    }
}
