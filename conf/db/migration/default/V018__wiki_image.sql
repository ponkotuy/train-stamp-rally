
CREATE TABLE image(
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        bytes LONGBLOB NOT NULL,
        created BIGINT NOT NULL
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE station_image(
        id BIGINT NOT NULL PRIMARY KEY,
        image_id BIGINT,
        FOREIGN KEY (id) REFERENCES station(id),
        FOREIGN KEY (image_id) REFERENCES image(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;

CREATE TABLE image_attribute(
        id BIGINT NOT NULL PRIMARY KEY,
        file_name VARCHAR(255) NOT NULL,
        `name` VARCHAR(255) NOT NULL,
        artist TEXT NOT NULL,
        license_short_name VARCHAR(64) NOT NULL,
        license_url VARCHAR(256) NOT NULL,
        credit TEXT NOT NULL,
        created BIGINT NOT NULL,
        FOREIGN KEY (id) REFERENCES station(id)
) ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4;
