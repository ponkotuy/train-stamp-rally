
ALTER TABLE line_station ADD INDEX(line_id);
ALTER TABLE line_station DROP INDEX line_id;
