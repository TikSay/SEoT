-- Påmelding uten konto: lagrer navn, alder, e-post, medlem og pris på hver påmelding.
-- Kjøres etter 003_kurs_og_pamelding.sql.

USE se26_G24;

ALTER TABLE enrollments
    MODIFY user_id INT NULL,
    ADD COLUMN fornavn     VARCHAR(50)   NOT NULL,
    ADD COLUMN etternavn   VARCHAR(50)   NOT NULL,
    ADD COLUMN alder       INT           NOT NULL,
    ADD COLUMN email       VARCHAR(100)  NOT NULL,
    ADD COLUMN er_medlem   BOOLEAN       NOT NULL DEFAULT FALSE,
    ADD COLUMN pris        DECIMAL(10,2) NOT NULL,
    ADD UNIQUE KEY uq_enrollments_email_course (email, course_id);  -- samme e-post kan ikke melde seg på samme kurs to ganger
