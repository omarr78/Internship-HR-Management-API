package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.SalaryDto;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.enums.Gender;
import com.internship.exception.ErrorCode;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.ExpertiseRepository;
import com.internship.repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.internship.enums.Gender.FEMALE;
import static com.internship.enums.Gender.MALE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class EmployeeControllerTest {
    private static final float DELTA = 0.0003f;
    private static final float TAX_REMINDER = 0.85f;
    private static final int INSURANCE_AMOUNT = 500;
    private static final Long NON_EXISTENT_ID = -1L;
    private static final String EMPTY_STRING = "";
    private static final float NEGATIVE_SALARY = -1.0f;
    private static final Long EXISTENT_DEPARTMENT1_ID = 1L;
    private static final Long EXISTENT_DEPARTMENT2_ID = 2L;
    private static final Long EXISTENT_TEAM1_ID = 1L;
    private static final Long EXISTENT_TEAM2_ID = 2L;
    private static final Long EXISTENT_MANAGER1_ID = 10L;
    private static final Long EXISTENT_MANAGER2_ID = 11L;
    private static final Long EXISTENT_EMPLOYEE1_ID = 1L;
    private static final Long EXISTENT_EMPLOYEE2_ID = 2L;
    private static final Long EXISTENT_SUBORDINATES1_ID = 3L;
    private static final Long EXISTENT_SUBORDINATES2_ID = 4L;
    private static final Long EXISTENT_EXPERTISE1_ID = 1L;
    private static final Long EXISTENT_EXPERTISE2_ID = 2L;
    @Autowired
    private JdbcTemplate jdbcTemplate;
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithoutDepartment_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(null); // no department
        request.setTeamId(EXISTENT_TEAM1_ID);
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithoutTeam_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID);
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithNotFoundDepartment_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(NON_EXISTENT_ID); // not found department id
        request.setTeamId(EXISTENT_TEAM1_ID);
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithNotFoundTeam_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithExistingDepartmentAndTeam_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithNotFoundManager_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithExistingManager_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
        request.setManagerId(EXISTENT_MANAGER1_ID); // existing manager id from dataset/create_employee.xml
        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(EXISTENT_MANAGER1_ID, response.getManagerId());
    }

    @Test
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithExistingExpertise_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        // existing expertises from dataset/create_employee.xml
        Expertise expertise1 = expertiseRepository.findById(EXISTENT_EXPERTISE1_ID).get();
        Expertise expertise2 = expertiseRepository.findById(EXISTENT_EXPERTISE2_ID).get();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithNotExistingExpertise_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
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
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithEmptyExpertiseName_shouldSucceedAndReturnEmployeeInfoWithoutExpertise() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
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

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateNotFoundEmployee_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder().build();
        mockMvc.perform(patch("/api/employees/" + NON_EXISTENT_ID)
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
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeNameWithAnEmptyName_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .name(EMPTY_STRING).build(); // set name with empty
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("name must not be empty and at least has 3 characters", error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeSalaryWithNegativeSalary_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .salary(NEGATIVE_SALARY).build(); // set negative
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("salary must be greater than or equal to 0", error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeBirthOfDateAndGraduationDateToDifferenceBetweenYearsLessThan20_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                // the different between years is 15 that is less than 20
                .dateOfBirth(LocalDate.of(2005, 1, 1))
                .graduationDate(LocalDate.of(2020, 1, 1))
                .build();
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("graduation date must be after birth date at least 20 years", error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeByRemovingHisAllFieldBySetItToNull_shouldSuccessAndReturningEmployeeInfoIgnoringChanges() throws Exception {
        // this applied for name, dateOfBirth, graduationDate, gender, departmentId, teamId
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .name(null)
                .dateOfBirth(null)
                .graduationDate(null)
                .gender(null)
                .departmentId(null)
                .teamId(null)
                .build();
        Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE1_ID).get();
        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(employee.getName(), response.getName());
        assertEquals(employee.getDateOfBirth(), response.getDateOfBirth());
        assertEquals(employee.getGraduationDate(), response.getGraduationDate());
        assertEquals(employee.getGender(), response.getGender());
        assertEquals(employee.getDepartment().getId(), response.getDepartmentId());
        assertEquals(employee.getTeam().getId(), response.getTeamId());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeManagerToSelfManagement_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .managerId(Optional.of(EXISTENT_EMPLOYEE1_ID)) // the same id as employee, means -> employee manager on himself
                .build();
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Employee cannot be self management", error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeByRemovingHisManager_shouldSuccessAndReturnEmployeeInfoWithNoManager() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .managerId(Optional.empty())
                .build();
        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNull(response.getManagerId());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeByRemovingHisExpertise_shouldSuccessAndReturnEmployeeInfoWithNoExpertise() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .expertises(List.of())
                .build();
        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertEquals(List.of(), response.getExpertises());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeWithNotFoundDepartment_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .departmentId(NON_EXISTENT_ID)
                .build();
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
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
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeWithNotFoundTeam_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .teamId(NON_EXISTENT_ID)
                .build();
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
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
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeWithNotFoundManager_shouldFail() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .managerId(Optional.of(NON_EXISTENT_ID))
                .build();
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
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
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeWithNotFoundExpertise_shouldSuccessAndReturnEmployeeInfo() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .expertises(List.of("Python", "Database"))
                .build();
        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(response.getExpertises(), request.getExpertises());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeWithoutChangingExpertise_shouldSuccessAndReturnEmployeeWithTheSameExpertise() throws Exception {
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder().build();
        String expertiseName = "spring boot"; // the employee with id 2 has this expertise from dataset/update_employees.xml
        mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE2_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        Employee employee = employeeRepository.findById(EXISTENT_EMPLOYEE2_ID).get();
        assertEquals(1, employee.getExpertises().size());
        assertEquals(expertiseName, employee.getExpertises().get(0).getName());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeWithCorrectValues_shouldSuccessAndReturnEmployeeInfo() throws Exception {
        /*
            will update this employee

            <employees id='1' name='Ahmed' date_of_birth='2003-10-05' graduation_date='2025-06-05' gender='MALE'
               salary='1000' department_id='1' team_id='1' manager_id='10'/>

            to

            <employees id='1' name='mai' date_of_birth='2003-01-01' graduation_date='2025-01-01' gender='FEMALE'
               salary='1500' department_id='2' team_id='2' manager_id='11'/>
        */
        String updatedName = "mai"; // valid name, the length of character >= 3
        // valid dates, the difference between years >= 20
        LocalDate updatedBirthDate = LocalDate.of(2003, 1, 1); // valid date, the date in the past
        LocalDate updatedGraduationDate = LocalDate.of(2025, 1, 1);
        Gender updatedGender = FEMALE;
        // valid salary >= 0
        float updatedSalary = 1500;
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .name(updatedName)
                .dateOfBirth(updatedBirthDate)
                .graduationDate(updatedGraduationDate)
                .gender(updatedGender)
                .salary(updatedSalary)
                .departmentId(EXISTENT_DEPARTMENT2_ID)
                .teamId(EXISTENT_TEAM2_ID)
                .managerId(Optional.of(EXISTENT_MANAGER2_ID))
                .build();
        MvcResult result = mockMvc.perform(patch("/api/employees/" + EXISTENT_EMPLOYEE1_ID)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getDateOfBirth(), response.getDateOfBirth());
        assertEquals(request.getGraduationDate(), response.getGraduationDate());
        assertEquals(request.getGender(), response.getGender());
        assertEquals(request.getSalary(), response.getSalary());
        assertEquals(request.getDepartmentId(), response.getDepartmentId());
        assertEquals(request.getTeamId(), response.getTeamId());
        assertEquals(request.getManagerId().get(), response.getManagerId());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testGetEmployeeInfo_shouldSuccessAndReturnEmployeeInfo() throws Exception {
        // from dataset/update_employees.xml
        /*
        <employees id='1' name='Ahmed' date_of_birth='2003-10-05' graduation_date='2025-06-05' gender='MALE'
               salary='1000' department_id='1' team_id='1' manager_id='10'/>
        <employee_expertise employee_id='1' expertise_id='1'/>  the employee has one expertise
        */
        // get employee with id = 1
        MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE1_ID))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        // Ahmed's data
        String expectedName = "Ahmed";
        LocalDate expectedBirthDate = LocalDate.of(2003, 10, 5);
        LocalDate expectedGraduationDate = LocalDate.of(2025, 6, 5);
        float expectedSalary = 1000;
        Long expectedManagerId = 10L;
        List<String> expectedExpertises = List.of("spring boot");

        assertNotNull(response);
        assertEquals(expectedName, response.getName());
        assertEquals(expectedBirthDate, response.getDateOfBirth());
        assertEquals(expectedGraduationDate, response.getGraduationDate());
        assertEquals(MALE, response.getGender());
        assertEquals(expectedSalary, response.getSalary());
        assertEquals(EXISTENT_DEPARTMENT1_ID, response.getDepartmentId());
        assertEquals(EXISTENT_TEAM1_ID, response.getTeamId());
        assertEquals(expectedManagerId, response.getManagerId());
        assertEquals(expectedExpertises.getFirst(), response.getExpertises().getFirst());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testGetEmployeeInfoWithNotFoundEmployee_shouldFail() throws Exception {
        mockMvc.perform(get("/api/employees/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Employee not found with id: " + NON_EXISTENT_ID, error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/delete_employees.xml")
    public void testDeleteEmployeeHasNoSubordinates_shouldSuccessAndReturnNoContent() throws Exception {
    /*
              1
             Omar
              |
              2
            Ahmed
            /    \
            3     4
        Mohamed Mahmoud
    */
        // try to delete employee Mahmoud
        mockMvc.perform(delete("/api/employees/" + EXISTENT_SUBORDINATES1_ID))
                .andExpect(status().isNoContent());
        // make sure the employee Mahmoud with id = 4 is deleted
        Optional<Employee> mahmoud = employeeRepository.findById(EXISTENT_SUBORDINATES1_ID);
        assertTrue(mahmoud.isEmpty());
    }

    @Test
    @DataSet("dataset/delete_employees.xml")
    public void testDeleteEmployeeHasManagerAndHasSubordinates_ShouldSuccessAndReturnNoContent() throws Exception {
    /*
              1
             Omar
              |
              2
            Ahmed
            /    \
            3     4
        Mohamed Mahmoud
    */
        mockMvc.perform(delete("/api/employees/" + EXISTENT_EMPLOYEE2_ID))
                .andExpect(status().isNoContent());
        Optional<Employee> ahmed = employeeRepository.findById(EXISTENT_EMPLOYEE2_ID);
        // first make sure that the employee Ahmed with id = 2 is deleted
        Assertions.assertTrue(ahmed.isEmpty());
        // then make sure that Ahmed's Subordinates moved to his manager
        // now employee omar is the manager of Mohamed and Mahmoud
        Employee omar = employeeRepository.findById(EXISTENT_EMPLOYEE1_ID).get();
        Employee mohamed = employeeRepository.findById(EXISTENT_SUBORDINATES1_ID).get();
        Employee mahmoud = employeeRepository.findById(EXISTENT_SUBORDINATES2_ID).get();
        assertEquals(mohamed.getManager(), omar);
        assertEquals(mahmoud.getManager(), omar);
    }

    @Test
    @DataSet("dataset/delete_employees.xml")
    public void testDeleteNotFoundEmployee_ShouldFailAndReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Employee not found with id: " + NON_EXISTENT_ID, error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/delete_employees.xml")
    public void testDeleteEmployeeHasNoManagerAndHasSubordinates_ShouldFailAndReturnConflict() throws Exception {
    /*
              1
             Omar
              |
              2
            Ahmed
            /    \
            3     4
        Mohamed Mahmoud
    */
        // will try to delete omar and omar has no manager
        mockMvc.perform(delete("/api/employees/" + EXISTENT_EMPLOYEE1_ID))
                .andExpect(status().isConflict())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Cannot remove manager (employee has subordinates) has no manager"
                            , error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testGetEmployeeSalary_ShouldReturnEmployeeNetSalary() throws Exception {
        /*
        from dataset/update_employees.xml
        <employees id='1' name='Ahmed' salary='1000' />
        */
        float ahmedSalary = 1000;
        float netSalary = ahmedSalary * TAX_REMINDER - INSURANCE_AMOUNT;
        MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE1_ID + "/salary"))
                .andExpect(status().isOk())
                .andReturn();
        SalaryDto response = objectMapper.readValue(result.getResponse().getContentAsString(), SalaryDto.class);
        assertEquals(netSalary, response.getSalary(), DELTA);
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testGetNotFoundEmployeeSalary_ShouldFailAndReturnEmployeeNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/" + NON_EXISTENT_ID + "/salary"))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Employee not found with id: " + NON_EXISTENT_ID, error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testGetEmployeeSalaryWithNegativeNetSalary_ShouldReturnEmployeeNetSalaryWithZero() throws Exception {
        /*
        from dataset/update_employees.xml
        <employees id='2' name='mostafa' salary='100'/>
        */
        mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE2_ID + "/salary"))
                .andExpect(status().isConflict())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Salary cannot be Negative after deduction"
                            , error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetEmployeesUnderManger_shouldSuccessAndReturnAllEmployeeUnderManger() throws Exception {
        /*
                1
                A
              /   \
             2     5
             B     E
            / \    |
           3   4   6
           C   D   F
        */
        MvcResult result = mockMvc.perform(get("/api/employees")
                        .param("recursiveManagerId", String.valueOf(EXISTENT_EMPLOYEE1_ID)))
                .andExpect(status().isOk())
                .andReturn();
        List<EmployeeResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeResponse.class));
        List<String> actualEmployeeNames = response.stream().map(EmployeeResponse::getName).toList();
        List<String> expectedEmployeeNames = List.of("B", "E", "C", "D", "F");
        assertEquals(expectedEmployeeNames, actualEmployeeNames);
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetEmployeesUnderEmployeeHasNoSubordinates_shouldSuccessAndReturnEmptyList() throws Exception {
        /*
                1
                A
              /   \
             2     5
             B     E
            / \    |
           3   4   6
           C   D   F
        */

        // we will test employee C for example
        MvcResult result = mockMvc.perform(get("/api/employees")
                        .param("recursiveManagerId", String.valueOf(EXISTENT_SUBORDINATES1_ID)))
                .andExpect(status().isOk())
                .andReturn();
        List<EmployeeResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeResponse.class));
        assertTrue(response.isEmpty());
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetEmployeesUnderNotFoundEmployee_shouldFailAndReturnEmployeeNotFound() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .param("recursiveManagerId", String.valueOf(NON_EXISTENT_ID)))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Employee not found with id: " + NON_EXISTENT_ID, error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetEmployeesUnderMangerWithHierarchyCycleDetected_shouldSuccessAndReturnAllEmployeeUnderManger() throws Exception {
        /*
                1
                A  <------|
              /   \       |
             2     5      |
             B     E      |
            / \    |      |
           3   4   6      |
           C   D   F -----|
        */
        // if F is manager of A entered by mistake it will throw an exception when get all employee under manager
        Long employeeFId = 6L;
        // Set F as the manager of A
        String updateManagerQuery = "UPDATE employees SET manager_id = ? WHERE id = ?";
        jdbcTemplate.update(updateManagerQuery, employeeFId, EXISTENT_EMPLOYEE1_ID);

        mockMvc.perform(get("/api/employees")
                        .param("recursiveManagerId", String.valueOf(EXISTENT_EMPLOYEE1_ID)))
                .andExpect(status().isConflict())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Cycle detected in employee hierarchy", error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetEmployeesUnderManagerWithMissedParameter_shouldFailAndReturnEmployeeBadRequest() throws Exception {
        mockMvc.perform(get("/api/employees"))
//                    missed recursiveManagerId parameter
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("recursiveManagerId parameter is missing", error.getErrorMessage());
                });
    }
}
