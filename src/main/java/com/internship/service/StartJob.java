package com.internship.service;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartJob implements CommandLineRunner {
    private final PayrollService payrollService;

    @Override
    public void run(String... args) throws Exception {
        payrollService.generatePayroll();
    }
}
