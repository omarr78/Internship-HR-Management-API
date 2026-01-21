CREATE TABLE bonuses
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount      FLOAT  NOT NULL,
    bonus_date  DATE   NOT NULL,
    employee_id BIGINT NOT NULL,

    CONSTRAINT fk_employee_on_bonuses
        FOREIGN KEY (employee_id)
            REFERENCES employees (id)
)