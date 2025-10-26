package com.internship.dto;

import com.internship.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateEmployeeRequest {
    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "date of birth required")
    private LocalDate dateOfBirth;

    @NotNull(message = "graduation date required")
    private LocalDate graduationDate;

    @NotNull(message = "gender required")
    private Gender gender;

    @NotNull(message = "departmentId required")
    private Long departmentId;

    @NotNull(message = "teamId required")
    private Long teamId;

    private Long managerId;

    @Min(0)
    private float salary;

    private List<String> expertises;
}