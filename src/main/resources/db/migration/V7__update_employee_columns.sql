-- Rename existing columns
ALTER TABLE employees
    RENAME COLUMN name TO first_name;

ALTER TABLE employees
    RENAME COLUMN salary TO gross_salary;

-- Add new columns
ALTER TABLE employees
    ADD COLUMN last_name VARCHAR(255);
ALTER TABLE employees
    ADD COLUMN national_id VARCHAR(30);
ALTER TABLE employees
    ADD COLUMN degree VARCHAR(50) NOT NULL DEFAULT 'NOT_SET';
ALTER TABLE employees
    ADD COLUMN past_experience_year INT NOT NULL DEFAULT 0;
ALTER TABLE employees
    ADD COLUMN joined_date DATE NOT NULL DEFAULT "9999-12-31";

-- Assign default value for national id since it is unique
UPDATE employees
SET national_id = CONCAT('TEMP-', id)
WHERE national_id IS NULL;

-- Enforce NOT NULL constraints
ALTER TABLE employees
    MODIFY national_id VARCHAR(30) NOT NULL;

-- Add unique constraint
ALTER TABLE employees
    ADD CONSTRAINT uc_employees_national UNIQUE (national_id);