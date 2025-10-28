package com.internship.service.interfac;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.dto.UpdateEmployeeRequest;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse addEmployee(CreateEmployeeRequest request);
    EmployeeResponse modifyEmployee(UpdateEmployeeRequest request, Long id);
    void removeEmployee(Long id);
    EmployeeResponse getEmployee(Long id);
    float getEmployeeSalaryInfo(Long id);
    List<EmployeeResponse> getAllEmployeesUnderManager(Long id);
}
