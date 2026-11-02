package Login;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** S-006: Reglene og grenseverdiene testes grundig her. Handler- og HTTP-testene sjekker bare at reglene brukes. */
class InputValidatorTest {

    // --- E-post ---

    @ParameterizedTest
    @ValueSource(strings = {"kari@example.com", "ola+kurs@husflid.no", "a@sub.domain.no", "  kari@example.com  "})
    void validEmailsAreAccepted(String email) {
        assertDoesNotThrow(() -> InputValidator.requireValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abc",                       // mangler @
            "@example.com",              // mangler navn
            "kari@",                     // mangler domene
            "kari@example",              // mangler toppdomene
            "kari@example.c",            // toppdomene på 1 tegn
            "kari@example..com",         // to punktum etter hverandre
            "kari@@example.com",         // to @
            "kari nordmann@example.com"  // mellomrom
    })
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

    // --- Nytt passord ---

    @Test
    void passwordOf7CharactersIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword("1234567", "Password"));
        assertEquals("Password must be at least 8 characters.", e.getMessage());
    }

    @Test
    void passwordOf8CharactersIsAccepted() {
        assertDoesNotThrow(() -> InputValidator.requireValidNewPassword("12345678", "Password"));
    }

    @Test
    void labelIsUsedInMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword("kort", "New password"));
        assertEquals("New password must be at least 8 characters.", e.getMessage());
    }

    @Test
    void passwordOf72BytesIsAccepted() {
        assertDoesNotThrow(() -> InputValidator.requireValidNewPassword("a".repeat(72), "Password"));
    }

    @Test
    void passwordOf73BytesIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword("a".repeat(73), "Password"));
        assertEquals("Password cannot be longer than 72 bytes.", e.getMessage());
    }

    /** Æ, ø og å er 2 bytes hver i UTF-8, så 37 tegn blir 74 bytes. */
    @Test
    void norwegianLettersCountAsTwoBytes() {
        assertDoesNotThrow(() -> InputValidator.requireValidNewPassword("æ".repeat(36), "Password"));
        assertThrows(IllegalArgumentException.class,
                () -> InputValidator.requireValidNewPassword("æ".repeat(37), "Password"));
    }

    // --- Reset-kode ---

    @ParameterizedTest
    @ValueSource(strings = {"000000", "123456", " 123456 "})
    void sixDigitCodesAreValid(String code) {
        assertTrue(InputValidator.isValidResetCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12345",     // 5 siffer
            "1234567",   // 7 siffer
            "abcdef",    // bokstaver
            "123 456",   // mellomrom inni
            "-12345",    // fortegn
            "١٢٣٤٥٦"     // arabiske siffer: regelen godtar bare 0-9
    })
    void otherCodesAreInvalid(String code) {
        assertFalse(InputValidator.isValidResetCode(code));
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
