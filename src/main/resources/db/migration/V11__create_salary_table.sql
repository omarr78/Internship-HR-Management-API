CREATE TABLE employee_salaries
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    creation_date DATETIME              NOT NULL DEFAULT (CURRENT_DATE),
    gross_salary  DECIMAL(15, 2)        NOT NULL,
    reason        VARCHAR(255)          NOT NULL,
    employee_id   BIGINT                NOT NULL,
    CONSTRAINT pk_employee_salaries PRIMARY KEY (id),
    CONSTRAINT fk_employee_on_employee_salaries FOREIGN KEY (employee_id) REFERENCES employees (id)
);

ALTER TABLE employees
    DROP COLUMN GROSS_SALARY;