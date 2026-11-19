package Login;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * S-006: Felles regler for inputvalidering, så Register-, Login- og ForgotPasswordHandler bruker de samme grensene.
 * Frontend bør speile disse reglene, men backend er den som faktisk håndhever dem.
 */
public final class InputValidator {

    /** Samme som users.email VARCHAR(100). */
    public static final int MAX_EMAIL_LENGTH = 100;

    /** Gjelder bare nye passord (registrering og tilbakestilling), ikke innlogging. */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** BCrypt bruker bare de første 72 bytene, resten ville blitt ignorert uten at brukeren merker det. */
    public static final int MAX_PASSWORD_BYTES = 72;

    /**
     * Øvre grense ved innlogging. Høyere enn 72 bytes, så brukere som registrerte et langt passord
     * før S-006 fortsatt kan logge inn, men stopper urimelig store verdier før vi slår opp i databasen.
     */
    public static final int MAX_LOGIN_PASSWORD_LENGTH = 256;

    /** navn@domene.tld: ingen mellomrom, nøyaktig én @, minst ett punktum i domenet og toppdomene på minst 2 tegn. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)*\\.[^@\\s.]{2,}$");

    private static final Pattern RESET_CODE = Pattern.compile("^[0-9]{6}$");

    private InputValidator() {
    }

    /** Kaster IllegalArgumentException hvis e-posten (etter trim) er for lang eller har ugyldig format. Forutsetter at den ikke er tom. */
    public static void requireValidEmail(String email) {
        String trimmed = email.trim();
        if (trimmed.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email cannot be longer than " + MAX_EMAIL_LENGTH + " characters.");
        }
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
    }

    /**
     * Kaster IllegalArgumentException hvis et nytt passord er kortere enn 8 tegn eller lengre enn 72 UTF-8-bytes.
     * Passordet trimmes ikke. Forutsetter at det ikke er tomt.
     *
     * @param label "Password" eller "New password", brukes i feilmeldingen
     */
    public static void requireValidNewPassword(String password, String label) {
        if (password.codePointCount(0, password.length()) < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(label + " must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException(label + " cannot be longer than " + MAX_PASSWORD_BYTES + " bytes.");
        }
    }

    /** True hvis koden (etter trim) består av nøyaktig 6 siffer 0-9. */
    public static boolean isValidResetCode(String code) {
        return code != null && RESET_CODE.matcher(code.trim()).matches();
    }

    /** True hvis innloggingsfeltene er innenfor rimelige grenser. Sjekker ikke format, så eldre kontoer kan fortsatt logge inn. */
    public static boolean isWithinLoginLimits(String email, String password) {
        return email.trim().length() <= MAX_EMAIL_LENGTH && password.length() <= MAX_LOGIN_PASSWORD_LENGTH;
    }
}
