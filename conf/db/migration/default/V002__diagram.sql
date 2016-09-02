
CREATE TABLE diagram (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(128) NOT NULL,
        train_type INT NOT NULL,
        sub_type VARCHAR(128) NOT NULL
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE train(
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        diagram_id BIGINT NOT NULL,
        `start` CHAR(4) NOT NULL,
        FOREIGN KEY (diagram_id) REFERENCES diagram(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE stop_station(
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        diagram_id BIGINT NOT NULL,
        line_station_id BIGINT NOT NULL,
        minutes INT NOT NULL,
        FOREIGN KEY (diagram_id) REFERENCES diagram(id),
        FOREIGN KEY (line_station_id) REFERENCES line_station(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
