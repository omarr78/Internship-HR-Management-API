package com.internship.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
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

    private CreateEmployeeRequest buildCreateEmployeeRequest() {
        return CreateEmployeeRequest.builder()
                .name("ahmed")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .salary(2000)
                .build();
    }

    private Employee buildEmployee() {
        return Employee.builder()
                .name("omar")
                .dateOfBirth(LocalDate.of(1999, 10, 5))
                .graduationDate(LocalDate.of(2025, 6, 5))
                .gender(MALE)
                .salary(2000)
                .build();
    }

    private Department buildDepartment() {
        return Department.builder()
                .name("Department 1")
                .build();
    }

    private Team buildTeam() {
        return Team.builder()
                .name("Team 1")
                .build();
    }

    private Expertise buildExpertise(String expertiseName) {
        return Expertise.builder()
                .name(expertiseName)
                .build();
    }

    @Test
    public void testAddEmployeeWithoutDepartment_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setTeamId(1L);
        request.setDepartmentId(null); // no department

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
    public void testAddEmployeeWithoutTeam_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(1L);
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
    public void testAddEmployeeWithNotFoundDepartment_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        request.setDepartmentId(NON_EXISTENT_ID); // not found department id
        request.setTeamId(1L);

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
    public void testAddEmployeeWithNotFoundTeam_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        Department department = departmentRepository.save(buildDepartment()); // add department in database
        request.setDepartmentId(department.getId()); // existing department id
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
    public void testAddEmployeeWithExistingDepartmentAndTeam_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        Department department = departmentRepository.save(buildDepartment()); // add department in database
        Team team = teamRepository.save(buildTeam()); // add team in database
        request.setDepartmentId(department.getId()); // existing department id
        request.setTeamId(team.getId()); // existing team id

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
    public void testAddEmployeeWithNotFoundManager_shouldFail() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        Department department = departmentRepository.save(buildDepartment()); // add department in database
        Team team = teamRepository.save(buildTeam()); // add team in database
        request.setDepartmentId(department.getId()); // existing department id
        request.setTeamId(team.getId()); // existing team id
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
    public void testAddEmployeeWithExistingManager_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        Department department = departmentRepository.save(buildDepartment()); // add department in database
        Team team = teamRepository.save(buildTeam()); // add team in database
        Employee manager = buildEmployee();
        manager.setDepartment(department);
        manager.setTeam(team);
        Employee savedManager = employeeRepository.save(manager); // add this manager in database
        request.setDepartmentId(department.getId()); // existing department id
        request.setTeamId(team.getId()); // existing team id
        request.setManagerId(savedManager.getId()); // set managerId to an existing employee


        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertNotNull(response);
        assertEquals(response.getManagerId(), manager.getId());
    }

    @Test
    public void testAddEmployeeWithExistingExpertise_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        Department department = departmentRepository.save(buildDepartment()); // add department in database
        Team team = teamRepository.save(buildTeam()); // add team in database

        Expertise expertise1 = expertiseRepository.save(buildExpertise("Java"));
        Expertise expertise2 = expertiseRepository.save(buildExpertise("Spring boot"));

        request.setDepartmentId(department.getId()); // existing department id
        request.setTeamId(team.getId()); // existing team id
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

    // when name of expertise not found && expertise are not empty so it will be created in expertise table and added to employee
    @Test
    public void testAddEmployeeWithNotExistingExpertise_shouldSucceedAndReturnEmployeeInfo() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        Department department = departmentRepository.save(buildDepartment()); // add department in database
        Team team = teamRepository.save(buildTeam()); // add team in database

        request.setDepartmentId(department.getId()); // existing department id
        request.setTeamId(team.getId()); // existing team id
        request.setExpertises(List.of("Java", "Spring boot")); // Not exist expertise

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

    // if expertise name are empty so it will skip it
    @Test
    public void testAddEmployeeWithEmptyExpertiseName_shouldSucceedAndReturnEmployeeInfoWithoutExpertise() throws Exception {
        CreateEmployeeRequest request = buildCreateEmployeeRequest();
        Department department = departmentRepository.save(buildDepartment()); // add department in database
        Team team = teamRepository.save(buildTeam()); // add team in database

        request.setDepartmentId(department.getId()); // existing department id
        request.setTeamId(team.getId()); // existing team id
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
