package Login;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S-007: Tester feilhåndtering gjennom HTTP: databasefeil, ødelagte data, ukjente ruter og statuskoder ved innlogging.
 * Databasen simuleres med FakeDataRegistry, som kan "slås av" med dbDown = true.
 */
class ErrorHandlingHttpTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private boolean dbDown;
    private FakeDataRegistry registry;
    private Javalin app;
    private CookieManager cookies;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        dbDown = false;
        // Kaster samme type feil som DatabaseRegistry når MySQL ikke svarer
        registry = new FakeDataRegistry() {
            @Override
            public UserCredentials findUserByEmail(String email) {
                failIfDbDown();
                return super.findUserByEmail(email);
            }

            @Override
            public User findUserById(int id) {
                failIfDbDown();
                return super.findUserById(id);
            }

            @Override
            public PasswordReset findPasswordReset(String email) {
                failIfDbDown();
                return super.findPasswordReset(email);
            }
        };
        registry.addUser("Kari", "Nordmann", "kari@example.com",
                BCrypt.hashpw("hemmelig123", BCrypt.gensalt(4)), "halden husflidslag");

        LoginController loginController = new LoginController(new LoginHandler(registry), new SessionUser(registry));
        RegisterController registerController = new RegisterController(new RegisterHandler(registry));
        ForgotPasswordController forgotPasswordController = new ForgotPasswordController(
                new ForgotPasswordHandler(registry, (email, code) -> { }, Clock.systemUTC()));

        app = Javalin.create(config -> {
            WebServer.configure(config, loginController, registerController, forgotPasswordController, null);
            config.jetty.port = 0;
            // Testrute som kaster en feil ingen controller fanger, for å teste den sentrale feilhåndteringen
            config.routes.get("/api/test-crash", ctx -> {
                throw new IllegalStateException("intern detalj som ikke skal vises");
            });
        }).start();

        cookies = new CookieManager();
        client = HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    private void failIfDbDown() {
        if (dbDown) {
            throw new RuntimeException("Database error during login lookup: Communications link failure");
        }
    }

    // --- Databasen er nede ---

    @ParameterizedTest(name = "{0} gir 500")
    @CsvSource(delimiter = '|', quoteCharacter = '\'', value = {
            "/api/login           | '{\"email\": \"kari@example.com\", \"password\": \"hemmelig123\"}'",
            "/api/register        | '{\"fornavn\": \"Ola\", \"etternavn\": \"Nordmann\", \"email\": \"ola@example.com\", \"password\": \"hemmelig123\", \"lokallag\": \"moss husflidslag\"}'",
            "/api/forgot-password | '{\"email\": \"kari@example.com\"}'",
            "/api/reset-password  | '{\"email\": \"kari@example.com\", \"code\": \"123456\", \"newPassword\": \"nyttPassord1\"}'"
    })
    void databaseErrorGives500WithGenericMessage(String path, String body) throws Exception {
        dbDown = true;

        HttpResponse<String> response = post(path, body);

        assertEquals(500, response.statusCode());
        assertFalse(json(response).get("success").asBoolean());
        assertTrue(json(response).get("message").asText().endsWith("Please try again later."), response.body());
        assertNoInternalDetails(response);
    }

    @Test
    void databaseErrorOnMeGives500AndKeepsSession() throws Exception {
        login("kari@example.com", "hemmelig123");
        dbDown = true;

        HttpResponse<String> me = get("/api/me");

        assertEquals(500, me.statusCode());
        assertNoInternalDetails(me);
        dbDown = false;
        assertEquals(200, get("/api/me").statusCode());   // sesjonen er ikke slettet på grunn av databasefeilen
    }

    @Test
    void databaseErrorOnAccountGives500HtmlPage() throws Exception {
        login("kari@example.com", "hemmelig123");
        dbDown = true;

        HttpResponse<String> account = get("/account");

        assertEquals(500, account.statusCode());
        assertTrue(account.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
        assertTrue(account.body().contains("Prøv igjen senere"), account.body());
        assertNoInternalDetails(account);
    }

    // --- Ødelagte data i databasen ---

    @Test
    void corruptResetCodeHashGives500WithoutInternalMessage() throws Exception {
        int userId = registry.findUserByEmail("kari@example.com").getUser().getId();
        registry.saveResetCode(userId, "ikke-en-bcrypt-hash", Instant.now().plusSeconds(600));

        HttpResponse<String> response = post("/api/reset-password",
                "{\"email\": \"kari@example.com\", \"code\": \"123456\", \"newPassword\": \"nyttPassord1\"}");

        assertEquals(500, response.statusCode());
        assertFalse(response.body().toLowerCase().contains("salt"), response.body());
    }

    // --- Ukjente ruter og uventede feil ---

    @ParameterizedTest(name = "{0} {1} gir 404 JSON")
    @CsvSource({
            "GET,  /api/finnes-ikke",
            "POST, /api/finnes-ikke",
            "GET,  /api/login"          // ruten finnes bare som POST
    })
    void unknownApiRouteGives404Json(String method, String path) throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri(path))
                .method(method, HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertFalse(json(response).get("success").asBoolean());
        assertEquals("Not found.", json(response).get("message").asText());
    }

    @Test
    void staticPagesStillWork() throws Exception {
        assertEquals(200, get("/login.html").statusCode());
    }

    @Test
    void unexpectedErrorIsHandledCentrallyWith500() throws Exception {
        HttpResponse<String> response = get("/api/test-crash");

        assertEquals(500, response.statusCode());
        assertEquals("Something went wrong. Please try again later.", json(response).get("message").asText());
        assertFalse(response.body().contains("intern detalj"), response.body());
    }

    // --- Statuskoder ved innlogging ---

    @ParameterizedTest(name = "[{0}] gir 400")
    @CsvSource(delimiter = '|', quoteCharacter = '\'', value = {
            "'{\"email\": \"\", \"password\": \"hemmelig123\"}'       | Email is required.",
            "'{\"email\": \"kari@example.com\", \"password\": \"  \"}' | Password is required.",
            "'{\"email\": \"kari@example.com\"}'                       | Password is required."
    })
    void loginWithMissingFieldGives400(String body, String expectedMessage) throws Exception {
        HttpResponse<String> response = post("/api/login", body);

        assertEquals(400, response.statusCode());
        assertEquals(expectedMessage, json(response).get("message").asText());
        assertTrue(cookies.getCookieStore().getCookies().isEmpty());
    }

    @ParameterizedTest(name = "{0} / feil passord gir 401")
    @CsvSource({"kari@example.com, feilPassord1", "ukjent@example.com, hemmelig123"})
    void wrongPasswordOrUnknownUserStillGives401(String email, String password) throws Exception {
        HttpResponse<String> response = login(email, password);

        assertEquals(401, response.statusCode());
        assertEquals("Invalid email or password.", json(response).get("message").asText());
    }

    @Test
    void loginFormWithMissingFieldGives400() throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("email=kari%40example.com&password=")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Password is required."));
    }

    @Test
    void loginFormWithValidCredentialsStillRedirectsToAccount() throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("email=kari%40example.com&password=hemmelig123")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(303, response.statusCode());
        assertEquals("/account", response.headers().firstValue("Location").orElse(""));
    }

    // --- Hjelpemetoder ---

    private void assertNoInternalDetails(HttpResponse<String> response) {
        String body = response.body();
        assertFalse(body.contains("Communications link failure"), body);
        assertFalse(body.contains("Exception"), body);
        assertFalse(body.contains("at Login."), body);   // ingen stack trace
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
