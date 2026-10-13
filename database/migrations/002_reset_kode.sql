-- Kolonner for glemt passord: hash av reset-koden, utløpstid og antall feil forsøk.
-- Kjøres etter 001. Ta backup først: mysqldump -u root -p --databases sprint1 > backup.sql

USE sprint1;

ALTER TABLE users
    ADD COLUMN reset_code_hash       VARCHAR(60) NULL,
    ADD COLUMN reset_code_expires_at DATETIME    NULL,
    ADD COLUMN reset_attempts        INT         NOT NULL DEFAULT 0;
