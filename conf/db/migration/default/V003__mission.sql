
CREATE TABLE mission (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        created BIGINT NOT NULL
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE mission_station (
        mission_id BIGINT NOT NULL,
        station_id BIGINT NOT NULL,
        PRIMARY KEY (mission_id, station_id),
        FOREIGN KEY (mission_id) REFERENCES mission(id),
        FOREIGN KEY (station_id) REFERENCES station(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
