
DELETE FROM mission_station;
DELETE FROM mission;
ALTER TABLE mission ADD start_station_id BIGINT NOT NULL;
ALTER TABLE mission ADD INDEX (start_station_id);
ALTER TABLE mission ADD FOREIGN KEY (start_station_id) REFERENCES station(id);
