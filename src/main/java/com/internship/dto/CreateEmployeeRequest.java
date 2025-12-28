package com.internship.dto;

import com.internship.enums.Degree;
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
public class CreateEmployeeRequest {
    @NotBlank(message = "name is required")
    @Size(min = 3, message = "name must not be empty and at least has 3 characters")
    private String firstName;

    @Size(min = 3, message = "name must not be empty and at least has 3 characters")
    private String lastName;

    @NotBlank(message = "nationalId is required")
    private String nationalId;

    @NotNull(message = "degree is required")
    private Degree degree;

    @NotNull(message = "pastExperienceYear is required")
    @Min(value = 0, message = "pastExperienceYear must be greater than or equal to 0")
    private Integer pastExperienceYear;

    @NotNull(message = "joinedDate is required")
    private LocalDate joinedDate = LocalDate.now();

    @NotNull(message = "date of birth required")
    @Past
    private LocalDate dateOfBirth;

    @NotNull(message = "graduation date required")
    private LocalDate graduationDate;

    @NotNull(message = "gender required")
    private Gender gender;

    @Min(0)
    private float grossSalary;

    @NotNull(message = "departmentId required")
    private Long departmentId;

    @NotNull(message = "teamId required")
    private Long teamId;

    private Long managerId;
    private List<String> expertises;
}
