package com.internship.dto;

import com.internship.enums.Degree;
import com.internship.enums.Gender;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String nationalId;
    private Degree degree;
    private Integer yearsOfExperience;
    private LocalDate joinedDate;
    private LocalDate dateOfBirth;
    private LocalDate graduationDate;
    private Gender gender;
    private float grossSalary;
    private Integer leaveDays;
    private Long departmentId;
    private Long teamId;
    private Long managerId;
    private List<String> expertises;
}
