package Login;

import java.time.Instant;

public interface DataRegistry {

    UserCredentials findUserByEmail(String email);

    void addUser(String fornavn, String etternavn, String email, String passwordHash, String lokallag);

    /** Lagrer en ny reset-kode (allerede hashet) og nullstiller antall forsøk. */
    void saveResetCode(int userId, String codeHash, Instant expiresAt);

    /** Henter reset-statusen for e-posten, eller null hvis ingen bruker har den e-posten. */
    PasswordReset findPasswordReset(String email);

    /** Teller ett feil forsøk på reset-koden. */
    void incrementResetAttempts(int userId);

    /** Setter nytt passord og fjerner reset-koden i én og samme UPDATE. */
    void updatePasswordAndClearReset(int userId, String newPasswordHash);
}
