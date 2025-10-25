package com.internship.service.interfac;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.entity.Employee;

public interface EmployeeService {
    Employee addEmployee(CreateEmployeeRequest request);
}
