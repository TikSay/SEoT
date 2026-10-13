package Login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgotPasswordHandlerTest {

    private static final String EMAIL = "alice@example.com";
    private static final String OLD_PASSWORD = "secret123";
    private static final String NEW_PASSWORD = "newSecret456";
    private static final String INVALID_CODE = "Invalid or expired reset code.";

    private FakeDataRegistry registry;
    private FakeResetCodeSender sender;
    private MutableClock clock;
    private ForgotPasswordHandler handler;

    @BeforeEach
    void setUp() {
        registry = new FakeDataRegistry();
        registry.addUser(1, "alice", EMAIL, OLD_PASSWORD);
        sender = new FakeResetCodeSender();
        clock = new MutableClock(Instant.parse("2026-10-08T12:00:00Z"));
        handler = new ForgotPasswordHandler(registry, sender, clock);
    }

    // --- Be om reset-kode ---

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyEmailIsRejectedWhenRequestingCode(String email) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.requestReset(email));
        assertEquals("Email cannot be empty.", e.getMessage());
        assertEquals(0, sender.sendCount);
    }

    @Test
    void unknownEmailSendsNothingAndThrowsNothing() {
        handler.requestReset("nobody@example.com");

        assertEquals(0, sender.sendCount);
        assertNull(registry.findPasswordReset("nobody@example.com"));
    }

    @Test
    void knownEmailSendsSixDigitCodeAndStoresOnlyHash() {
        handler.requestReset("  Alice@Example.COM ");

        assertEquals(1, sender.sendCount);
        assertEquals(EMAIL, sender.lastEmail);
        assertTrue(sender.lastCode.matches("\\d{6}"));

        PasswordReset stored = registry.findPasswordReset(EMAIL);
        assertNotNull(stored.getCodeHash());
        assertNotEquals(sender.lastCode, stored.getCodeHash());
        assertTrue(stored.getCodeHash().startsWith("$2a$"));
        assertTrue(BCrypt.checkpw(sender.lastCode, stored.getCodeHash()));
        assertEquals(clock.instant().plus(Duration.ofMinutes(15)), stored.getExpiresAt());
        assertEquals(0, stored.getAttempts());
    }

    @Test
    void newRequestReplacesOldCode() {
        handler.requestReset(EMAIL);
        String oldCode = sender.lastCode;
        do {
            handler.requestReset(EMAIL);
        } while (sender.lastCode.equals(oldCode));   // i det sjeldne tilfellet at samme kode trekkes to ganger

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, oldCode, NEW_PASSWORD));
        assertEquals(INVALID_CODE, e.getMessage());

        handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD);
        assertTrue(passwordIs(NEW_PASSWORD));
    }

    // --- Nytt passord med kode ---

    @Test
    void correctCodeResetsPasswordAndLoginUsesNewPassword() {
        handler.requestReset(EMAIL);

        handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD);

        assertTrue(passwordIs(NEW_PASSWORD));
        assertFalse(passwordIs(OLD_PASSWORD));

        LoginHandler loginHandler = new LoginHandler(registry);
        assertTrue(loginHandler.login(EMAIL, NEW_PASSWORD).isSuccess());
        assertFalse(loginHandler.login(EMAIL, OLD_PASSWORD).isSuccess());
    }

    @Test
    void successfulResetClearsCodeAndAttempts() {
        handler.requestReset(EMAIL);
        wrongAttempt();

        handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD);

        PasswordReset stored = registry.findPasswordReset(EMAIL);
        assertNull(stored.getCodeHash());
        assertNull(stored.getExpiresAt());
        assertEquals(0, stored.getAttempts());
    }

    @Test
    void wrongCodeIsRejectedAndCounted() {
        handler.requestReset(EMAIL);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, wrongCode(), NEW_PASSWORD));

        assertEquals(INVALID_CODE, e.getMessage());
        assertEquals(1, registry.findPasswordReset(EMAIL).getAttempts());
        assertTrue(passwordIs(OLD_PASSWORD));
    }

    @Test
    void correctCodeIsRejectedAfterFiveWrongAttempts() {
        handler.requestReset(EMAIL);
        for (int i = 0; i < 5; i++) {
            wrongAttempt();
        }

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD));

        assertEquals(INVALID_CODE, e.getMessage());
        assertTrue(passwordIs(OLD_PASSWORD));
    }

    @Test
    void correctCodeIsAcceptedAfterFourWrongAttempts() {
        handler.requestReset(EMAIL);
        for (int i = 0; i < 4; i++) {
            wrongAttempt();
        }

        handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD);

        assertTrue(passwordIs(NEW_PASSWORD));
    }

    @Test
    void codeIsValidForFifteenMinutes() {
        handler.requestReset(EMAIL);
        clock.advance(Duration.ofMinutes(15));

        handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD);

        assertTrue(passwordIs(NEW_PASSWORD));
    }

    @Test
    void expiredCodeIsRejected() {
        handler.requestReset(EMAIL);
        clock.advance(Duration.ofMinutes(15).plusSeconds(1));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD));

        assertEquals(INVALID_CODE, e.getMessage());
        assertTrue(passwordIs(OLD_PASSWORD));
    }

    @Test
    void codeCanOnlyBeUsedOnce() {
        handler.requestReset(EMAIL);
        String code = sender.lastCode;
        handler.resetPassword(EMAIL, code, NEW_PASSWORD);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, code, "anotherPassword789"));

        assertEquals(INVALID_CODE, e.getMessage());
        assertTrue(passwordIs(NEW_PASSWORD));
    }

    @Test
    void resetWithoutRequestedCodeIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, "123456", NEW_PASSWORD));

        assertEquals(INVALID_CODE, e.getMessage());
        assertTrue(passwordIs(OLD_PASSWORD));
    }

    @Test
    void unknownEmailGivesSameMessageAsWrongCode() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword("nobody@example.com", "123456", NEW_PASSWORD));

        assertEquals(INVALID_CODE, e.getMessage());
    }

    @Test
    void emailIsNormalizedWhenResetting() {
        handler.requestReset(EMAIL);

        handler.resetPassword("  ALICE@example.com ", sender.lastCode, NEW_PASSWORD);

        assertTrue(passwordIs(NEW_PASSWORD));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyCodeIsRejected(String code) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, code, NEW_PASSWORD));
        assertEquals("Reset code cannot be empty.", e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyNewPasswordIsRejected(String newPassword) {
        handler.requestReset(EMAIL);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, sender.lastCode, newPassword));

        assertEquals("New password cannot be empty.", e.getMessage());
        assertTrue(passwordIs(OLD_PASSWORD));
        assertEquals(0, registry.findPasswordReset(EMAIL).getAttempts());
    }

    @Test
    void newPasswordIsStoredAsBCryptHash() {
        handler.requestReset(EMAIL);

        handler.resetPassword(EMAIL, sender.lastCode, NEW_PASSWORD);

        String storedHash = registry.findUserByEmail(EMAIL).getPasswordHash();
        assertNotEquals(NEW_PASSWORD, storedHash);
        assertTrue(storedHash.startsWith("$2a$"));
    }

    // --- Hjelpemetoder ---

    private boolean passwordIs(String password) {
        return BCrypt.checkpw(password, registry.findUserByEmail(EMAIL).getPasswordHash());
    }

    private String wrongCode() {
        return sender.lastCode.equals("000000") ? "000001" : "000000";
    }

    private void wrongAttempt() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.resetPassword(EMAIL, wrongCode(), NEW_PASSWORD));
    }

    /** Husker siste kode i stedet for å sende den. */
    private static class FakeResetCodeSender implements ResetCodeSender {
        int sendCount = 0;
        String lastEmail;
        String lastCode;

        @Override
        public void send(String email, String code) {
            sendCount++;
            lastEmail = email;
            lastCode = code;
        }
    }

    /** En klokke testen kan spole fremover. */
    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
