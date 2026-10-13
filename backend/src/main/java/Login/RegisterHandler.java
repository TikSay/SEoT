package Login;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Set;

public class RegisterHandler {

    private static final int MAX_NAME_LENGTH = 50;       // samme som users.fornavn / users.etternavn VARCHAR(50)
    private static final int MAX_EMAIL_LENGTH = 100;     // samme som users.email VARCHAR(100)

    // Må stemme med valgene i «By adresse»-menyen i Registrer.jsx
    private static final Set<String> LOKALLAG = Set.of(
            "fredrikstad husflidslag", "halden husflidslag", "hobøl husflidslag",
            "indre østfold husflidslag", "moss husflidslag", "rygge husflidslag",
            "råde husflidslag", "sarpsborg husflidslag");

    private final DataRegistry dataRegistry;

    public RegisterHandler(DataRegistry dataRegistry) {
        this.dataRegistry = dataRegistry;
    }

    public void register(String fornavn, String etternavn, String email, String password, String lokallag) {

        if (fornavn == null || fornavn.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }

        if (etternavn == null || etternavn.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (fornavn.trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("First name cannot be longer than 50 characters.");
        }

        if (etternavn.trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Last name cannot be longer than 50 characters.");
        }

        if (email.trim().length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email cannot be longer than 100 characters.");
        }

        if (lokallag == null || !LOKALLAG.contains(lokallag)) {
            throw new IllegalArgumentException("Please choose a valid local chapter.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (dataRegistry.findUserByEmail(normalizedEmail) != null) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        String passwordHash =
                BCrypt.hashpw(password, BCrypt.gensalt());

        dataRegistry.addUser(
                fornavn.trim(),
                etternavn.trim(),
                normalizedEmail,
                passwordHash,
                lokallag
        );
    }
}
