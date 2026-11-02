package Login;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mindrot.jbcrypt.BCrypt;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S-006: Tester inputvalidering gjennom HTTP, slik frontend sender forespørslene.
 * Bruker samme oppsett som WebServer, men med FakeDataRegistry (ingen database).
 */
class InputValidationHttpTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String[] JSON_ENDPOINTS = {
            "/api/login", "/api/register", "/api/forgot-password", "/api/reset-password"};

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

    // --- Feilformatert JSON ---

    static Stream<Arguments> malformedBodies() {
        String[] bodies = {"", "   ", "{", "{\"email\": ", "not json", "null", "[]", "[{\"email\":\"kari@example.com\"}]", "\"kari@example.com\"", "42"};
        return Stream.of(JSON_ENDPOINTS).flatMap(path -> Stream.of(bodies).map(body -> Arguments.of(path, body)));
    }

    @ParameterizedTest(name = "{0} med body [{1}] gir 400")
    @MethodSource("malformedBodies")
    void malformedJsonGives400(String path, String body) throws Exception {
        HttpResponse<String> response = post(path, body);

        assertEquals(400, response.statusCode(), response.body());
        assertFalse(json(response).get("success").asBoolean());
        assertEquals("Invalid request.", json(response).get("message").asText());
        assertTrue(cookies.getCookieStore().getCookies().isEmpty());
    }

    // --- Feil datatype i tekstfelt ---

    static Stream<Arguments> wrongTypes() {
        return Stream.of(
                Arguments.of("/api/login", "{\"email\": 123, \"password\": \"hemmelig123\"}"),
                Arguments.of("/api/login", "{\"email\": \"kari@example.com\", \"password\": 12345678}"),
                Arguments.of("/api/login", "{\"email\": true, \"password\": \"hemmelig123\"}"),
                Arguments.of("/api/login", "{\"email\": \"kari@example.com\", \"password\": 1.5}"),
                Arguments.of("/api/login", "{\"email\": [\"kari@example.com\"], \"password\": \"hemmelig123\"}"),
                Arguments.of("/api/login", "{\"email\": {\"a\": 1}, \"password\": \"hemmelig123\"}"),
                Arguments.of("/api/register", "{\"fornavn\": 1, \"etternavn\": \"Nordmann\", \"email\": \"ola@example.com\","
                        + " \"password\": \"hemmelig123\", \"lokallag\": \"moss husflidslag\"}"),
                Arguments.of("/api/register", "{\"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\", \"email\": \"ola@example.com\","
                        + " \"password\": 12345678, \"lokallag\": \"moss husflidslag\"}"),
                Arguments.of("/api/register", "{\"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\", \"email\": \"ola@example.com\","
                        + " \"password\": \"hemmelig123\", \"lokallag\": false}"),
                Arguments.of("/api/forgot-password", "{\"email\": 42}"),
                Arguments.of("/api/forgot-password", "{\"email\": [\"kari@example.com\"]}"),
                Arguments.of("/api/reset-password", "{\"email\": \"kari@example.com\", \"code\": 123456, \"newPassword\": \"nyttPassord1\"}"),
                Arguments.of("/api/reset-password", "{\"email\": \"kari@example.com\", \"code\": \"123456\", \"newPassword\": true}"));
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("wrongTypes")
    void wrongDataTypeGives400(String path, String body) throws Exception {
        HttpResponse<String> response = post(path, body);

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("Invalid request.", json(response).get("message").asText());
        assertTrue(cookies.getCookieStore().getCookies().isEmpty());
    }

    @Test
    void wrongDataTypeDoesNotRegisterUser() throws Exception {
        post("/api/register", "{\"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\", \"email\": \"ola@example.com\","
                + " \"password\": 12345678, \"lokallag\": \"moss husflidslag\"}");

        assertNull(registry.findUserByEmail("ola@example.com"));
    }

    @Test
    void jsonNullInFieldIsTreatedAsMissing() throws Exception {
        HttpResponse<String> response = post("/api/login", "{\"email\": null, \"password\": \"hemmelig123\"}");

        assertEquals(401, response.statusCode());
        assertEquals("Email is required.", json(response).get("message").asText());
    }

    /** Javalin 7 avviser ukjente felt allerede før S-006. Testen låser den oppførselen, så frontend må sende nøyaktige feltnavn. */
    @Test
    void unknownFieldsGive400() throws Exception {
        HttpResponse<String> response = post("/api/login",
                "{\"email\": \"kari@example.com\", \"password\": \"hemmelig123\", \"remember\": true}");

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("Invalid request.", json(response).get("message").asText());
        assertTrue(cookies.getCookieStore().getCookies().isEmpty());
    }

    // --- Registrering ---

    /** Passord som bare består av siffer er fortsatt gyldige, så lenge de sendes som tekst i JSON. */
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
        assertEquals("Please enter a valid email address.", json(response).get("message").asText());
        assertNull(registry.findUserByEmail("ola.example.com"));
    }

    @Test
    void registerWithOneCharacterPasswordGives400() throws Exception {
        HttpResponse<String> response = register("ola@example.com", "a");

        assertEquals(400, response.statusCode());
        assertEquals("Password must be at least 8 characters.", json(response).get("message").asText());
        assertNull(registry.findUserByEmail("ola@example.com"));
    }

    @Test
    void registerWithTooLongPasswordGives400() throws Exception {
        HttpResponse<String> response = register("ola@example.com", "a".repeat(73));

        assertEquals(400, response.statusCode());
        assertEquals("Password cannot be longer than 72 bytes.", json(response).get("message").asText());
    }

    // --- Glemt passord / tilbakestilling ---

    @Test
    void forgotPasswordWithInvalidEmailGives400() throws Exception {
        HttpResponse<String> response = post("/api/forgot-password", "{\"email\": \"kari@\"}");

        assertEquals(400, response.statusCode());
        assertEquals("Please enter a valid email address.", json(response).get("message").asText());
    }

    @Test
    void forgotPasswordWithValidEmailStillGivesGenericAnswer() throws Exception {
        HttpResponse<String> known = post("/api/forgot-password", "{\"email\": \"kari@example.com\"}");
        HttpResponse<String> unknown = post("/api/forgot-password", "{\"email\": \"nobody@example.com\"}");

        assertEquals(200, known.statusCode());
        assertEquals(known.body(), unknown.body());
    }

    @Test
    void resetPasswordWithNonNumericCodeGives400() throws Exception {
        HttpResponse<String> response = post("/api/reset-password", JSON.writeValueAsString(Map.of(
                "email", "kari@example.com", "code", "abcdef", "newPassword", "nyttPassord1")));

        assertEquals(400, response.statusCode());
        assertEquals("Invalid or expired reset code.", json(response).get("message").asText());
    }

    @Test
    void resetPasswordWithShortNewPasswordGives400() throws Exception {
        HttpResponse<String> response = post("/api/reset-password", JSON.writeValueAsString(Map.of(
                "email", "kari@example.com", "code", "123456", "newPassword", "kort")));

        assertEquals(400, response.statusCode());
        assertEquals("New password must be at least 8 characters.", json(response).get("message").asText());
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
    void loginWithTooLongEmailGivesGeneric401() throws Exception {
        HttpResponse<String> response = login("a".repeat(101) + "@example.com", "hemmelig123");

        assertEquals(401, response.statusCode());
        assertEquals("Invalid email or password.", json(response).get("message").asText());
        assertTrue(cookies.getCookieStore().getCookies().isEmpty());
    }

    @Test
    void loginWithTooLongPasswordGivesGeneric401() throws Exception {
        HttpResponse<String> response = login("kari@example.com", "a".repeat(257));

        assertEquals(401, response.statusCode());
        assertEquals("Invalid email or password.", json(response).get("message").asText());
    }

    // --- Hjelpemetoder ---

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

    private static JsonNode json(HttpResponse<String> response) throws Exception {
        return JSON.readTree(response.body());
    }
}
