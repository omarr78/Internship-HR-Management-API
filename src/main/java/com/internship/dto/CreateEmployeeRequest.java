package com.internship.dto;

import com.internship.enums.Gender;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeRequest {
    private String name;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    private Gender gender;
    private Long departmentId;
    private Long teamId;
    private Long managerId;
    private float salary;
}