package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.EmployeeResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class TeamControllerTest {
    private static final Long EXISTENT_TEAM1_ID = 1L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DataSet("dataset/get-employees-under-team.xml")
    public void testGetEmployeesUnderTeam_shouldSuccessAndReturnEmployeesUnderTeam() throws Exception {
        /*
            -- From data set
            1- team A -> Omar, Ahmed, Mostafa
            2- team B -> Ali, Mohamed
            3- team C ->
        */
        // we will get all employees under team A
        MvcResult result = mockMvc.perform(get("/api/teams/" + EXISTENT_TEAM1_ID + "/members"))
                .andExpect(status().isOk())
                .andReturn();

        List<EmployeeResponse> employeeResponses = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeResponse.class));

        List<String> expectedEmployeeNames = List.of("Omar", "Ahmed", "Mostafa");
        List<String> actualEmployeeNames = employeeResponses.stream().map(EmployeeResponse::getName).toList();

        assertEquals(expectedEmployeeNames.size(), actualEmployeeNames.size());
        assertTrue(expectedEmployeeNames.containsAll(actualEmployeeNames));
        assertTrue(actualEmployeeNames.containsAll(expectedEmployeeNames));
    }
}
