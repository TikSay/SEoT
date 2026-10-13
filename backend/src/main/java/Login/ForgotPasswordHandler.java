package Login;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;


public class ForgotPasswordHandler {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration CODE_VALIDITY = Duration.ofMinutes(15);
    private static final String INVALID_CODE = "Invalid or expired reset code.";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DataRegistry registry;
    private final ResetCodeSender sender;
    private final Clock clock;

    public ForgotPasswordHandler(DataRegistry registry, ResetCodeSender sender, Clock clock) {
        this.registry = registry;
        this.sender = sender;
        this.clock = clock;
    }


    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        UserCredentials credentials = registry.findUserByEmail(normalizedEmail);
        if (credentials == null) {
            return;
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        // Vi lagrer bare hashen av koden, aldri selve koden
        registry.saveResetCode(
                credentials.getUser().getId(),
                BCrypt.hashpw(code, BCrypt.gensalt()),
                clock.instant().plus(CODE_VALIDITY)
        );

        sender.send(normalizedEmail, code);
    }


    public void resetPassword(String email, String code, String newPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Reset code cannot be empty.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty.");
        }

        PasswordReset reset = registry.findPasswordReset(email.trim().toLowerCase());

        if (reset == null
                || reset.getCodeHash() == null
                || reset.getExpiresAt() == null
                || clock.instant().isAfter(reset.getExpiresAt())
                || reset.getAttempts() >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException(INVALID_CODE);
        }

        if (!BCrypt.checkpw(code.trim(), reset.getCodeHash())) {
            registry.incrementResetAttempts(reset.getUserId());
            throw new IllegalArgumentException(INVALID_CODE);
        }

        // Nytt passord lagres som BCrypt-hash, og koden slettes i samme UPDATE
        registry.updatePasswordAndClearReset(
                reset.getUserId(),
                BCrypt.hashpw(newPassword, BCrypt.gensalt())
        );
    }
}
