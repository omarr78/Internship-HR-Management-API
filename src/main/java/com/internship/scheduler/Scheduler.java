package com.internship.scheduler;

import com.internship.service.PayrollService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class Scheduler {
    private final PayrollService payrollService;

    // At 12:00 AM, on day 1 of the month
    @Scheduled(cron = "0 0 0 1 * ?")
    public void startGenerationPayroll() {
        payrollService.generatePayroll();
    }
}
