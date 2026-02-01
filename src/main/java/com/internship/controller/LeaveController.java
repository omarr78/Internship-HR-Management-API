package com.internship.controller;

import com.internship.dto.CreateLeaveRequest;
import com.internship.dto.CreateLeaveResponse;
import com.internship.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<List<CreateLeaveResponse>> createLeave(@RequestBody @Valid final CreateLeaveRequest request) {
        List<CreateLeaveResponse> response = leaveService.addLeave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
