package com.internship.dto;

import com.internship.enums.Degree;
import com.internship.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
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
    private String firstName;

    @Size(min = 3, message = "name must not be empty and at least has 3 characters")
    private String lastName;

    private String nationalId;
    private Degree degree;

    @Min(value = 0, message = "pastExperienceYear must be greater than or equal to 0")
    private Integer pastExperienceYear;
    private LocalDate joinedDate;

    @Past
    private LocalDate dateOfBirth;
    private LocalDate graduationDate;
    private Gender gender;

    @Min(value = 0, message = "salary must be greater than or equal to 0")
    private Float grossSalary;

    private Long departmentId;
    private Long teamId;
    private Optional<Long> managerId;
    private List<String> expertises;
}
