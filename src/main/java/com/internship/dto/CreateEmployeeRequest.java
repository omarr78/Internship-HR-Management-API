package com.internship.dto;

import com.internship.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateEmployeeRequest {
    @NotBlank(message = "name is required")
    @Size(min = 3, message = "name must not be empty and at least has 3 characters")
    private String name;

    @NotNull(message = "date of birth required")
    @Past
    private LocalDate dateOfBirth;

    @NotNull(message = "graduation date required")
    private LocalDate graduationDate;

    @NotNull(message = "gender required")
    private Gender gender;

    @Min(0)
    private float salary;
}
