CREATE TABLE departments
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (id)
);

CREATE TABLE employee_expertise
(
    employee_id  BIGINT NOT NULL,
    expertise_id BIGINT NOT NULL
);

CREATE TABLE employees
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)          NOT NULL,
    date_of_birth   date                  NOT NULL,
    graduation_date date                  NOT NULL,
    gender          VARCHAR(255)          NOT NULL,
    department_id   BIGINT                NOT NULL,
    team_id         BIGINT                NOT NULL,
    manager_id      BIGINT                NULL,
    salary          FLOAT                 NOT NULL,
    CONSTRAINT pk_employees PRIMARY KEY (id)
);

CREATE TABLE expertises
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_expertises PRIMARY KEY (id)
);

CREATE TABLE teams
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_teams PRIMARY KEY (id)
);

ALTER TABLE employees
    ADD CONSTRAINT FK_EMPLOYEES_ON_DEPARTMENT FOREIGN KEY (department_id) REFERENCES departments (id);

ALTER TABLE employees
    ADD CONSTRAINT FK_EMPLOYEES_ON_MANAGER FOREIGN KEY (manager_id) REFERENCES employees (id);

ALTER TABLE employees
    ADD CONSTRAINT FK_EMPLOYEES_ON_TEAM FOREIGN KEY (team_id) REFERENCES teams (id);

ALTER TABLE employee_expertise
    ADD CONSTRAINT fk_empexp_on_employee FOREIGN KEY (employee_id) REFERENCES employees (id);

ALTER TABLE employee_expertise
    ADD CONSTRAINT fk_empexp_on_expertise FOREIGN KEY (expertise_id) REFERENCES expertises (id);