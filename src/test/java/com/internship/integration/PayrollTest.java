package com.internship.integration;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.entity.Payroll;
import com.internship.exception.ApiError;
import com.internship.exception.BusinessException;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.LeaveRepository;
import com.internship.repository.PayrollRepository;
import com.internship.service.PayrollService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DBRider
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false"
})
public class PayrollTest {
    static final LocalDate START_OF_JAN = LocalDate.of(2020, 1, 1);
    static final LocalDate START_OF_FEB = LocalDate.of(2020, 2, 1);
    static final LocalDate FIXED_DATE = LocalDate.of(2020, 3, 1);
    static final Integer PAYROLL_YEAR = 2020;
    static final Integer PAYROLL_MONTH = 2;
    static final Integer WORKING_DAY_IN_MONTH = 30;
    private static final Long EXISTENT_EMPLOYEE1_ID = 1L;
    private static final Long EXISTENT_EMPLOYEE2_ID = 2L;
    private static final Long EXISTENT_EMPLOYEE3_ID = 3L;
    private static final Long EXISTENT_EMPLOYEE4_ID = 4L;
    private static final BigDecimal INSURANCE_AMOUNT = BigDecimal.valueOf(500);
    private static final BigDecimal TAX_RATIO = BigDecimal.valueOf(0.15);

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRepository leaveRepository;
    @Autowired
    private PayrollService payrollService;
    @Autowired
    private PayrollRepository payrollRepository;

    private List<Leave> generateLeaves(LocalDate startDate, int leaveDays, Employee employee) {
        List<Leave> leaves = new ArrayList<>();
        LocalDate currentDate = startDate;
        for (int i = 0; i < leaveDays; i++) {
            if (currentDate.getDayOfWeek() == DayOfWeek.FRIDAY
                    || currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                currentDate = currentDate.plusDays(1);
            }
            leaves.add(new Leave(currentDate, employee));
            currentDate = currentDate.plusDays(1);
        }
        return leaves;
    }

    private BigDecimal calculateTax(BigDecimal grossSalary) {
        // tax is 15% from employee salary
        // tax = gross salary * tax ratio
        return grossSalary.multiply(TAX_RATIO);
    }

    private BigDecimal calculateLeavesDeduction(BigDecimal grossSalary, Integer deductedLeaveDays) {
        // total leaves deduction = gross salary / Number of working days in month * deducted leave days
        return grossSalary
                .divide(BigDecimal.valueOf(WORKING_DAY_IN_MONTH), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(deductedLeaveDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNetSalary(BigDecimal grossSalary, BigDecimal tax,
                                          BigDecimal leavesDeduction, BigDecimal bonus) {
        // net salary = gross salary - (tax + insurance amount + leaves deduction) + bonus
        return grossSalary
                .subtract(tax.add(INSURANCE_AMOUNT).add(leavesDeduction))
                .add(bonus);
    }

    @Test
    @DataSet(value = "dataset/employees_payroll.xml", cleanBefore = true, cleanAfter = true)
    public void testGenerateEmployeePayroll_shouldSuccessAndPersistPayrollDetails() {
        // Suppose we are at in 2020-03-01, and we generate EmployeePayroll for Feb month
        //==========================================================================
        /*
         employee 1 has 21 leave days
         Takes 20 leave in Jan, and takes 1 leave in Feb -- so no leave deduction in the current month
         And there is no bonuses in the current month
        */
        Employee employee1 = employeeRepository.findById(EXISTENT_EMPLOYEE1_ID).get();
        final List<Leave> leavesEmployee1 = new ArrayList<>();
        leavesEmployee1.addAll(generateLeaves(START_OF_JAN, 20, employee1));
        leavesEmployee1.addAll(generateLeaves(START_OF_FEB, 1, employee1));
        leaveRepository.saveAllAndFlush(leavesEmployee1);
        //==========================================================================
        /*
         employee 2 has 21 leave days
         Takes 20 leave in Jan, and takes 5 leave in Feb -- so there are 4 deducted leave days in the current month
         he has one bonus 1500.00 in the current month
        */
        Employee employee2 = employeeRepository.findById(EXISTENT_EMPLOYEE2_ID).get();
        final List<Leave> leavesEmployee2 = new ArrayList<>();
        leavesEmployee2.addAll(generateLeaves(START_OF_JAN, 20, employee2));
        leavesEmployee2.addAll(generateLeaves(START_OF_FEB, 5, employee2));
        leaveRepository.saveAllAndFlush(leavesEmployee2);
        //==========================================================================
        /*
         employee 3 has 21 leave days
         Takes 22 leave in Jan, and takes 0 leave in Feb -- so no leave deduction in the current month
         And there is no bonuses in the current month
        */
        Employee employee3 = employeeRepository.findById(EXISTENT_EMPLOYEE3_ID).get();
        final List<Leave> leavesEmployee3 = new ArrayList<>(generateLeaves(START_OF_JAN, 22, employee3));
        leaveRepository.saveAllAndFlush(leavesEmployee3);
        //==========================================================================
        /*
         employee 4 has 30 leave days
         Takes 20 leave in Jan, and takes 15 leave in Feb -- so there are 5 deducted leave days in the current month
         And there is 2 bonuses in the current month 1100.00, and 900.00
        */
        Employee employee4 = employeeRepository.findById(EXISTENT_EMPLOYEE4_ID).get();
        final List<Leave> leavesEmployee4 = new ArrayList<>();
        leavesEmployee4.addAll(generateLeaves(START_OF_JAN, 20, employee4));
        leavesEmployee4.addAll(generateLeaves(START_OF_FEB, 15, employee4));
        leaveRepository.saveAllAndFlush(leavesEmployee4);
        //==========================================================================
        List<Payroll> employeePayrollBefore = payrollRepository.findAll();
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(FIXED_DATE);

            // Generate employeePayroll
            payrollService.generatePayroll();

            // Assert on database
            List<Payroll> employeePayrollAfter = payrollRepository.findAll();
            List<Payroll> insertedEmployeePayroll = employeePayrollAfter.stream()
                    .filter(ep -> !employeePayrollBefore.contains(ep)).toList();

            assertEquals(4, insertedEmployeePayroll.size());

            BigDecimal expectedEmployeePayrollGrossSalary;
            BigDecimal expectedEmployeePayrollBonus;
            BigDecimal expectedEmployeePayrollTax;
            BigDecimal expectedEmployeePayrollLeaveDeduction;
            BigDecimal expectedEmployeePayrollNetSalary;

            List<BigDecimal> employeeBonuses = List.of(
                    BigDecimal.valueOf(0), // employee1
                    BigDecimal.valueOf(1500), // employee2
                    BigDecimal.valueOf(0), // employee3
                    BigDecimal.valueOf(900 + 1100) // employee4 has 2 bonuses
            );

            List<Integer> employeeDeductedLeavesDay = List.of(0, 4, 0, 5);

            for (int i = 0; i < 4; i++) {
                final Long employeeId = i + 1L;
                Payroll employeePayroll = insertedEmployeePayroll.stream()
                        .filter(ep -> ep.getEmployee().getId().equals(employeeId))
                        .findFirst().orElseThrow();

                assertEquals(PAYROLL_YEAR, employeePayroll.getPayrollYear());
                assertEquals(PAYROLL_MONTH, employeePayroll.getPayrollMonth());

                expectedEmployeePayrollGrossSalary = employeePayroll.getGrossSalary();
                assertEquals(0, expectedEmployeePayrollGrossSalary.compareTo(employeePayroll.getGrossSalary()));

                expectedEmployeePayrollBonus = employeeBonuses.get(i);
                assertEquals(0, expectedEmployeePayrollBonus.compareTo(employeePayroll.getBonus()));

                expectedEmployeePayrollTax = calculateTax(expectedEmployeePayrollGrossSalary);
                assertEquals(0, expectedEmployeePayrollTax.compareTo(employeePayroll.getTaxAmount()));

                assertEquals(0, INSURANCE_AMOUNT.compareTo(employeePayroll.getInsuranceDeduction()));

                expectedEmployeePayrollLeaveDeduction =
                        calculateLeavesDeduction(expectedEmployeePayrollGrossSalary, employeeDeductedLeavesDay.get(i));

                assertEquals(0, expectedEmployeePayrollLeaveDeduction
                        .compareTo(employeePayroll.getLeavesDeduction()));

                expectedEmployeePayrollNetSalary =
                        calculateNetSalary(expectedEmployeePayrollGrossSalary, expectedEmployeePayrollTax,
                                expectedEmployeePayrollLeaveDeduction, expectedEmployeePayrollBonus);
                assertEquals(0, expectedEmployeePayrollNetSalary.compareTo(employeePayroll.getNetSalary()));

                assertEquals(employeeId, employeePayroll.getEmployee().getId());
            }
        }
    }

    @Test
    @DataSet(value = "dataset/employees_payroll.xml", cleanBefore = true, cleanAfter = true)
    public void testDuplicationGenerateSameEmployeePayrollForSameMonthAndYear_shouldFailAndReturnConflict() {
        // First generation → should succeed
        payrollService.generatePayroll();

        // Second generation → should throw exception
        BusinessException exception = assertThrows(BusinessException.class, () -> payrollService.generatePayroll());
        assertEquals(
                ApiError.DUPLICATE_PAYROLL_EXCEPTION.getDefaultMessage(),
                exception.getMessage()
        );
    }
}