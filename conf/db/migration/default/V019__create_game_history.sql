
CREATE TABLE game_history(
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        game_id BIGINT NOT NULL,
        `time` VARCHAR(12) NOT NULL,
        distance DOUBLE NOT NULL,
        money INT NOT NULL,
        station_id BIGINT NOT NULL,
        created BIGINT NOT NULL,
        FOREIGN KEY (game_id) REFERENCES game(id),
        FOREIGN KEY (station_id) REFERENCES station(id),
        INDEX (game_id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
