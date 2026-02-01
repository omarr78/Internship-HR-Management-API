CREATE TABLE leaves
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    leave_date  DATE   NOT NULL,
    employee_id BIGINT NOT NULL,

    CONSTRAINT uq_leaves UNIQUE (employee_id, leave_date),
    CONSTRAINT fk_employee_on_leaves
        FOREIGN KEY (employee_id)
            REFERENCES employees (id)
)