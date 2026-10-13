-- Oppretter databasen og users-tabellen fra bunnen av.
-- Kjør: mysql -u root -p < database/schema.sql
-- Har du allerede en eldre users-tabell? Kjør heller filene i migrations/ som mangler.

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
