package com.internship.mapper;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.CreateEmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.internship.enums.Gender.MALE;


@Component
public class EmployeeMapper {
    public Employee toEmployee(CreateEmployeeRequest request,
                               Department department,
                               Team team,
                               Employee manager,
                               List<Expertise> expertises) {
        return Employee.builder()
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .graduationDate(request.getGraduationDate())
                .gender(request.getGender())
                .department(department)
                .team(team)
                .manager(manager)
                .expertises(expertises)
                .salary(request.getSalary())
                .build();
    }

    public CreateEmployeeResponse toResponse(Employee employee) {
        List<String> expertises = employee.getExpertises()
                .stream().map(Expertise::getName).toList();

        return CreateEmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .dateOfBirth(employee.getDateOfBirth())
                .graduationDate(employee.getGraduationDate())
                .gender(employee.getGender())
                .departmentId(employee.getDepartment().getId())
                .teamId(employee.getTeam().getId())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .salary(employee.getSalary())
                .expertises(expertises)
                .build();
    }
}
