
CREATE TABLE mission_rate (
        mission_id BIGINT NOT NULL PRIMARY KEY,
        rate INT NOT NULL,
        FOREIGN KEY (mission_id) REFERENCES mission(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
