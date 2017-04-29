
CREATE TABLE session (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        account_id BIGINT NOT NULL,
        token CHAR(64) NOT NULL,
        created BIGINT NOT NULL,
        expire BIGINT NOT NULL,
        UNIQUE KEY(token) USING HASH
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
