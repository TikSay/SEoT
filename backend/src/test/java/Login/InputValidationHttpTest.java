package Login;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mindrot.jbcrypt.BCrypt;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S-006: Tester det som skjer i JSON- og controllerlaget: statuskoder og meldinger.
 * Selve valideringsreglene testes i InputValidatorTest.
 * Bruker samme oppsett som WebServer, men med FakeDataRegistry (ingen database).
 */
class InputValidationHttpTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private FakeDataRegistry registry;
    private Javalin app;
    private CookieManager cookies;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        registry = new FakeDataRegistry();
        registry.addUser("Kari", "Nordmann", "kari@example.com",
                BCrypt.hashpw("hemmelig123", BCrypt.gensalt(4)),
                "halden husflidslag");

        LoginController loginController = new LoginController(new LoginHandler(registry), new SessionUser(registry));
        RegisterController registerController = new RegisterController(new RegisterHandler(registry));
        ForgotPasswordController forgotPasswordController = new ForgotPasswordController(
                new ForgotPasswordHandler(registry, (email, code) -> { }, Clock.systemUTC()));

        app = Javalin.create(config -> {
            WebServer.configure(config, loginController, registerController, forgotPasswordController, null);
            config.jetty.port = 0;
        }).start();

        cookies = new CookieManager();
        client = HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    // --- Feilformatert JSON (fungerte før S-006, testene låser oppførselen) ---

    @ParameterizedTest(name = "{0} med body [{1}] gir 400")
    @CsvSource(delimiter = '|', quoteCharacter = '\'', value = {
            "/api/login           | ''",          // tom body
            "/api/login           | '{'",         // avkuttet JSON
            "/api/register        | 'not json'",  // ikke JSON
            "/api/register        | 'null'",      // JSON null
            "/api/forgot-password | '[]'",        // liste i stedet for objekt
            "/api/reset-password  | '42'"         // tall i stedet for objekt
    })
    void malformedJsonGives400(String path, String body) throws Exception {
        assertInvalidRequest(post(path, body));
    }

    // --- Feil datatype i tekstfelt (nytt i S-006) ---

    @ParameterizedTest(name = "{0}: {1}")
    @CsvSource(delimiter = '|', quoteCharacter = '\'', value = {
            "/api/login           | '{\"email\": 123, \"password\": \"hemmelig123\"}'",                                   // heltall
            "/api/login           | '{\"email\": \"kari@example.com\", \"password\": 1.5}'",                             // desimaltall
            "/api/login           | '{\"email\": true, \"password\": \"hemmelig123\"}'",                                 // boolsk
            "/api/forgot-password | '{\"email\": [\"kari@example.com\"]}'",                                              // liste
            "/api/reset-password  | '{\"email\": \"kari@example.com\", \"code\": 123456, \"newPassword\": \"nyttPassord1\"}'"
    })
    void wrongDataTypeGives400(String path, String body) throws Exception {
        assertInvalidRequest(post(path, body));
    }

    @Test
    void numberAsPasswordDoesNotRegisterUser() throws Exception {
        HttpResponse<String> response = post("/api/register", "{\"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\","
                + " \"email\": \"ola@example.com\", \"password\": 12345678, \"lokallag\": \"moss husflidslag\"}");

        assertInvalidRequest(response);
        assertNull(registry.findUserByEmail("ola@example.com"));
    }

    @Test
    void jsonNullInFieldIsTreatedAsMissing() throws Exception {
        HttpResponse<String> response = post("/api/login", "{\"email\": null, \"password\": \"hemmelig123\"}");

        assertEquals(401, response.statusCode());
        assertEquals("Email is required.", message(response));
    }

    /** Javalin 7 avviser ukjente felt allerede før S-006, så frontend må sende nøyaktige feltnavn. */
    @Test
    void unknownFieldsGive400() throws Exception {
        assertInvalidRequest(post("/api/login",
                "{\"email\": \"kari@example.com\", \"password\": \"hemmelig123\", \"remember\": true}"));
    }

    // --- Valideringsfeil gir 400 med forklarende melding ---

    /** Passord som bare består av siffer er fortsatt gyldige, så lenge de sendes som tekst. */
    @Test
    void registerWithValidInputGives201() throws Exception {
        HttpResponse<String> response = register("ola@example.com", "12345678");

        assertEquals(201, response.statusCode(), response.body());
        assertTrue(BCrypt.checkpw("12345678", registry.findUserByEmail("ola@example.com").getPasswordHash()));
    }

    @Test
    void registerWithInvalidEmailGives400() throws Exception {
        HttpResponse<String> response = register("ola.example.com", "hemmelig123");

        assertEquals(400, response.statusCode());
        assertEquals("Please enter a valid email address.", message(response));
    }

    @Test
    void registerWithOneCharacterPasswordGives400() throws Exception {
        HttpResponse<String> response = register("ola@example.com", "a");

        assertEquals(400, response.statusCode());
        assertEquals("Password must be at least 8 characters.", message(response));
        assertNull(registry.findUserByEmail("ola@example.com"));
    }

    @Test
    void forgotPasswordWithInvalidEmailGives400() throws Exception {
        HttpResponse<String> response = post("/api/forgot-password", "{\"email\": \"kari@\"}");

        assertEquals(400, response.statusCode());
        assertEquals("Please enter a valid email address.", message(response));
    }

    @Test
    void resetPasswordWithNonNumericCodeGives400() throws Exception {
        HttpResponse<String> response = post("/api/reset-password", JSON.writeValueAsString(Map.of(
                "email", "kari@example.com", "code", "abcdef", "newPassword", "nyttPassord1")));

        assertEquals(400, response.statusCode());
        assertEquals("Invalid or expired reset code.", message(response));
    }

    // --- Innlogging (S-005 skal fortsatt virke) ---

    @Test
    void existingUserWithShortPasswordCanStillLogIn() throws Exception {
        registry.addUser("Gammel", "Bruker", "gammel@example.com", BCrypt.hashpw("abc", BCrypt.gensalt(4)), "moss husflidslag");

        HttpResponse<String> response = login("gammel@example.com", "abc");

        assertEquals(200, response.statusCode(), response.body());
        assertEquals(200, get("/api/me").statusCode());
    }

    @Test
    void loginWithTooLongPasswordGivesGeneric401() throws Exception {
        HttpResponse<String> response = login("kari@example.com", "a".repeat(257));

        assertEquals(401, response.statusCode());
        assertEquals("Invalid email or password.", message(response));
    }

    // --- Hjelpemetoder ---

    /** 400 "Invalid request." og ingen sesjon. */
    private void assertInvalidRequest(HttpResponse<String> response) throws Exception {
        assertEquals(400, response.statusCode(), response.body());
        assertEquals("Invalid request.", message(response));
        assertTrue(cookies.getCookieStore().getCookies().isEmpty());
    }

    private HttpResponse<String> register(String email, String password) throws Exception {
        return post("/api/register", JSON.writeValueAsString(Map.of(
                "fornavn", "Ola", "etternavn", "Nordmann", "email", email,
                "password", password, "lokallag", "moss husflidslag")));
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        return post("/api/login", JSON.writeValueAsString(Map.of("email", email, "password", password)));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + app.port() + path);
    }

    private static String message(HttpResponse<String> response) throws Exception {
        return JSON.readTree(response.body()).get("message").asText();
    }
}
