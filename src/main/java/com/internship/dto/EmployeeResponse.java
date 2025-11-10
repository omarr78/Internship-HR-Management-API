package com.internship.dto;

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
    private String name;
    private LocalDate dateOfBirth;
    private LocalDate graduationDate;
    private Gender gender;
    private float salary;
    private Long departmentId;
    private Long teamId;
    private Long managerId;
    private List<String> expertises;
}
