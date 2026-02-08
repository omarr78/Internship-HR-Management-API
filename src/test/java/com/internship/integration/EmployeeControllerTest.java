package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.SalaryDto;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.entity.Employee;
import com.internship.entity.EmployeeSalary;
import com.internship.entity.Expertise;
import com.internship.enums.Degree;
import com.internship.enums.Gender;
import com.internship.enums.SalaryReason;
import com.internship.exception.ErrorCode;
import com.internship.repository.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.internship.enums.Degree.FRESH;
import static com.internship.enums.Degree.INTERMEDIATE;
import static com.internship.enums.Gender.FEMALE;
import static com.internship.enums.Gender.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
public class EmployeeControllerTest {
    private static final BigDecimal TAX_REMINDER = BigDecimal.valueOf(0.85);
    private static final BigDecimal INSURANCE_AMOUNT = BigDecimal.valueOf(500);
    private static final Long NON_EXISTENT_ID = -1L;
    private static final String EMPTY_STRING = "";
    private static final BigDecimal NEGATIVE_SALARY = BigDecimal.valueOf(-1.0);
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
    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 1, 1);
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExpertiseRepository expertiseRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private EmployeeSalaryRepository employeeSalaryRepository;

    private CreateEmployeeRequest buildCreateEmployeeRequest() {
        return CreateEmployeeRequest.builder()
                .firstName("Ahmed")
                .lastName("Ali")
                .nationalId("28501020112345")
                .degree(Degree.INTERMEDIATE)
                .pastExperienceYear(3)
                .joinedDate(LocalDate.of(2022, 12, 5))
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .grossSalary(BigDecimal.valueOf(5000))
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

        // capture database state before the api call
        List<Employee> employeesBefore = employeeRepository.findAll();
        List<EmployeeSalary> employeeSalariesBefore = employeeSalaryRepository.findAll();

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(FIXED_DATE);

            MvcResult result = mockMvc.perform(post("/api/employees")
                            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();
            EmployeeResponse response = objectMapper
                    .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

            // joined year = 2022, past experience = 3
            // then years of experience = 3 + (2025 - 2022) = 6
            final int EXPECTED_YEARS_OF_EXPERIENCE = 6;

            // to calculate the number of leave days
            // 2025 - 2022 = 3 < 10 so it will be 21 day
            int EXPECTED_LEAVE_DAYS = 21;

            // assertion on response
            EmployeeResponse expectedEmployeeResponse = EmployeeResponse.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .nationalId(request.getNationalId())
                    .degree(request.getDegree())
                    .joinedDate(request.getJoinedDate())
                    .dateOfBirth(request.getDateOfBirth())
                    .graduationDate(request.getGraduationDate())
                    .gender(request.getGender())
                    .grossSalary(request.getGrossSalary())
                    .departmentId(request.getDepartmentId())
                    .teamId(request.getTeamId())
                    .yearsOfExperience(EXPECTED_YEARS_OF_EXPERIENCE)
                    .leaveDays(EXPECTED_LEAVE_DAYS)
                    .expertises(List.of())
                    .build();

            Assertions.assertThat(response)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expectedEmployeeResponse);

            // assertion on database for inserted employee
            Employee expectedEmployee = Employee.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .nationalId(request.getNationalId())
                    .degree(request.getDegree())
                    .pastExperienceYear(request.getPastExperienceYear())
                    .joinedDate(request.getJoinedDate())
                    .dateOfBirth(request.getDateOfBirth())
                    .graduationDate(request.getGraduationDate())
                    .gender(request.getGender())
                    .department(departmentRepository.findById(EXISTENT_DEPARTMENT1_ID).get())
                    .team(teamRepository.findById(EXISTENT_TEAM1_ID).get())
                    .expertises(List.of())
                    .build();

            List<Employee> employeesAfter = employeeRepository.findAll();

            Assertions.assertThat(employeesAfter)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsAll(employeesBefore)
                    .contains(expectedEmployee)
                    .hasSize(employeesBefore.size() + 1);

            // assertion on database for inserted employeeSalary
            List<EmployeeSalary> employeeSalariesAfter = employeeSalaryRepository.findAll();

            BigDecimal expectedGrossSalary = request.getGrossSalary();
            String expectedReason = SalaryReason.INITIAL_BASE_SALARY.getMessage();

            // get inserted employee
            Employee insertedEmployee = employeesAfter.stream()
                    .filter(e -> !employeesBefore.contains(e))
                    .findFirst().get();

            // get inserted salary
            EmployeeSalary insertedSalary = employeeSalariesAfter.stream()
                    .filter(es -> !employeeSalariesBefore.contains(es))
                    .findFirst().get();

            assertThat(insertedSalary.getCreationDate()).isNotNull();
            assertEquals(expectedGrossSalary, insertedSalary.getGrossSalary());
            assertEquals(expectedReason, insertedSalary.getReason());
            assertEquals(insertedEmployee, insertedSalary.getEmployee());
        }
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

        // capture database state before the api call
        List<Employee> employeesBefore = employeeRepository.findAll();

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(EXISTENT_MANAGER1_ID, response.getManagerId());

        // assertion on database that employee has expected manager
        List<Employee> employeesAfter = employeeRepository.findAll();

        // get inserted employee
        Employee insertedEmployee = employeesAfter.stream()
                .filter(e -> !employeesBefore.contains(e))
                .findFirst().get();

        assertEquals(EXISTENT_MANAGER1_ID, insertedEmployee.getManager().getId());
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

        // capture database state before the api call
        List<Employee> employeesBefore = employeeRepository.findAll();

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(response.getExpertises(), request.getExpertises());

        // assertion on database that employee has expected expertise
        List<Employee> employeesAfter = employeeRepository.findAll();

        // get inserted employee
        Employee insertedEmployee = employeesAfter.stream()
                .filter(e -> !employeesBefore.contains(e))
                .findFirst().get();

        List<Expertise> expectedExpertises = List.of(expertise1, expertise2);
        assertEquals(expectedExpertises, insertedEmployee.getExpertises());
    }

    // when the name of expertise not found && expertise are not empty
    // so it will be created in expertise table and added to employee
    @Test
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeWithNotExistingExpertise_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
        request.setExpertises(List.of("Python", "Database")); // Not exist expertise

        // capture database state before the api call
        List<Employee> employeesBefore = employeeRepository.findAll();

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(response.getExpertises(), request.getExpertises());

        // assertion on database that employee has expected expertise
        List<Employee> employeesAfter = employeeRepository.findAll();

        // get inserted employee
        Employee insertedEmployee = employeesAfter.stream()
                .filter(e -> !employeesBefore.contains(e))
                .findFirst().get();

        List<String> expectedExpertises = request.getExpertises();
        List<String> actualExpertises = insertedEmployee.getExpertises()
                .stream()
                .map(Expertise::getName)
                .toList();

        assertEquals(expectedExpertises, actualExpertises);
    }

    // if expertise name is empty, so it will skip it
    @Test
    @DataSet("dataset/create_employee.xml")
    public void testAddEmployeeEmptyExpertiseName_shouldSucceedAndReturnEmployeeWithoutExpertise() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(EXISTENT_DEPARTMENT1_ID); // existing department id from dataset/create_employee.xml
        request.setTeamId(EXISTENT_TEAM1_ID); // existing team id from dataset/create_employee.xml
        request.setExpertises(List.of(EMPTY_STRING, EMPTY_STRING)); // empty expertise names -> ""

        // capture database state before the api call
        List<Employee> employeesBefore = employeeRepository.findAll();

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);
        assertNotNull(response);
        assertEquals(response.getExpertises(), List.of());

        // assertion on database that employee has expected expertise
        List<Employee> employeesAfter = employeeRepository.findAll();

        // get inserted employee
        Employee insertedEmployee = employeesAfter.stream()
                .filter(e -> !employeesBefore.contains(e))
                .findFirst().get();

        assertEquals(List.of(), insertedEmployee.getExpertises());
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
                .firstName(EMPTY_STRING).build(); // set name with empty
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
                .grossSalary(NEGATIVE_SALARY).build(); // set negative
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
    public void testUpdateEmployeeDatesWithAgeGapUnder20_shouldFail() throws Exception {
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

    // testUpdateEmployeeByRemovingHisAllField
    @Test
    @DataSet("dataset/update_employees.xml")
    public void testUpdateEmployeeSetAllFieldsNull_shouldSucceedIgnoreChanges() throws Exception {
        // this applied for name, dateOfBirth, graduationDate, gender, departmentId, teamId
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .firstName(null)
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
        assertEquals(employee.getFirstName(), response.getFirstName());
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
                // the same id as employee, means -> employee manager on himself
                .managerId(Optional.of(EXISTENT_EMPLOYEE1_ID))
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
    public void testUpdateEmployeeRemoveManager_shouldSuccessAndReturnEmployeeInfoWithNoManager() throws Exception {
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
    public void testUpdateEmployeeRemoveExpertise_shouldSuccessAndReturnEmployeeInfoWithNoExpertise() throws Exception {
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
    public void testUpdateEmployeeKeepExpertise_shouldSuccessAndReturnEmployeeWithTheSameExpertise() throws Exception {
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

            <employees id='1' first_name='Ahmed' last_name='Ali' national_id='NID-AHM-003' degree='INTERMEDIATE'
             past_experience_year='2' joined_date='2024-02-01' date_of_birth='2003-10-05' graduation_date='2025-06-05'
             gender='MALE' salary='1000' department_id='1' team_id='1' manager_id='10'/>

            to

            <employees id='1' first_name='Mai' last_name='Mohamed' national_id='NID-MAI-004' degree='FRESH'
             past_experience_year='0' joined_date='2025-01-01' date_of_birth='2003-01-01' graduation_date='2025-01-01'
             gender='FEMALE' salary='1500' department_id='2' team_id='2' manager_id='11'/>
        */

        String updatedFirstName = "Mai"; // valid name, the length of character >= 3
        String updatedLastName = "Mohamed"; // valid name, the length of character >= 3
        String updatedNationalId = "NID-MAI-004";
        Degree updatedDegree = FRESH;
        int updatedPastExperience = 0;
        LocalDate updatedJoinedDate = LocalDate.of(2025, 1, 1);
        // valid dates, the difference between years >= 20
        LocalDate updatedBirthDate = LocalDate.of(2003, 1, 1); // valid date, the date in the past
        LocalDate updatedGraduationDate = LocalDate.of(2025, 1, 1);
        Gender updatedGender = FEMALE;
        // valid salary >= 0
        BigDecimal updatedGrossSalary = BigDecimal.valueOf(1500);
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .firstName(updatedFirstName)
                .lastName(updatedLastName)
                .nationalId(updatedNationalId)
                .degree(updatedDegree)
                .pastExperienceYear(updatedPastExperience)
                .joinedDate(updatedJoinedDate)
                .dateOfBirth(updatedBirthDate)
                .graduationDate(updatedGraduationDate)
                .gender(updatedGender)
                .grossSalary(updatedGrossSalary)
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
        assertNotNull(response.getId());

        assertEquals(updatedFirstName, response.getFirstName());
        assertEquals(updatedLastName, response.getLastName());
        assertEquals(updatedNationalId, response.getNationalId());
        assertEquals(updatedDegree, response.getDegree());
        assertEquals(updatedJoinedDate, response.getJoinedDate());
        assertEquals(updatedBirthDate, response.getDateOfBirth());
        assertEquals(updatedGraduationDate, response.getGraduationDate());
        assertEquals(updatedGender, response.getGender());
        assertEquals(updatedGrossSalary, response.getGrossSalary());
        assertEquals(EXISTENT_DEPARTMENT2_ID, response.getDepartmentId());
        assertEquals(EXISTENT_TEAM2_ID, response.getTeamId());
        assertEquals(EXISTENT_MANAGER2_ID, response.getManagerId());
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testGetEmployeeInfo_shouldSuccessAndReturnEmployeeInfo() throws Exception {
        // from dataset/update_employees.xml
        /*
        <employees id='1' first_name='Ahmed' last_name='Ali' national_id='NID-AHM-003' degree='INTERMEDIATE'
         past_experience_year='2' joined_date='2024-02-01' date_of_birth='2003-10-05' graduation_date='2025-06-05'
         gender='MALE' salary='1000' department_id='1' team_id='1' manager_id='10'/>
        <employee_expertise employee_id='1' expertise_id='1'/>  the employee has one expertise
        */
        // get employee with id = 1
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(FIXED_DATE);
            MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE1_ID))
                    .andExpect(status().isOk())
                    .andReturn();
            EmployeeResponse response = objectMapper
                    .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

            // Ahmed's data
            assertNotNull(response);

            String expectedName = "Ahmed";
            assertEquals(expectedName, response.getFirstName());

            String expectedLastName = "Ali";
            assertEquals(expectedLastName, response.getLastName());

            String expectedNationalId = "NID-AHM-003";
            assertEquals(expectedNationalId, response.getNationalId());

            assertEquals(INTERMEDIATE, response.getDegree());

            LocalDate expectedJoinedYear = LocalDate.of(2024, 2, 1);
            assertEquals(expectedJoinedYear, response.getJoinedDate());

            // years of experience = past experience + (current date - joined date)
            // then years of experience = 2 + (2025 - 2024) = 3
            int expectedYearOfExperience = 3;
            assertEquals(expectedYearOfExperience, response.getYearsOfExperience());

            LocalDate expectedBirthDate = LocalDate.of(2003, 10, 5);
            assertEquals(expectedBirthDate, response.getDateOfBirth());

            LocalDate expectedGraduationDate = LocalDate.of(2025, 6, 5);
            assertEquals(expectedGraduationDate, response.getGraduationDate());

            BigDecimal expectedSalary = BigDecimal.valueOf(1000);
            assertThat(response.getGrossSalary()).isEqualByComparingTo(expectedSalary);

            // to calculate the number of leave days
            // current date - joined date
            // 2025 - 2024 = 1 < 10 so it will be 21 day
            int expectedLeaveDays = 21;
            assertEquals(expectedLeaveDays, response.getLeaveDays());

            Long expectedManagerId = 10L;
            assertEquals(expectedManagerId, response.getManagerId());

            List<String> expectedExpertises = List.of("spring boot");
            assertEquals(expectedExpertises.getFirst(), response.getExpertises().getFirst());

            assertEquals(MALE, response.getGender());
            assertEquals(EXISTENT_DEPARTMENT1_ID, response.getDepartmentId());
            assertEquals(EXISTENT_TEAM1_ID, response.getTeamId());
        }
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
        assertThat(ahmed.isEmpty());
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
                    assertEquals("Cannot remove manager (employee has subordinates) has no manager",
                            error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/update_employees.xml")
    public void testGetEmployeeSalary_ShouldReturnEmployeeNetSalary() throws Exception {
        /*
        from dataset/update_employees.xml
        <employees id='1' name='Ahmed' salary='1000' />
        */
        BigDecimal ahmedSalary = BigDecimal.valueOf(1000);
        BigDecimal netSalary = ahmedSalary.multiply(TAX_REMINDER).subtract(INSURANCE_AMOUNT);
        MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE1_ID + "/salary"))
                .andExpect(status().isOk())
                .andReturn();
        SalaryDto response = objectMapper.readValue(result.getResponse().getContentAsString(), SalaryDto.class);
        assertThat(response.getGrossSalary()).isEqualByComparingTo(ahmedSalary);
        assertThat(response.getNetSalary()).isEqualByComparingTo(netSalary);
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
    public void testGetEmployeeSalaryWithNegativeNetSalary_ShouldFailAndReturnIsConflict() throws Exception {
        /*
        from dataset/update_employees.xml
        <employees id='2' name='mostafa' salary='100'/>
        */
        mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE2_ID + "/salary"))
                .andExpect(status().isConflict())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Salary cannot be Negative after deduction",
                            error.getErrorMessage());
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
        MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE1_ID + "/hierarchy"))
                .andExpect(status().isOk())
                .andReturn();
        List<EmployeeResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeResponse.class));
        List<String> actualEmployeeNames = response.stream().map(EmployeeResponse::getFirstName).toList();
        List<String> expectedEmployeeNames = List.of("B", "E", "C", "D", "F");
        assertEquals(expectedEmployeeNames.size(), actualEmployeeNames.size());
        assertTrue(expectedEmployeeNames.containsAll(actualEmployeeNames));
        assertTrue(actualEmployeeNames.containsAll(expectedEmployeeNames));
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
        MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_SUBORDINATES1_ID + "/hierarchy"))
                .andExpect(status().isOk())
                .andReturn();
        List<EmployeeResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeResponse.class));
        assertTrue(response.isEmpty());
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetEmployeesUnderNotFoundEmployee_shouldFailAndReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/" + NON_EXISTENT_ID + "/hierarchy"))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Employee not found with id: " + NON_EXISTENT_ID, error.getErrorMessage());
                });
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetDirectEmployeesUnderManager_shouldReturnHisSubordinates() throws Exception {
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
        // we will test employee A for example
        MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_EMPLOYEE1_ID + "/subordinates"))
                .andExpect(status().isOk())
                .andReturn();
        List<EmployeeResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeResponse.class));
        List<String> actualEmployeeNames = response.stream().map(EmployeeResponse::getFirstName).toList();
        List<String> expectedEmployeeNames = List.of("B", "E");
        assertEquals(expectedEmployeeNames.size(), actualEmployeeNames.size());
        assertTrue(expectedEmployeeNames.containsAll(actualEmployeeNames));
        assertTrue(actualEmployeeNames.containsAll(expectedEmployeeNames));
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetDirectEmployeesUnderManagerWithNoSubordinates_shouldReturnEmptyList() throws Exception {
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
        // we will test employee A for example
        MvcResult result = mockMvc.perform(get("/api/employees/" + EXISTENT_SUBORDINATES1_ID + "/subordinates"))
                .andExpect(status().isOk())
                .andReturn();
        List<EmployeeResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeResponse.class));
        assertTrue(response.isEmpty());
    }

    @Test
    @DataSet("dataset/get-employees-under-manager.xml")
    public void testGetDirectEmployeesUnderNotFoundEmployee_shouldFailAndReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/" + NON_EXISTENT_ID + "/subordinates"))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ErrorCode error = objectMapper.readValue(json, ErrorCode.class);
                    assertEquals("Employee not found with id: " + NON_EXISTENT_ID, error.getErrorMessage());
                });
    }
}
