package com.internship.service.interfac;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.CreateEmployeeResponse;

public interface EmployeeService {
    CreateEmployeeResponse addEmployee(CreateEmployeeRequest request);
}
