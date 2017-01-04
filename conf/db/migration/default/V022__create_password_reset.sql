
CREATE TABLE password_reset(
        id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
        account_id BIGINT NOT NULL,
        secret CHAR(36) CHARACTER SET ascii NOT NULL UNIQUE,
        created BIGINT NOT NULL,
        FOREIGN KEY (account_id) REFERENCES account(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
