package Login;

import java.time.Instant;

/**
 * Reset-statusen for én bruker, slik den ligger i databasen.
 * codeHash og expiresAt er null hvis brukeren ikke har bedt om kode.
 */
public class PasswordReset {
    private final int userId;
    private final String codeHash;
    private final Instant expiresAt;
    private final int attempts;

    public PasswordReset(int userId, String codeHash, Instant expiresAt, int attempts) {
        this.userId = userId;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
    }

    public int getUserId() {
        return userId;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }
}
