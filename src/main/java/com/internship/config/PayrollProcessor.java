package com.internship.config;

import com.internship.entity.*;
import com.internship.repository.BonusRepository;
import com.internship.repository.EmployeeSalaryRepository;
import com.internship.repository.LeaveRepository;
import com.internship.repository.PayrollRepository;
import com.internship.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollProcessor implements ItemProcessor<Employee, Payroll> {
    static final Integer WORKING_DAY_IN_MONTH = 30;
    private static final BigDecimal INSURANCE_AMOUNT = BigDecimal.valueOf(500);
    private static final BigDecimal TAX_RATIO = BigDecimal.valueOf(0.15);

    private final BonusRepository bonusRepository;
    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final PayrollRepository payrollRepository;
    // maps to store pre-fetched data
    private Map<Long, List<Bonus>> bonusMap = new HashMap<>();
    private Map<Long, List<Leave>> leaveMap = new HashMap<>();
    private Map<Long, BigDecimal> salaryMap = new HashMap<>();
    private Set<Long> payrollExists = new HashSet<>();

    private int month;
    private int year;

    // this called once per chunk
    public void loadChunkData(List<Long> employeeIds) {
        // we're generating payroll for the prev month
        LocalDate today = LocalDate.now().minusMonths(1);
        this.month = today.getMonthValue();
//        this.month = 12;
        this.year = today.getYear();
        // just for testing
//        this.year = 2025;

        this.bonusMap = getBonusesPerChunkInSpecificDateRange(employeeIds, month, year);
        this.leaveMap = getLeavesPerChunkInSpecificDateRange(employeeIds, month, year);
        this.salaryMap = getSalariesPerChunk(employeeIds);
        this.payrollExists = getExistsEmployeePayrollPerChunk(employeeIds);
    }

    @Override
    public Payroll process(@NonNull Employee employee) {
        // Check if payroll exists for employee/year/month
        boolean alreadyExists = payrollExists.contains(employee.getId());

        // if it exists return null
        // Returning null in a Spring Batch Processor tells the framework to "filter" this item out.
        if (alreadyExists) {
            log.info("Skipping Employee {} - Payroll already exists.", employee.getId());
            return null;
        }

        // Otherwise generate payroll for this employee
        BigDecimal grossSalary = salaryMap.get(employee.getId());
        BigDecimal bonus = calculateBonus(employee.getId());
        BigDecimal leavesDeduction = calculateLeavesDeduction(employee, grossSalary);
        BigDecimal taxAmount = grossSalary.multiply(TAX_RATIO);
        BigDecimal netSalary = grossSalary.subtract(taxAmount.add(INSURANCE_AMOUNT).add(leavesDeduction)).add(bonus);

//        log.info("Employee {} - Gross: {}, Bonus: {}, Tax: {}, Leaves: {}, Net: {}",
//                employee.getId(), grossSalary, bonus, taxAmount, leavesDeduction, netSalary);

        return Payroll.builder()
                .payrollYear(year)
                .payrollMonth(month)
                .grossSalary(grossSalary)
                .bonus(bonus)
                .taxAmount(taxAmount)
                .insuranceDeduction(INSURANCE_AMOUNT)
                .leavesDeduction(leavesDeduction)
                .netSalary(netSalary)
                .employee(employee)
                .build();
    }

    private BigDecimal calculateBonus(Long employeeId) {
        return bonusMap.getOrDefault(employeeId, List.of())
                .stream()
                .map(Bonus::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateLeavesDeduction(Employee employee, BigDecimal grossSalary) {
        int maxLeaveDays = employeeService.getTheNumberOfLeaveDays(employee.getJoinedDate());
        List<Leave> leaves = leaveMap.getOrDefault(employee.getId(), List.of())
                .stream()
                .sorted(Comparator.comparing(Leave::getLeaveDate))
                .toList();

        int deductionDays = 0;
        int leaveCounter = 0;
        for (Leave leave : leaves) {
            leaveCounter++;
            if (leaveCounter > maxLeaveDays && leave.getLeaveDate().getMonthValue() == month) {
                deductionDays++;
            }
        }

        return grossSalary
                .divide(BigDecimal.valueOf(WORKING_DAY_IN_MONTH), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(deductionDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Map<Long, List<Bonus>> getBonusesPerChunkInSpecificDateRange(List<Long> ids, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        return bonusRepository
                .findByEmployeeIdInAndBonusDateBetween(ids, start, end)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getEmployee().getId()));
    }

    private Map<Long, List<Leave>> getLeavesPerChunkInSpecificDateRange(List<Long> ids, int month, int year) {
        LocalDate start = YearMonth.of(year, 1).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        return leaveRepository.findByEmployeeIdInAndLeaveDateBetween(ids, start, end)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getEmployee().getId()));
    }

    private Map<Long, BigDecimal> getSalariesPerChunk(List<Long> ids) {
        return employeeSalaryRepository.findGrossSalaryByEmployeeIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getEmployee().getId(),
                        EmployeeSalary::getGrossSalary
                ));
    }

    private Set<Long> getExistsEmployeePayrollPerChunk(List<Long> employeeIds) {
        return payrollRepository.findByEmployeeIdInAndPayrollYearAndPayrollMonth(employeeIds, year, month)
                .stream()
                .map(payroll -> payroll.getEmployee().getId())
                .collect(Collectors.toSet());
    }
}
