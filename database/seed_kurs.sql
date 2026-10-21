-- Testkurs for Sprint 2 (kurskalender). Krever at 003_kurs_og_pamelding.sql er kjørt.
-- Kjør fra rotmappen SEoT/: mysql -u root -p < database/seed_kurs.sql
-- Kan kjøres flere ganger: et kurs legges bare inn hvis samme tittel, lokallag og dato ikke finnes fra før.
-- Legger bare inn kurs, ikke påmeldinger.

SET NAMES utf8mb4;
USE sprint1;

START TRANSACTION;

CREATE TEMPORARY TABLE seed_courses LIKE courses;

INSERT INTO seed_courses (tittel, beskrivelse, lokallag, sted, dato, starttid, kapasitet, pris, nivaa) VALUES
    ('Sytreff i Råde',
     'Ta med symaskinen og det du holder på med. Vi syr sammen, deler tips og drikker kaffe.',
     'råde husflidslag', 'Råde kulturhus', '2026-11-04', '18:00:00', 15, 0.00, 'alle nivåer'),
    ('Meksikansk fletting',
     'Lær teknikken bak fargerike flettede kurver og brikker. Alt materiell er inkludert.',
     'moss husflidslag', 'Husflidshuset, Moss', '2026-11-12', '17:30:00', 10, 450.00, 'viderekommen'),
    ('Lær å brodere, plattsøm og tellesøm',
     'Grunnleggende broderi med plattsøm og tellesøm. Du går hjem med en ferdig liten duk.',
     'fredrikstad husflidslag', 'Gamlebyen, Fredrikstad', '2026-11-21', '11:00:00', 12, 650.00, 'nybegynner'),
    ('Strikkekafé',
     'Uformelt treff for alle som strikker eller vil lære. Garn og pinner kan lånes.',
     'halden husflidslag', 'Halden bibliotek', '2026-11-26', '18:00:00', 20, 0.00, 'alle nivåer'),
    ('Juleverksted for barn og voksne',
     'Vi lager julepynt av papir, filt og naturmaterialer. Passer for hele familien.',
     'rygge husflidslag', 'Rygge samfunnshus', '2026-12-05', '12:00:00', 25, 0.00, 'alle nivåer'),
    ('Bunadkurs',
     'Sy eller gjør ferdig din egen bunad med veiledning. Kurset går over flere kvelder.',
     'sarpsborg husflidslag', 'Sarpsborg kulturhus', '2027-01-13', '18:00:00', 8, 2400.00, 'viderekommen'),
    ('Grunnkurs i treskjæring',
     'Lær å bruke tollekniv og skjærejern trygt, og lag din egen smørkniv.',
     'indre østfold husflidslag', 'Mysen', '2027-02-06', '10:00:00', 8, 900.00, 'nybegynner'),
    ('Toving av votter',
     'Våttoving fra ull til ferdige votter. Ta med håndkle og forkle.',
     'hobøl husflidslag', 'Hobøl grendehus', '2027-02-17', '18:00:00', 10, 350.00, 'nybegynner');

INSERT INTO courses (tittel, beskrivelse, lokallag, sted, dato, starttid, kapasitet, pris, nivaa)
SELECT s.tittel, s.beskrivelse, s.lokallag, s.sted, s.dato, s.starttid, s.kapasitet, s.pris, s.nivaa
FROM seed_courses s
WHERE NOT EXISTS (
    SELECT 1 FROM courses c
    WHERE c.tittel = s.tittel AND c.lokallag = s.lokallag AND c.dato = s.dato
);

DROP TEMPORARY TABLE seed_courses;

COMMIT;
