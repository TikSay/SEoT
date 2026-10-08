package Login;

/**
 * En bruker sammen med den lagrede BCrypt-hashen av passordet.
 * Brukes bare mellom DataRegistry og handlerne. Hashen sendes aldri ut til frontend.
 */
public class UserCredentials {
    private final User user;
    private final String passwordHash;

    public UserCredentials(User user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
    }

    public User getUser() {
        return user;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
