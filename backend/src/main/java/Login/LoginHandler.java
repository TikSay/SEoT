package Login;

import org.mindrot.jbcrypt.BCrypt;

public class LoginHandler {

    private final DataRegistry registry;

    public LoginHandler(DataRegistry registry) {
        this.registry = registry;
    }

    public LoginResult login(String email, String password) {
        // 1. Sjekk at begge feltene er fylt ut
        if (email == null || email.isBlank()) {
            return new LoginResult(false, "Email is required.");
        }
        if (password == null || password.isBlank()) {
            return new LoginResult(false, "Password is required.");
        }

        // 2 & 3. Finn brukeren på e-post og hent den lagrede hashen
        UserCredentials credentials = registry.findUserByEmail(email.trim().toLowerCase());

        // 4 & 6. Ukjent e-post og feil passord gir samme melding, så ingen kan finne ut hvilke e-poster som finnes
        if (credentials == null || !BCrypt.checkpw(password, credentials.getPasswordHash())) {
            return new LoginResult(false, "Invalid email or password.");
        }

        // 5. Alt stemmer
        return new LoginResult(true, "Login successful.", credentials.getUser());
    }
}
