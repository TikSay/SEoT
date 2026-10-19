-- Oppretter databasen og tabellene users, courses og enrollments fra bunnen av.
-- Kjør: mysql -u root -p < database/schema.sql
-- Har du allerede databasen fra før? Kjør heller filene i migrations/ som mangler.

CREATE DATABASE IF NOT EXISTS sprint1
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE sprint1;

CREATE TABLE IF NOT EXISTS users (
    id                    INT          NOT NULL AUTO_INCREMENT,
    username              VARCHAR(50)  NULL,                -- valgfri, brukes ikke av nye medlemmer
    fornavn               VARCHAR(50)  NULL,
    etternavn             VARCHAR(50)  NULL,
    lokallag              VARCHAR(50)  NULL,                -- f.eks. 'halden husflidslag'
    email                 VARCHAR(100) NOT NULL,            -- brukes til innlogging
    password_hash         VARCHAR(60)  NOT NULL,            -- BCrypt
    reset_code_hash       VARCHAR(60)  NULL,                -- BCrypt-hash av reset-koden
    reset_code_expires_at DATETIME     NULL,
    reset_attempts        INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY email (email),
    UNIQUE KEY username (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS courses (
    id          INT           NOT NULL AUTO_INCREMENT,
    tittel      VARCHAR(100)  NOT NULL,
    beskrivelse TEXT          NULL,
    lokallag    VARCHAR(50)   NOT NULL,                 -- samme verdier som users.lokallag, f.eks. 'halden husflidslag'
    sted        VARCHAR(100)  NOT NULL,
    dato        DATE          NOT NULL,
    starttid    TIME          NOT NULL,
    kapasitet   INT           NOT NULL,
    pris        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    nivaa       VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_courses_kapasitet CHECK (kapasitet > 0),
    CONSTRAINT chk_courses_pris      CHECK (pris >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS enrollments (
    id                INT      NOT NULL AUTO_INCREMENT,
    user_id           INT      NOT NULL,
    course_id         INT      NOT NULL,
    pameldt_tidspunkt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_enrollments_user_course (user_id, course_id),  -- samme bruker kan ikke melde seg på samme kurs to ganger
    KEY fk_enrollments_course_idx (course_id),
    CONSTRAINT fk_enrollments_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,                    -- slettes brukeren, slettes påmeldingene
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses (id)
        ON DELETE RESTRICT ON UPDATE NO ACTION                    -- kurs med påmeldte kan ikke slettes
) ENGINE=InnoDB;
