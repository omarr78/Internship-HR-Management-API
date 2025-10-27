package com.internship.dto;

import com.internship.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmployeeRequest {

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