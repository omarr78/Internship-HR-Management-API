ALTER TABLE employees
    MODIFY COLUMN gross_salary decimal(15, 2) NOT NULL;

ALTER TABLE bonuses
    MODIFY COLUMN amount decimal(15, 2) NOT NULL;
