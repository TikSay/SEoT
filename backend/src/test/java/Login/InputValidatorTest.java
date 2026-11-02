package Login;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputValidatorTest {

    // --- E-post ---

    @ParameterizedTest
    @ValueSource(strings = {
            "kari@example.com",
            "Kari.Nordmann@Example.COM",
            "ola+kurs@husflid.no",
            "first_last-1@sub.domain.example.org",
            "a@b.no",
            "  kari@example.com  ",          // trimmes
            "bjørn@husflid.no"})
    void validEmailsAreAccepted(String email) {
        assertDoesNotThrow(() -> InputValidator.requireValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abc",
            "kari.example.com",
            "kari@",
            "@example.com",
            "kari@example",
            "kari@example.c",
            "kari@.com",
            "kari@example.",
            "kari@example..com",
            "kari@@example.com",
            "kari@ex@ample.com",
            "kari nordmann@example.com",
            "kari@exa mple.com",
            "kari@example.com\nbcc@evil.com",
            "kari\t@example.com"})
    void invalidEmailsAreRejected(String email) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidEmail(email));
        assertEquals("Please enter a valid email address.", e.getMessage());
    }

    @Test
    void emailOfExactly100CharactersIsAccepted() {
        String email = "b".repeat(88) + "@example.com";
        assertEquals(100, email.length());
        assertDoesNotThrow(() -> InputValidator.requireValidEmail(email));
    }

    @Test
    void emailOf101CharactersIsRejected() {
        String email = "b".repeat(89) + "@example.com";
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidEmail(email));
        assertEquals("Email cannot be longer than 100 characters.", e.getMessage());
    }

    @Test
    void surroundingWhitespaceDoesNotCountTowardsEmailLength() {
        String email = "   " + "b".repeat(88) + "@example.com   ";
        assertDoesNotThrow(() -> InputValidator.requireValidEmail(email));
    }

    // --- Nytt passord ---

    @ParameterizedTest
    @ValueSource(strings = {"a", "abc", "1234567", "       "})
    void passwordShorterThan8CharactersIsRejected(String password) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword(password, "Password"));
        assertEquals("Password must be at least 8 characters.", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "abcdefgh", "   abcde", "æøåÆØÅæø"})
    void passwordOf8CharactersIsAccepted(String password) {
        assertDoesNotThrow(() -> InputValidator.requireValidNewPassword(password, "Password"));
    }

    @Test
    void labelIsUsedInMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword("kort", "New password"));
        assertEquals("New password must be at least 8 characters.", e.getMessage());
    }

    @Test
    void passwordOfExactly72BytesIsAccepted() {
        assertDoesNotThrow(() -> InputValidator.requireValidNewPassword("a".repeat(72), "Password"));
    }

    @Test
    void passwordOf73BytesIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword("a".repeat(73), "Password"));
        assertEquals("Password cannot be longer than 72 bytes.", e.getMessage());
    }

    @Test
    void norwegianLettersCountAsTwoBytes() {
        String ok = "æ".repeat(36);         // 72 bytes
        String tooLong = "æ".repeat(37);    // 74 bytes, men bare 37 tegn
        assertEquals(72, ok.getBytes(StandardCharsets.UTF_8).length);

        assertDoesNotThrow(() -> InputValidator.requireValidNewPassword(ok, "Password"));
        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword(tooLong, "Password"));
    }

    @Test
    void emojiCountsAsOneCharacterForMinimumLength() {
        String sevenEmoji = "😀".repeat(7);   // 7 tegn, 14 UTF-16-enheter
        String eightEmoji = "😀".repeat(8);

        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword(sevenEmoji, "Password"));
        assertDoesNotThrow(() -> InputValidator.requireValidNewPassword(eightEmoji, "Password"));
    }

    // --- Reset-kode ---

    @ParameterizedTest
    @ValueSource(strings = {"000000", "123456", "999999", " 123456 "})
    void sixDigitCodesAreValid(String code) {
        assertTrue(InputValidator.isValidResetCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345", "1234567", "abcdef", "12345a", "123 456", "-12345", "12345.", "١٢٣٤٥٦", "１２３４５６"})
    void otherCodesAreInvalid(String code) {
        assertFalse(InputValidator.isValidResetCode(code));
    }

    @Test
    void nullCodeIsInvalid() {
        assertFalse(InputValidator.isValidResetCode(null));
    }

    // --- Innlogging ---

    @Test
    void loginLimitsAllowBoundaryValues() {
        assertTrue(InputValidator.isWithinLoginLimits("b".repeat(88) + "@example.com", "p".repeat(256)));
    }

    @Test
    void loginLimitsRejectTooLongEmail() {
        assertFalse(InputValidator.isWithinLoginLimits("b".repeat(89) + "@example.com", "secret123"));
    }

    @Test
    void loginLimitsRejectTooLongPassword() {
        assertFalse(InputValidator.isWithinLoginLimits("kari@example.com", "p".repeat(257)));
    }

    @Test
    void loginLimitsDoNotCheckFormatOrMinimumLength() {
        assertTrue(InputValidator.isWithinLoginLimits("ikke-en-epost", "a"));
    }
}
