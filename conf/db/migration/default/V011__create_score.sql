
CREATE TABLE score(
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        mission_id BIGINT NOT NULL,
        account_id BIGINT NOT NULL,
        `time` VARCHAR(12) NOT NULL,
        money INT NOT NULL,
        distance DOUBLE NOT NULL,
        created BIGINT NOT NULL,
        FOREIGN KEY (mission_id) REFERENCES mission(id),
        FOREIGN KEY (account_id) REFERENCES account(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
