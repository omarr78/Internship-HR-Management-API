package com.internship.service;

import com.internship.dto.EmployeeResponse;
import com.internship.exception.BusinessException;
import com.internship.mapper.EmployeeMapper;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.internship.exception.ApiError.TEAM_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeService employeeService;

    public List<EmployeeResponse> getMembers(Long id) {
        teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(TEAM_NOT_FOUND,
                        "Team not found with id: " + id));

        return employeeRepository.findByTeamId(id)
                .stream()
                .map(employee ->
                        employeeMapper.toResponse(
                                employee,
                                employeeService.calculateYearsOfExperience(
                                        employee.getPastExperienceYear(),
                                        employee.getJoinedDate()
                                ),
                                employeeService.getTheNumberOfLeaveDays(
                                        employee.getJoinedDate()
                                )
                        )
                )
                .toList();
    }
}
