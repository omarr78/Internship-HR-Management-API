package com.internship.dto;

import com.internship.enums.Gender;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeDtoInterface {
    Long getId();

    String getName();

    LocalDate getDateOfBirth();

    LocalDate getGraduationDate();

    Gender getGender();

    float getSalary();

    Long getDepartmentId();

    Long getTeamId();

    Long getManagerId();

    List<String> getExpertises();
}
