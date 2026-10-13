package Login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterHandlerTest {

    private static final String LOKALLAG = "fredrikstad husflidslag";

    private FakeDataRegistry registry;
    private RegisterHandler handler;

    @BeforeEach
    void setUp() {
        registry = new FakeDataRegistry();
        registry.addUser(1, "alice", "alice@example.com", "secret123");
        handler = new RegisterHandler(registry);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyFirstNameIsRejected(String fornavn) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register(fornavn, "Nordmann", "new@example.com", "secret123", LOKALLAG));
        assertEquals("First name cannot be empty.", e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyLastNameIsRejected(String etternavn) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register("Ola", etternavn, "new@example.com", "secret123", LOKALLAG));
        assertEquals("Last name cannot be empty.", e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyEmailIsRejected(String email) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register("Ola", "Nordmann", email, "secret123", LOKALLAG));
        assertEquals("Email cannot be empty.", e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emptyPasswordIsRejected(String password) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register("Ola", "Nordmann", "new@example.com", password, LOKALLAG));
        assertEquals("Password cannot be empty.", e.getMessage());
        assertNull(registry.findUserByEmail("new@example.com"));
    }

    @Test
    void firstNameLongerThan50CharactersIsRejected() {
        String fornavn = "a".repeat(51);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register(fornavn, "Nordmann", "new@example.com", "secret123", LOKALLAG));
        assertEquals("First name cannot be longer than 50 characters.", e.getMessage());
        assertNull(registry.findUserByEmail("new@example.com"));
    }

    @Test
    void lastNameLongerThan50CharactersIsRejected() {
        String etternavn = "a".repeat(51);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register("Ola", etternavn, "new@example.com", "secret123", LOKALLAG));
        assertEquals("Last name cannot be longer than 50 characters.", e.getMessage());
        assertNull(registry.findUserByEmail("new@example.com"));
    }

    @Test
    void emailLongerThan100CharactersIsRejected() {
        String email = "b".repeat(89) + "@example.com";   // 101 tegn
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register("Ola", "Nordmann", email, "secret123", LOKALLAG));
        assertEquals("Email cannot be longer than 100 characters.", e.getMessage());
        assertNull(registry.findUserByEmail(email));
    }

    @Test
    void invalidLokallagIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register("Ola", "Nordmann", "new@example.com", "secret123", "oslo husflidslag"));
        assertEquals("Please choose a valid local chapter.", e.getMessage());
        assertNull(registry.findUserByEmail("new@example.com"));
    }

    @Test
    void duplicateEmailIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.register("Alice", "Hansen", "  Alice@Example.com ", "other123", LOKALLAG));
        assertEquals("Email is already registered.", e.getMessage());
        assertEquals("alice", registry.findUserByEmail("alice@example.com").getUser().getUsername());
    }

    @Test
    void successfulRegistrationStoresMemberWithBCryptHash() {
        handler.register("  Ola ", " Nordmann  ", "  New@Example.com ", "secret123", LOKALLAG);

        UserCredentials stored = registry.findUserByEmail("new@example.com");
        assertNotNull(stored);
        assertEquals("Ola", stored.getUser().getFornavn());
        assertEquals("Nordmann", stored.getUser().getEtternavn());
        assertEquals("new@example.com", stored.getUser().getEmail());
        assertEquals(LOKALLAG, stored.getUser().getLokallag());
        assertNull(stored.getUser().getUsername());
        assertTrue(stored.getPasswordHash().startsWith("$2a$"));
        assertTrue(BCrypt.checkpw("secret123", stored.getPasswordHash()));
    }
}
