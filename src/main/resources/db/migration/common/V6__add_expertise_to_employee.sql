CREATE TABLE employee_expertise
(
    employee_id  BIGINT NOT NULL,
    expertise_id BIGINT NOT NULL
);

ALTER TABLE employee_expertise
    ADD CONSTRAINT fk_empexp_on_employee FOREIGN KEY (employee_id) REFERENCES employees (id);

ALTER TABLE employee_expertise
    ADD CONSTRAINT fk_empexp_on_expertise FOREIGN KEY (expertise_id) REFERENCES expertises (id);