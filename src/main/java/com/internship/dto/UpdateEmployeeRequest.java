package com.internship.dto;

import com.internship.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmployeeRequest {
    @Size(min = 2, message = "name must not be empty and at least has 2 characters")
    private String name;

    @Past
    private LocalDate dateOfBirth;

    @Past
    private LocalDate graduationDate;

    private Gender gender;

    private Long departmentId;

    private Long teamId;

    private Long managerId;

    @Min(0)
    private float salary;

    private List<String> expertises;
}