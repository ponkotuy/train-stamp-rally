
CREATE TABLE line (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(128) NOT NULL
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE station (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(128) NOT NULL,
        rank INT NOT NULL
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE line_station (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        line_id BIGINT NOT NULL,
        station_id BIGINT NOT NULL,
        km DOUBLE NOT NULL,
        UNIQUE KEY (line_id, station_id),
        FOREIGN KEY (line_id) REFERENCES line(id),
        FOREIGN KEY (station_id) REFERENCES station(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
