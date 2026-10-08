-- For eldre users-tabeller fra før registreringssiden fikk fornavn, etternavn og by.
-- username blir valgfri (UNIQUE godtar flere NULL), og tre nye kolonner legges til.
-- Ta backup først: mysqldump -u root -p --databases sprint1 > backup.sql

USE sprint1;

ALTER TABLE users
    MODIFY username  VARCHAR(50) NULL,
    ADD COLUMN fornavn   VARCHAR(50) NULL AFTER username,
    ADD COLUMN etternavn VARCHAR(50) NULL AFTER fornavn,
    ADD COLUMN lokallag  VARCHAR(50) NULL AFTER etternavn;
