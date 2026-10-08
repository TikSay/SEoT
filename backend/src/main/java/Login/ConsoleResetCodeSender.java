package Login;

/**
 * Bare for Sprint 1: skriver reset-koden i konsollen, så vi kan teste lokalt.
 * Koden sendes aldri tilbake i API-svaret.
 * Byttes ut med ekte e-post (f.eks. SMTP) i WebServer når vi kommer så langt.
 */
public class ConsoleResetCodeSender implements ResetCodeSender {

    @Override
    public void send(String email, String code) {
        System.out.println("[DEV ONLY] Password reset code for " + email + ": " + code);
    }
}
