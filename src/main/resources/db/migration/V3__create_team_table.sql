CREATE TABLE teams
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_teams PRIMARY KEY (id)
);

ALTER TABLE employees
    ADD COLUMN team_id BIGINT NOT NULL;

ALTER TABLE employees
    ADD CONSTRAINT FK_EMPLOYEES_ON_TEAM
        FOREIGN KEY (team_id)
            REFERENCES teams (id);