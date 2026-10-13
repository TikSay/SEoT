# Backend – Østfolds Husflidslag

Java-backend for innlogging, registrering og glemt passord. Den bruker Javalin og MySQL,
og passord lagres som BCrypt-hash. React-frontenden i rotmappa snakker med denne via `/api/...`.

## Det du trenger

- **JDK 25** (pom.xml krever det, og koden bruker `static void main()` og `IO.println`)
- **MySQL 8**
- **IntelliJ IDEA** (har Maven innebygd)
- **Node.js** for å kjøre frontenden

## 1. Sett opp databasen

Kjør disse kommandoene fra rotmappen `SEoT/`:

Ny database fra bunnen av:

```bash
mysql -u root -p < database/schema.sql
```

Har du allerede en eldre `users`-tabell? Kjør bare migreringene du mangler, i rekkefølge:

```bash
mysql -u root -p < database/migrations/001_fornavn_etternavn_lokallag.sql
mysql -u root -p < database/migrations/002_reset_kode.sql
```

Ta gjerne backup først: `mysqldump -u root -p --databases sprint1 > backup.sql`
(backup-filer ignoreres av git).

## 2. Lag din egen db.properties

Kopier eksempelfila og fyll inn din egen MySQL-bruker og ditt eget passord:

```bash
cp backend/db.properties.example backend/db.properties
```

`db.properties` ligger i `.gitignore`, så passordet ditt havner aldri på GitHub.
**Ikke commit den.**

Vil du heller bruke miljøvariabler, kan du sette `DB_URL`, `DB_USER` og `DB_PASSWORD`.
De brukes først hvis de finnes.

## 3. Start backend

1. Åpne `backend/` i IntelliJ (File → Open → velg `backend/pom.xml` → Open as Project).
2. Kjør `WebServer`.
3. Sjekk at konsollen viser `Login backend running on http://localhost:7070`.

Working directory må være `backend/`, ellers finner den ikke `db.properties`
(Run → Edit Configurations → Working directory).

## 4. Start frontend

I rotmappa:

```bash
npm install     # bare første gang
npm run dev
```

Åpne http://localhost:5173. Vite sender alle kall til `/api` videre til backend på port 7070
(se `vite.config.js`), så du trenger ikke tenke på CORS.

## Endepunkter

Alle tar imot og svarer med JSON. Svaret har alltid `success` og `message`.

| Endepunkt | Sender inn | Svar |
|---|---|---|
| `POST /api/register` | `fornavn`, `etternavn`, `email`, `password`, `lokallag` | `201` ok, `400` ved feil (f.eks. e-post finnes allerede) |
| `POST /api/login` | `email`, `password` | `200` med `user` (id, email, fornavn, etternavn, lokallag), `401` ved feil |
| `POST /api/forgot-password` | `email` | Alltid `200` med samme svar, så ingen kan sjekke hvilke e-poster som finnes |
| `POST /api/reset-password` | `email`, `code`, `newPassword` | `200` ok, `400` ved feil eller utløpt kode |

`lokallag` må være én av verdiene i «By adresse»-menyen i `Registrer.jsx`,
f.eks. `"halden husflidslag"`.

## Glemt passord i Sprint 1

Vi sender ikke ekte e-post ennå. Reset-koden skrives i IntelliJ-konsollen:

```
[DEV ONLY] Password reset code for ola@example.com: 123456
```

Koden er gyldig i 15 minutter, og du har 5 forsøk. Den lagres bare som BCrypt-hash i databasen.
Ekte e-post kobles på senere ved å lage en ny klasse som implementerer `ResetCodeSender`
og bytte den inn i `WebServer`.

## Tester

```bash
cd backend
mvn test
```

Eller høyreklikk `src/test/java` i IntelliJ → Run 'All Tests'. Det skal bli **64 tester**, alle grønne.
Testene bruker en falsk database i minnet, så de trenger ikke MySQL.

## Greit å vite

- **Innloggingen huskes ikke etter refresh.** Backend lager ingen sesjon for `/api/login` ennå,
  så brukeren må logge inn på nytt. Det kommer senere med sesjon og `GET /api/me`.
- **`src/main/resources/public/login.html` må ligge der foreløpig.** Den gamle HTML-innloggingen
  leser den når serveren starter. React er den ordentlige frontenden.
- **Feilmeldingene fra backend er på engelsk** (f.eks. «Invalid email or password.»).
- **`reset_code_expires_at` bruker MySQL DATETIME.** Dagens lokale oppsett fungerer, men
  tidssonehåndtering bør gjøres tydeligere før produksjon.
