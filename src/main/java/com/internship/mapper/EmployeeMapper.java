package com.internship.mapper;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.internship.enums.Gender.MALE;

@Component
public class EmployeeMapper {
    public Employee toEmployee(CreateEmployeeRequest request,
                               Department department,
                               Team team,
                               Employee manager) {
        return Employee.builder()
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .graduationDate(request.getGraduationDate())
                .gender(request.getGender())
                .department(department)
                .team(team)
                .manager(manager)
                .expertises(request.getExpertises())
                .salary(2000)
                .build();
    }
}
