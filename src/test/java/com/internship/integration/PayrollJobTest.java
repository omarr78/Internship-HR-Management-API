package com.internship.integration;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import com.internship.entity.Employee;
import com.internship.entity.Leave;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.LeaveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SpringBatchTest
@DBRider
@TestExecutionListeners(
        // Explicitly Register Listeners
        // Forces DBRider to run early enough
        // Prevents Spring Batch from initializing before data is inserted
        listeners = {
                com.github.database.rider.spring.DBRiderTestExecutionListener.class,
                org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
                org.springframework.test.context.transaction.TransactionalTestExecutionListener.class,
                org.springframework.test.context.support.DirtiesContextTestExecutionListener.class
        },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false"
})
public class PayrollJobTest {
    static final LocalDate START_OF_JAN = LocalDate.of(2020, 1, 1);
    static final LocalDate START_OF_FEB = LocalDate.of(2020, 2, 1);
    static final LocalDate START_OF_MAR = LocalDate.of(2020, 3, 1);
    static final LocalDate FIXED_DATE = LocalDate.of(2020, 3, 1);
    private static final Long EXISTENT_EMPLOYEE1_ID = 1L;
    private static final Long EXISTENT_EMPLOYEE2_ID = 2L;
    private static final Long EXISTENT_EMPLOYEE3_ID = 3L;
    private static final Long EXISTENT_EMPLOYEE4_ID = 4L;
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    @Autowired
    private Job generatePayrollJob;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRepository leaveRepository;

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

    @BeforeEach
    void setup() {
        jobLauncherTestUtils.setJob(generatePayrollJob);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        // Clears BATCH_JOB_EXECUTION, BATCH_STEP_EXECUTION, etc.
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    // since this test is not transactional because spring batch so we need to clean before and after test
    @DataSet(
            value = "dataset/employees_payroll.xml",
            cleanBefore = true, cleanAfter = true,
            skipCleaningFor = {"flyway_schema_history"}
    )
    @ExpectedDataSet(value = "dataset/expected_payroll.xml", ignoreCols = {"id"})
    public void testGenerateEmployeePayroll_shouldSuccessAndPersistPayrollDetails() throws Exception {
        // generate leaves for employees
        Employee employee1 = employeeRepository.findById(EXISTENT_EMPLOYEE1_ID).get();
        final List<Leave> leavesEmployee1 = new ArrayList<>();
        leavesEmployee1.addAll(generateLeaves(START_OF_MAR, 5, employee1));
        leavesEmployee1.addAll(generateLeaves(START_OF_FEB, 1, employee1));
        leavesEmployee1.addAll(generateLeaves(START_OF_JAN, 20, employee1));
        leaveRepository.saveAll(leavesEmployee1);

        Employee employee2 = employeeRepository.findById(EXISTENT_EMPLOYEE2_ID).get();
        final List<Leave> leavesEmployee2 = new ArrayList<>();
        leavesEmployee2.addAll(generateLeaves(START_OF_MAR, 5, employee2));
        leavesEmployee2.addAll(generateLeaves(START_OF_FEB, 5, employee2));
        leavesEmployee2.addAll(generateLeaves(START_OF_JAN, 20, employee2));
        leaveRepository.saveAll(leavesEmployee2);

        Employee employee3 = employeeRepository.findById(EXISTENT_EMPLOYEE3_ID).get();
        final List<Leave> leavesEmployee3 = new ArrayList<>(generateLeaves(START_OF_JAN, 22, employee3));
        leaveRepository.saveAll(leavesEmployee3);

        Employee employee4 = employeeRepository.findById(EXISTENT_EMPLOYEE4_ID).get();
        final List<Leave> leavesEmployee4 = new ArrayList<>();
        leavesEmployee4.addAll(generateLeaves(START_OF_MAR, 5, employee4));
        leavesEmployee4.addAll(generateLeaves(START_OF_FEB, 15, employee4));
        leavesEmployee4.addAll(generateLeaves(START_OF_JAN, 20, employee4));
        leaveRepository.saveAll(leavesEmployee4);

        // Suppose we are at in 2020-03-01, and we generate EmployeePayroll for Feb month
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(FIXED_DATE);

            // Prepare Job Parameters (February 2020)
            JobParameters params = new JobParametersBuilder()
                    .addLong("runTime", System.currentTimeMillis()) // Just unique run ID
                    .toJobParameters();

            // Run the Job
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);

            // Assert Job Status
            assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

            // DB Rider @ExpectedDataSet will now automatically verify
        }
    }
}
