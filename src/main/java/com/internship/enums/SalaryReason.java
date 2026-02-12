package com.internship.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SalaryReason {
    INITIAL_BASE_SALARY("Initial base salary"),
    SALARY_UPDATED("Salary updated"),
    SALARY_RAISED("Salary raised");
    private final String message;
}