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
    ADD COLUMN degree ENUM ('FRESH','INTERMEDIATE','SENIOR','ARCHITECT');
ALTER TABLE employees
    ADD COLUMN past_experience_year INT;
ALTER TABLE employees
    ADD COLUMN joined_year DATE;

-- Backfill existing rows
UPDATE employees
SET degree = 'FRESH'
WHERE degree IS NULL;
UPDATE employees
SET past_experience_year = 0
WHERE past_experience_year IS NULL;
UPDATE employees
SET joined_year = CURRENT_DATE
WHERE joined_year IS NULL;

UPDATE employees
SET national_id = CONCAT('TEMP-', id)
WHERE national_id IS NULL;

-- Enforce NOT NULL constraints
ALTER TABLE employees
    MODIFY degree ENUM ('FRESH','INTERMEDIATE','SENIOR','ARCHITECT') NOT NULL;

ALTER TABLE employees
    MODIFY past_experience_year INT NOT NULL;

ALTER TABLE employees
    MODIFY joined_year DATE NOT NULL;

ALTER TABLE employees
    MODIFY national_id VARCHAR(30) NOT NULL;

ALTER TABLE employees
    MODIFY first_name VARCHAR(255) NOT NULL;

ALTER TABLE employees
    MODIFY gross_salary FLOAT NOT NULL;

-- Add unique constraint
ALTER TABLE employees
    ADD CONSTRAINT uc_employees_national UNIQUE (national_id);