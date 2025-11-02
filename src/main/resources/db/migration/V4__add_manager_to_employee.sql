ALTER TABLE employees
    ADD COLUMN manager_id BIGINT NULL;

ALTER TABLE employees
    ADD CONSTRAINT FK_EMPLOYEES_ON_MANAGER
        FOREIGN KEY (manager_id)
            REFERENCES employees (id);