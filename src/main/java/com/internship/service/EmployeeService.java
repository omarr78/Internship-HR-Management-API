package com.internship.service;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.UpdateEmployeeRequest;

public interface EmployeeService {
    EmployeeResponse addEmployee(CreateEmployeeRequest request);
    EmployeeResponse modifyEmployee(UpdateEmployeeRequest request, Long id);
}
