
ALTER TABLE game ADD updated BIGINT;
UPDATE game set updated = created;
ALTER TABLE game MODIFY updated BIGINT NOT NULL;
