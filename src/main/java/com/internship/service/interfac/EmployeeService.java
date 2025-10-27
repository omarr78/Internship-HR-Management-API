package com.internship.service.interfac;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;

public interface EmployeeService {
    EmployeeResponse addEmployee(CreateEmployeeRequest request);
}
