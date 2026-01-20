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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class BonusControllerTest {
    static final Float POSITIVE_AMOUNT = 1000F;
    static final Float NEGATIVE_AMOUNT = -500F;
    static final Float ZERO_AMOUNT = 0F;
    private static final Long NON_EXISTENT_ID = -1L;
    private static final Long EXISTENT_EMPLOYEE_ID = 1L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private BonusRepository leaveRepository;
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

            CreateBonusResponse expectedBonusResponse = new CreateBonusResponse(mockedToday, EXISTENT_EMPLOYEE_ID);

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expectedBonusResponse);

            Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE_ID).get();

            Bonus expectedBonus = new Bonus(mockedToday, POSITIVE_AMOUNT, employee);
            List<Bonus> bonuses = bonusRepository.findAll();

            // assertion on database
            Assertions.assertThat(bonuses)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .contains(expectedBonus);
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

            CreateBonusResponse expectedBonusResponse = new CreateBonusResponse(requestedBonusDate,
                    EXISTENT_EMPLOYEE_ID);

            // assertion on response
            Assertions.assertThat(response)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expectedBonusResponse);

            Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE_ID).get();

            Bonus expectedBonus = new Bonus(requestedBonusDate, POSITIVE_AMOUNT, employee);
            List<Bonus> bonuses = bonusRepository.findAll();

            // assertion on database
            Assertions.assertThat(bonuses)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .contains(expectedBonus);
        }
    }
}
