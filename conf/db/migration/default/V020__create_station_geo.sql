
CREATE TABLE station_geo(
        station_id BIGINT NOT NULL PRIMARY KEY,
        latitude DOUBLE NOT NULL,
        longitude DOUBLE NOT NULL
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
