package com.internship.dto;

import com.internship.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmployeeRequest {
    @Size(min = 3, message = "name must not be empty and at least has 3 characters")
    private String name;
    @Past
    private LocalDate dateOfBirth;
    private LocalDate graduationDate;
    private Gender gender;
    @Min(value = 0, message = "salary must be greater than or equal to 0")
    private float salary;
    private Long departmentId;
    private Long teamId;
    private Optional<Long> managerId;
    private List<String> expertises;
}
