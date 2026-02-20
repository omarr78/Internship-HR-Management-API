package com.internship.service;

import com.internship.entity.Bonus;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.entity.Payroll;
import com.internship.exception.BusinessException;
import com.internship.repository.BonusRepository;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.LeaveRepository;
import com.internship.repository.PayrollRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static com.internship.exception.ApiError.DUPLICATE_PAYROLL_EXCEPTION;

@Service
@RequiredArgsConstructor
public class PayrollService {
    static final Integer WORKING_DAY_IN_MONTH = 30;
    private static final BigDecimal INSURANCE_AMOUNT = BigDecimal.valueOf(500);
    private static final BigDecimal TAX_RATIO = BigDecimal.valueOf(0.15);
    private final EmployeeRepository employeeRepository;
    private final BonusRepository bonusRepository;
    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;
    private final PayrollRepository payrollRepository;

    @Transactional
    public void generatePayroll() {
        // we're generating payroll for the prev month
        LocalDate today = LocalDate.now().minusMonths(1);

        int month = today.getMonthValue();
        int year = today.getYear();

        List<Employee> employees = employeeRepository.findAll();
        List<Payroll> employeePayroll = new ArrayList<>();

        for (Employee employee : employees) {
            BigDecimal grossSalary = employee.getGrossSalary();
            BigDecimal bonus = calculateBonusOfEmployeeInSpecificMonthAndYear(employee, month, year);
            BigDecimal taxAmount = grossSalary.multiply(TAX_RATIO);
            BigDecimal leavesDeduction =
                    calculateLeavesDeductionOfEmployeeInSpecificMonthAndYear(employee, month, year);
            BigDecimal netSalary =
                    grossSalary.subtract(taxAmount.add(INSURANCE_AMOUNT).add(leavesDeduction)).add(bonus);

            Payroll payroll = Payroll.builder()
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

            employeePayroll.add(payroll);
        }
        try {
            payrollRepository.saveAll(employeePayroll);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(DUPLICATE_PAYROLL_EXCEPTION);
        }
    }

    private BigDecimal calculateBonusOfEmployeeInSpecificMonthAndYear(final Employee employee, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Bonus> bonuses = bonusRepository.findByEmployeeIdAndBonusDateBetween(employee.getId(), start, end);
        return bonuses.stream().map(Bonus::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateLeavesDeductionOfEmployeeInSpecificMonthAndYear(final Employee employee,
                                                                                int month, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        int maxLeaveDays = employeeService.getTheNumberOfLeaveDays(employee.getJoinedDate());
        List<Leave> leaves = leaveRepository.findByEmployeeIdAndLeaveDateBetween(employee.getId(), start, end);
        int numberOfLeavesDeduction = 0;
        int leaveCounter = 0;
        for (Leave leave : leaves) {
            leaveCounter++;
            if (leaveCounter > maxLeaveDays && leave.getLeaveDate().getMonthValue() == month) {
                numberOfLeavesDeduction++;
            }
        }
        // total leaves deduction = gross salary / Number of working days in month * deducted leave days
        return employee.getGrossSalary()
                .divide(BigDecimal.valueOf(WORKING_DAY_IN_MONTH), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(numberOfLeavesDeduction))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
