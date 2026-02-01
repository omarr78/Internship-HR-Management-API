package com.internship.controller;

import com.internship.dto.CreateBonusRequest;
import com.internship.dto.CreateBonusResponse;
import com.internship.service.BonusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
public class BonusController {
    private final BonusService bonusService;

    @PostMapping
    public ResponseEntity<CreateBonusResponse> createBonus(@RequestBody @Valid final CreateBonusRequest request) {
        CreateBonusResponse response = bonusService.addBonus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
