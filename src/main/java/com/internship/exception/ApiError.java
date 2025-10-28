package com.internship.exception;

import org.springframework.http.HttpStatus;

public enum ApiError {

    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Department not found"),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "Team not found"),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "Employee not found"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    SELF_MANAGEMENT(HttpStatus.BAD_REQUEST, "Employee cannot be self management"),
    INVALID_EMPLOYEE_REMOVAL(HttpStatus.BAD_REQUEST, "Cannot remove manager (employee has subordinates) has no manager");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ApiError(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
