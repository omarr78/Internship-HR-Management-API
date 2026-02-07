package com.internship.mapper;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeDtoInterface;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class EmployeeMapper {
    public Employee toEmployee(CreateEmployeeRequest request,
                               Department department, Team team,
                               Employee manager, List<Expertise> expertises) {
        return Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .nationalId(request.getNationalId())
                .degree(request.getDegree())
                .pastExperienceYear(request.getPastExperienceYear())
                .joinedDate(request.getJoinedDate())
                .dateOfBirth(request.getDateOfBirth())
                .graduationDate(request.getGraduationDate())
                .gender(request.getGender())
                .department(department)
                .team(team)
                .manager(manager)
                .expertises(expertises)
                .build();
    }

    public EmployeeResponse toResponse(Employee employee, int yearsOfExperience, int leaveDays,
                                       BigDecimal grossSalary) {
        List<String> expertises = employee.getExpertises()
                .stream().map(Expertise::getName).toList();

        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .nationalId(employee.getNationalId())
                .degree(employee.getDegree())
                .joinedDate(employee.getJoinedDate())
                .yearsOfExperience(yearsOfExperience)
                .dateOfBirth(employee.getDateOfBirth())
                .graduationDate(employee.getGraduationDate())
                .gender(employee.getGender())
                .grossSalary(grossSalary)
                .leaveDays(leaveDays)
                .departmentId(employee.getDepartment().getId())
                .teamId(employee.getTeam().getId())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .expertises(expertises)
                .build();
    }

    public EmployeeResponse fromInterfaceToResponse(EmployeeDtoInterface dto, int yearsOfExperience, int leaveDays) {
        return EmployeeResponse.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .nationalId(dto.getNationalId())
                .degree(dto.getDegree())
                .joinedDate(dto.getJoinedDate())
                .yearsOfExperience(yearsOfExperience)
                .dateOfBirth(dto.getDateOfBirth())
                .graduationDate(dto.getGraduationDate())
                .gender(dto.getGender())
                .grossSalary(dto.getGrossSalary())
                .leaveDays(leaveDays)
                .departmentId(dto.getDepartmentId())
                .teamId(dto.getTeamId())
                .managerId(dto.getManagerId())
                .expertises(dto.getExpertises())
                .build();
    }

}
