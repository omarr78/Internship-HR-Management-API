CREATE TABLE employee_payroll
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_year        INT     NOT NULL,
    payroll_month       INT     NOT NULL,
    gross_salary        DECIMAL NOT NULL,
    bonus               DECIMAL NOT NULL,
    tax_amount          DECIMAL NOT NULL,
    insurance_deduction DECIMAL NOT NULL,
    leaves_deduction    DECIMAL NOT NULL,
    net_salary          DECIMAL NOT NULL,
    employee_id         BIGINT  NOT NULL,

    CONSTRAINT uq_payroll UNIQUE (employee_id, payroll_year, payroll_month),
    CONSTRAINT fk_employee_on_payroll
        FOREIGN KEY (employee_id)
            REFERENCES employees (id)
);