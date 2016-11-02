
CREATE TABLE fare (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        company_id BIGINT NOT NULL,
        train_type INT NOT NULL,
        km DECIMAL(6, 1) NOT NULL,
        cost INT NOT NULL,
        FOREIGN KEY (company_id) REFERENCES company(id),
        UNIQUE KEY (company_id, train_type, km)
) ENGINE =InnoDB, DEFAULT CHARSET=utf8mb4;
