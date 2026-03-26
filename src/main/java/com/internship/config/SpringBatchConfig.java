package com.internship.config;

import com.internship.entity.Employee;
import com.internship.entity.Payroll;
import com.internship.repository.EmployeeRepository;
import com.internship.repository.PayrollRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SpringBatchConfig {
    private static final int CHUNK_SIZE = 500;

    @Bean
    public RepositoryItemReader<Employee> itemReader(EmployeeRepository employeeRepository) {
        return new RepositoryItemReaderBuilder<Employee>()
                .name("employeeReader")
                .repository(employeeRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public RepositoryItemWriter<Payroll> itemWriter(PayrollRepository payrollRepository) {
        return new RepositoryItemWriterBuilder<Payroll>()
                .repository(payrollRepository)
                .build();
    }

    @Bean
    public Step generatePayroll(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Employee> reader,
            PayrollProcessor processor,
            RepositoryItemWriter<Payroll> writer,
            EntityManager entityManager
    ) {
        List<Long> buffer = new ArrayList<>();
        AtomicBoolean loaded = new AtomicBoolean(false);
        AtomicInteger batchCounter = new AtomicInteger(0);

        return new StepBuilder("generatePayrollStep", jobRepository)
                .<Employee, Payroll>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)

                // Collect employee while reading
                .listener(new ItemReadListener<>() {
                    @Override
                    public void afterRead(@NonNull Employee employee) {
                        buffer.add(employee.getId());
                    }
                })

                // Load data before processing
                .listener(new ItemProcessListener<>() {
                    @Override
                    public void beforeProcess(@NonNull Employee item) {
                        if (!loaded.get()) {
                            processor.loadChunkData(buffer);
                            loaded.set(true);
                        }
                    }
                })

                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(@NonNull ChunkContext context) {
                        buffer.clear();
                        loaded.set(false);
                        // Increment and log at the start of chunk
                        int currentBatch = batchCounter.incrementAndGet();
                        log.info("Starting Batch #{}", currentBatch);
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext context) {
                        // Log completion
                        log.info("Finished Batch #{}", batchCounter.get());
                        entityManager.clear();
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext context) {
                        log.error("Error occurred in Batch #{}", batchCounter.get());
                    }
                })
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job runJob(JobRepository jobRepository, Step generatePayroll) {
        return new JobBuilder("generatePayrollJob", jobRepository)
                .start(generatePayroll)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(@NonNull JobExecution jobExecution) {
                        jobExecution.getStepExecutions().forEach(stepExecution -> {
                            log.info("--- Step Summary: {} ---", stepExecution.getStepName());
                            log.info("Read Count:    {}", stepExecution.getReadCount());
                            log.info("Write Count:   {}", stepExecution.getWriteCount());
                            log.info("Filter Count:  {}", stepExecution.getFilterCount());
                            log.info("Skip Count:    {}", stepExecution.getSkipCount());
                            log.info("Exit Status:   {}", stepExecution.getExitStatus().getExitCode());
                        });
                    }
                })
                .build();
    }
}
