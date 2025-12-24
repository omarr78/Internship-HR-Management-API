package com.internship.mapper;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeDtoInterface;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeMapper {
    public Employee toEmployee(CreateEmployeeRequest request,
                               Department department, Team team,
                               Employee manager, List<Expertise> expertises) {
        return Employee.builder()
                .firstName(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .graduationDate(request.getGraduationDate())
                .gender(request.getGender())
                .grossSalary(request.getSalary())
                .department(department)
                .team(team)
                .manager(manager)
                .expertises(expertises)
                .build();
    }

    public EmployeeResponse toResponse(Employee employee) {
        List<String> expertises = employee.getExpertises()
                .stream().map(Expertise::getName).toList();

        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getFirstName())
                .dateOfBirth(employee.getDateOfBirth())
                .graduationDate(employee.getGraduationDate())
                .gender(employee.getGender())
                .salary(employee.getGrossSalary())
                .departmentId(employee.getDepartment().getId())
                .teamId(employee.getTeam().getId())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .expertises(expertises)
                .build();
    }

    public EmployeeResponse formInterfaceToResponse(EmployeeDtoInterface dto) {
        return EmployeeResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .dateOfBirth(dto.getDateOfBirth())
                .graduationDate(dto.getGraduationDate())
                .gender(dto.getGender())
                .salary(dto.getSalary())
                .departmentId(dto.getDepartmentId())
                .teamId(dto.getTeamId())
                .managerId(dto.getManagerId())
                .expertises(dto.getExpertises())
                .build();
    }
}
