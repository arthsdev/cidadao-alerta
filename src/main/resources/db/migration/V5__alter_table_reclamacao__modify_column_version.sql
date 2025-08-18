ALTER TABLE reclamacao
MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;
UPDATE reclamacao SET version = 0 WHERE version IS NULL;
