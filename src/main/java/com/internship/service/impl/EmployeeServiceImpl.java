package com.internship.service.impl;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.EmployeeResponse;
import com.internship.entity.Department;
import com.internship.entity.Employee;
import com.internship.entity.Expertise;
import com.internship.entity.Team;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.DepartmentRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.ExpertiseRepository;
import com.internship.repository.TeamRepository;
import com.internship.service.interfac.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.internship.exception.ApiError.*;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final ExpertiseRepository expertiseRepository;

    @Override
    @Transactional
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(DEPARTMENT_NOT_FOUND,
                        "Department not found with id: " + request.getDepartmentId()));

        Team team = teamRepository.findById(request.getTeamId()).
                orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                        "Team not found with id: " + request.getTeamId()));

        Employee manager = null;
        if(request.getManagerId() != null){
            manager = employeeRepository.findById(request.getManagerId()).
                    orElseThrow(() -> new BusinessException(EMPLOYEE_NOT_FOUND,
                            "Manager not found with id: " + request.getManagerId()));
        }
        List<Expertise> expertises = new ArrayList<>();

        if(request.getExpertises() != null){
            for(String expertise:request.getExpertises()){
                Optional<Expertise> optional = expertiseRepository.findExpertiseByName(expertise);
                if(optional.isPresent()){
                    expertises.add(optional.get());
                }
                else{
                    Expertise exp = Expertise.builder().name(expertise).build();
                    Expertise savedExpertise = expertiseRepository.save(exp);
                    expertises.add(savedExpertise);
                }
            }
        }

        Employee employee = employeeMapper.toEmployee(request,department,team,manager,expertises);
        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toResponse(savedEmployee);
    }
}
