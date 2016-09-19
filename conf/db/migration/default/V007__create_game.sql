
CREATE TABLE game(
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        mission_id BIGINT NOT NULL,
        account_id BIGINT NOT NULL,
        `time` VARCHAR(12) NOT NULL,
        created BIGINT NOT NULL,
        UNIQUE KEY (account_id, mission_id),
        FOREIGN KEY (mission_id) REFERENCES mission(id),
        FOREIGN KEY (account_id) REFERENCES account(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE game_progress(
        game_id BIGINT NOT NULL,
        station_id BIGINT NOT NULL,
        arrival_time VARCHAR(12),
        PRIMARY KEY (game_id, station_id),
        FOREIGN KEY (game_id) REFERENCES game(id),
        FOREIGN KEY (station_id) REFERENCES station(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
