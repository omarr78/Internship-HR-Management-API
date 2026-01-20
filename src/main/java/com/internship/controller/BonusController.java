package com.internship.controller;

import com.internship.dto.CreateBonusRequest;
import com.internship.dto.CreateBonusResponse;
import com.internship.entity.Bonus;
import com.internship.entity.Employee;
import com.internship.repository.BonusRepository;
import com.internship.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
public class BonusController {
    private final BonusRepository bonusRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    public ResponseEntity<CreateBonusResponse> createBonus(@RequestBody @Valid final CreateBonusRequest request) {
        Long id = request.getEmployeeId();
        Employee employee = employeeRepository.findById(id).get();
        Bonus bonus;
        if (request.getBonusDate() == null) {
            bonus = new Bonus(LocalDate.now(), request.getAmount(), employee);
        } else {
            bonus = new Bonus(request.getBonusDate(), request.getAmount(), employee);
        }
        bonusRepository.save(bonus);
        CreateBonusResponse response = CreateBonusResponse.builder()
                .id(bonus.getId())
                .bonusDate(bonus.getBonusDate())
                .employeeId(bonus.getId())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
