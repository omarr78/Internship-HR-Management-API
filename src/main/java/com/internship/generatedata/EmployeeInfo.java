package com.internship.generatedata;

import com.internship.enums.Degree;

import java.time.LocalDate;

public record EmployeeInfo(Long id, Degree degree, LocalDate joinedDate) {
}
