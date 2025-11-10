CREATE TABLE employees
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)          NOT NULL,
    date_of_birth   date                  NOT NULL,
    graduation_date date                  NOT NULL,
    gender          VARCHAR(255)          NOT NULL,
    salary          FLOAT                 NOT NULL,
    CONSTRAINT pk_employees PRIMARY KEY (id)
);
