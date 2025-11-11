package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Expertise;
import com.internship.exception.ErrorCode;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.ExpertiseRepository;
import com.internship.repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static com.internship.enums.Gender.MALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExpertiseRepository expertiseRepository;

    private static final Long NON_EXISTENT_ID = -1L;
    private static final String EMPTY_STRING = "";
    // from dataset/employees.xml
    private static final Long EXISTENT_DEPARTMENT_ID = 1L;
    private static final Long EXISTENT_TEAM_ID = 1L;
    private static final Long EXISTENT_MANAGER_ID = 10L;
    private static final Long EXISTENT_EXPERTISE1_ID = 1L;
    private static final Long EXISTENT_EXPERTISE2_ID = 2L;

    private CreateEmployeeRequest buildCreateEmployeeRequest() {
        return CreateEmployeeRequest.builder()
                .name("ahmed")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .salary(2000)
                .build();
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithoutDepartment_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(null); // no department
        request.setTeamId(EXISTENT_TEAM_ID);

        mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("departmentId required", error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithoutTeam_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID);
        request.setTeamId(null); // no team

        mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("teamId required", error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithNotFoundDepartment_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(NON_EXISTENT_ID); // not found department id
        request.setTeamId(EXISTENT_TEAM_ID);

        mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Department not found with id: " + request.getDepartmentId(), error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithNotFoundTeam_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID); // existing department id from dataset/employees.xml
        request.setTeamId(NON_EXISTENT_ID); // not found team id

        mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Team not found with id: " + request.getTeamId(), error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithExistingDepartmentAndTeam_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID); // existing department id from dataset/employees.xml
        request.setTeamId(EXISTENT_TEAM_ID); // existing team id from dataset/employees.xml

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(response.getName(), request.getName());
        assertEquals(response.getDateOfBirth(), request.getDateOfBirth());
        assertEquals(response.getGraduationDate(), request.getGraduationDate());
        assertEquals(response.getGender(), request.getGender());
        assertEquals(response.getSalary(), request.getSalary());
        assertEquals(response.getDepartmentId(), request.getDepartmentId());
        assertEquals(response.getTeamId(), request.getTeamId());
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithNotFoundManager_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID); // existing department id from dataset/employees.xml
        request.setTeamId(EXISTENT_TEAM_ID); // existing team id from dataset/employees.xml
        request.setManagerId(NON_EXISTENT_ID); // there is no employee with this id

        mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Manager not found with id: " + request.getManagerId(), error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithExistingManager_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID); // existing department id from dataset/employees.xml
        request.setTeamId(EXISTENT_TEAM_ID); // existing team id from dataset/employees.xml
        request.setManagerId(EXISTENT_MANAGER_ID); // existing manager id from dataset/employees.xml

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertNotNull(response);
        assertEquals(EXISTENT_MANAGER_ID, response.getManagerId());
    }

    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithExistingExpertise_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        // existing expertises from dataset/employees.xml
        Expertise expertise1 = expertiseRepository.findById(EXISTENT_EXPERTISE1_ID).get();
        Expertise expertise2 = expertiseRepository.findById(EXISTENT_EXPERTISE2_ID).get();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID); // existing department id from dataset/employees.xml
        request.setTeamId(EXISTENT_TEAM_ID); // existing team id from dataset/employees.xml
        request.setExpertises(List.of(expertise1.getName(), expertise2.getName()));

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertNotNull(response);
        assertEquals(response.getExpertises(), request.getExpertises());
    }

    // when the name of expertise not found && expertise are not empty so it will be created in expertise table and added to employee
    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithNotExistingExpertise_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID); // existing department id from dataset/employees.xml
        request.setTeamId(EXISTENT_TEAM_ID); // existing team id from dataset/employees.xml
        request.setExpertises(List.of("Python", "Database")); // Not exist expertise

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertNotNull(response);
        assertEquals(response.getExpertises(), request.getExpertises());
    }

    // if expertise name is empty, so it will skip it
    @Test
    @DataSet("dataset/employees.xml")
    public void testAddEmployeeWithEmptyExpertiseName_shouldSucceedAndReturnEmployeeInfoWithoutExpertise() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT_ID); // existing department id from dataset/employees.xml
        request.setTeamId(EXISTENT_TEAM_ID); // existing team id from dataset/employees.xml
        request.setExpertises(List.of(EMPTY_STRING, EMPTY_STRING)); // empty expertise names -> ""

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertNotNull(response);
        assertEquals(response.getExpertises(), List.of());
    }
}
