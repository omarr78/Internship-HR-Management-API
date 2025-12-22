package com.internship.service;

import com.internship.dto.EmployeeResponse;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public List<EmployeeResponse> getMembers(Long id) {
        return employeeRepository.findByTeamId(id)
                .stream().map(employeeMapper::toResponse).toList();
    }
}
