package com.internship.controller;

import com.internship.dto.EmployeeResponse;
import com.internship.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService service;

    @GetMapping("{id}/members")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesUnderTeam(@PathVariable final Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.getMembers(id));
    }
}
