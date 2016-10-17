
ALTER TABLE game ADD station_id BIGINT NOT NULL;
ALTER TABLE game ADD FOREIGN KEY (station_id) REFERENCES station(id);
