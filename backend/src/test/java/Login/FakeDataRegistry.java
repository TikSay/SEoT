package Login;

import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Falsk DataRegistry for enhetstestene.
 * Holder brukerne i minnet i stedet for i den ekte databasen.
 */
public class FakeDataRegistry implements DataRegistry {

    private final Map<String, UserCredentials> usersByEmail = new HashMap<>();
    private final Map<Integer, PasswordReset> resetsByUserId = new HashMap<>();
    private int callCount = 0;
    private String lastEmailLookedUp;
    private int nextId = 1;

    /**
     * Hjelpemetode for login-testene.
     * Tar inn passordet i klartekst og hasher det før det lagres.
     */
    public void addUser(int id, String username, String email, String plainPassword) {
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(4));

        usersByEmail.put(
                email,
                new UserCredentials(
                        new User(id, username, email),
                        hash
                )
        );
    }

    /**
     * Kreves av DataRegistry.
     * Passordet er allerede hashet av RegisterHandler.
     */
    @Override
    public void addUser(String fornavn, String etternavn, String email, String passwordHash, String lokallag) {
        User user = new User(nextId++, null, email, fornavn, etternavn, lokallag);

        usersByEmail.put(
                email,
                new UserCredentials(user, passwordHash)
        );
    }

    /**
     * Lagrer verdien akkurat som den er.
     * Brukes for å teste hva som skjer med en ugyldig lagret hash.
     */
    public void addRawStoredValue(
            int id,
            String username,
            String email,
            String storedValue
    ) {
        usersByEmail.put(
                email,
                new UserCredentials(
                        new User(id, username, email),
                        storedValue
                )
        );
    }

    @Override
    public UserCredentials findUserByEmail(String email) {
        callCount++;
        lastEmailLookedUp = email;

        return usersByEmail.get(email);
    }

    @Override
    public void saveResetCode(int userId, String codeHash, Instant expiresAt) {
        resetsByUserId.put(userId, new PasswordReset(userId, codeHash, expiresAt, 0));
    }

    @Override
    public PasswordReset findPasswordReset(String email) {
        UserCredentials credentials = usersByEmail.get(email);
        if (credentials == null) {
            return null;
        }
        int userId = credentials.getUser().getId();
        return resetsByUserId.getOrDefault(userId, new PasswordReset(userId, null, null, 0));
    }

    @Override
    public void incrementResetAttempts(int userId) {
        PasswordReset reset = resetsByUserId.get(userId);
        if (reset != null) {
            resetsByUserId.put(userId, new PasswordReset(
                    userId, reset.getCodeHash(), reset.getExpiresAt(), reset.getAttempts() + 1));
        }
    }

    @Override
    public void updatePasswordAndClearReset(int userId, String newPasswordHash) {
        for (Map.Entry<String, UserCredentials> entry : usersByEmail.entrySet()) {
            User user = entry.getValue().getUser();
            if (user.getId() == userId) {
                entry.setValue(new UserCredentials(user, newPasswordHash));
            }
        }
        resetsByUserId.remove(userId);
    }

    public int getCallCount() {
        return callCount;
    }

    public String getLastEmailLookedUp() {
        return lastEmailLookedUp;
    }
}