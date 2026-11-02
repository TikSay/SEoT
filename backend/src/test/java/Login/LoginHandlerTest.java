package Login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginHandlerTest {

    private FakeDataRegistry registry;
    private LoginHandler handler;

    @BeforeEach
    void setUp() {
        registry = new FakeDataRegistry();
        registry.addUser(1, "alice", "alice@example.com", "secret123");
        handler = new LoginHandler(registry);
    }

    @Test
    void validCredentialsSucceed() {
        LoginResult result = handler.login("alice@example.com", "secret123");
        assertTrue(result.isSuccess());
        assertEquals("Login successful.", result.getMessage());
    }

    @Test
    void emailIsTrimmedAndLowercasedBeforeLookup() {
        LoginResult result = handler.login("  Alice@Example.COM ", "secret123");
        assertTrue(result.isSuccess());
        assertEquals("alice@example.com", registry.getLastEmailLookedUp());
    }

    @Test
    void wrongPasswordFails() {
        LoginResult result = handler.login("alice@example.com", "wrongPassword");
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password.", result.getMessage());
    }

    @Test
    void unknownEmailFails() {
        LoginResult result = handler.login("nobody@example.com", "secret123");
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password.", result.getMessage());
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveIdenticalResponses() {
        LoginResult wrongPassword = handler.login("alice@example.com", "wrongPassword");
        LoginResult unknownEmail = handler.login("nobody@example.com", "wrongPassword");
        assertEquals(wrongPassword.isSuccess(), unknownEmail.isSuccess());
        assertEquals(wrongPassword.getMessage(), unknownEmail.getMessage());
    }

    @Test
    void passwordIsCaseSensitive() {
        LoginResult result = handler.login("alice@example.com", "Secret123");
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password.", result.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void missingEmailIsRejected(String email) {
        LoginResult result = handler.login(email, "secret123");
        assertFalse(result.isSuccess());
        assertEquals("Email is required.", result.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void missingPasswordIsRejected(String password) {
        LoginResult result = handler.login("alice@example.com", password);
        assertFalse(result.isSuccess());
        assertEquals("Password is required.", result.getMessage());
    }

    @Test
    void emailIsValidatedBeforePassword() {
        LoginResult result = handler.login("", "");
        assertEquals("Email is required.", result.getMessage());
    }

    @Test
    void blankInputDoesNotQueryRegistry() {
        handler.login("", "secret123");
        handler.login("alice@example.com", "");
        assertEquals(0, registry.getCallCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"secret123 ", " secret123"})
    void passwordIsNotTrimmed(String password) {
        LoginResult result = handler.login("alice@example.com", password);
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password.", result.getMessage());
    }

    // --- S-006: lengdegrenser, men ingen nye krav for eksisterende brukere ---

    @Test
    void existingUserWithPasswordShorterThan8CharactersCanStillLogIn() {
        registry.addUser(2, "bob", "bob@example.com", "abc");
        assertTrue(handler.login("bob@example.com", "abc").isSuccess());
    }

    @Test
    void existingUserWithOddEmailFormatCanStillLogIn() {
        registry.addUser(2, "bob", "bob", "secret123");
        assertTrue(handler.login("bob", "secret123").isSuccess());
    }

    @Test
    void existingUserWithPasswordLongerThan72BytesCanStillLogIn() {
        String longPassword = "p".repeat(100);   // kunne registreres før S-006
        registry.addUser(2, "bob", "bob@example.com", longPassword);
        assertTrue(handler.login("bob@example.com", longPassword).isSuccess());
    }

    @Test
    void emailOfExactly100CharactersIsLookedUp() {
        String email = "b".repeat(88) + "@example.com";
        registry.addUser(2, "bob", email, "secret123");
        assertTrue(handler.login(email, "secret123").isSuccess());
    }

    @Test
    void emailLongerThan100CharactersIsRejectedWithoutQueryingRegistry() {
        LoginResult result = handler.login("b".repeat(89) + "@example.com", "secret123");
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password.", result.getMessage());
        assertNull(result.getUser());
        assertEquals(0, registry.getCallCount());
    }

    @Test
    void passwordOfExactly256CharactersIsChecked() {
        String password = "p".repeat(256);
        registry.addUser(2, "bob", "bob@example.com", password);
        assertTrue(handler.login("bob@example.com", password).isSuccess());
    }

    @Test
    void passwordLongerThan256CharactersIsRejectedWithoutQueryingRegistry() {
        LoginResult result = handler.login("alice@example.com", "p".repeat(257));
        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password.", result.getMessage());
        assertEquals(0, registry.getCallCount());
    }

    @Test
    void tooLongInputGivesSameResponseAsWrongPassword() {
        LoginResult tooLong = handler.login("alice@example.com", "p".repeat(257));
        LoginResult wrong = handler.login("alice@example.com", "wrongPassword");
        assertEquals(wrong.isSuccess(), tooLong.isSuccess());
        assertEquals(wrong.getMessage(), tooLong.getMessage());
    }

    @Test
    void invalidStoredHashThrowsException() {
        registry.addRawStoredValue(2, "bob", "bob@example.com", "plaintext");
        assertThrows(IllegalArgumentException.class,
                () -> handler.login("bob@example.com", "plaintext"));
    }

    @Test
    void registryExceptionIsPropagated() {
        RuntimeException dbError = new RuntimeException("Database down");
        DataRegistry failingRegistry = new FakeDataRegistry() {
            @Override
            public UserCredentials findUserByEmail(String email) {
                throw dbError;
            }
        };
        LoginHandler failingHandler = new LoginHandler(failingRegistry);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> failingHandler.login("alice@example.com", "secret123"));
        assertSame(dbError, thrown);
    }

    @Test
    void successfulLoginReturnsUser() {
        LoginResult result = handler.login("alice@example.com", "secret123");
        User user = result.getUser();
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
    }

    @Test
    void failedLoginReturnsNoUser() {
        assertNull(handler.login("alice@example.com", "wrongPassword").getUser());
        assertNull(handler.login("nobody@example.com", "secret123").getUser());
        assertNull(handler.login("", "secret123").getUser());
    }
}
