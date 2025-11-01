CREATE TABLE departments
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (id)
);

ALTER TABLE employees
    ADD COLUMN department_id BIGINT NOT NULL,
    ADD CONSTRAINT FK_EMPLOYEES_ON_DEPARTMENT FOREIGN KEY (department_id)
    REFERENCES departments (id);