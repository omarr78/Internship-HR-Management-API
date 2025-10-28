package com.internship.service.interfac;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.UpdateEmployeeRequest;

public interface EmployeeService {
    EmployeeResponse addEmployee(CreateEmployeeRequest request);
    EmployeeResponse modifyEmployee(UpdateEmployeeRequest request, Long id);
    void removeEmployee(Long id);
}
