CREATE TABLE employee_salaries
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    date         date                  NOT NULL,
    gross_salary DECIMAL(15, 2)        NOT NULL,
    reason       VARCHAR(255)          NOT NULL,
    employee_id  BIGINT                NOT NULL,
    CONSTRAINT pk_employee_salaries PRIMARY KEY (id)
);