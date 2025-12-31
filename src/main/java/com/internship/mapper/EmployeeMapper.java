package com.internship.mapper;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeDtoInterface;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EmployeeMapper {
    private static final int MIN_YEARS_FOR_EXTRA_LEAVE = 10;
    private static final int STANDARD_LEAVE_DAYS = 21;
    private static final int EXTENDED_LEAVE_DAYS = 30;

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
                .grossSalary(request.getGrossSalary())
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
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .nationalId(employee.getNationalId())
                .degree(employee.getDegree())
                .joinedDate(employee.getJoinedDate())
                .yearsOfExperience(
                        calculateYearsOfExperience(employee.getPastExperienceYear(), employee.getJoinedDate())
                )
                .dateOfBirth(employee.getDateOfBirth())
                .graduationDate(employee.getGraduationDate())
                .gender(employee.getGender())
                .grossSalary(employee.getGrossSalary())
                .leaveDays(getTheNumberOfLeaveDays(employee.getJoinedDate()))
                .departmentId(employee.getDepartment().getId())
                .teamId(employee.getTeam().getId())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .expertises(expertises)
                .build();
    }

    public EmployeeResponse formInterfaceToResponse(EmployeeDtoInterface dto) {
        return EmployeeResponse.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .nationalId(dto.getNationalId())
                .degree(dto.getDegree())
                .joinedDate(dto.getJoinedDate())
                .yearsOfExperience(
                        calculateYearsOfExperience(dto.getPastExperienceYear(), dto.getJoinedDate())
                )
                .dateOfBirth(dto.getDateOfBirth())
                .graduationDate(dto.getGraduationDate())
                .gender(dto.getGender())
                .grossSalary(dto.getGrossSalary())
                .leaveDays(getTheNumberOfLeaveDays(dto.getJoinedDate()))
                .departmentId(dto.getDepartmentId())
                .teamId(dto.getTeamId())
                .managerId(dto.getManagerId())
                .expertises(dto.getExpertises())
                .build();
    }

    public void updateEmployee(Employee employee, UpdateEmployeeRequest request,
                               Department department, Team team,
                               Employee manager, List<Expertise> expertises) {
        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getNationalId() != null) {
            employee.setNationalId(request.getNationalId());
        }
        if (request.getDegree() != null) {
            employee.setDegree(request.getDegree());
        }
        if (request.getPastExperienceYear() != null) {
            employee.setPastExperienceYear(request.getPastExperienceYear());
        }
        if (request.getJoinedDate() != null) {
            employee.setJoinedDate(request.getJoinedDate());
        }
        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGraduationDate() != null) {
            employee.setGraduationDate(request.getGraduationDate());
        }
        if (request.getGender() != null) {
            employee.setGender(request.getGender());
        }
        if (request.getGrossSalary() != null) {
            employee.setGrossSalary(request.getGrossSalary());
        }
        employee.setDepartment(department);
        employee.setTeam(team);
        employee.setManager(manager);
        employee.setExpertises(expertises);
    }

    private int calculateYearsOfExperience(int pastExperience, LocalDate joinedDate) {
        int currentYear = LocalDate.now().getYear();
        int joinedYear = joinedDate.getYear();
        return pastExperience + (currentYear - joinedYear);
    }

    private int getTheNumberOfLeaveDays(LocalDate joinedDate) {
        int currentYear = LocalDate.now().getYear();
        int joinedYear = joinedDate.getYear();
        return currentYear - joinedYear >= MIN_YEARS_FOR_EXTRA_LEAVE
                ? EXTENDED_LEAVE_DAYS : STANDARD_LEAVE_DAYS;
    }
}
