package Login;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** S-017: PUT /api/user/update gjennom HTTP, med samme oppsett som WebServer og FakeDataRegistry. */
class UserProfileHttpTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String VALID_UPDATE = "{\"fornavn\": \"Kari\", \"etternavn\": \"Hansen\","
            + " \"email\": \"kari.hansen@example.com\", \"lokallag\": \"moss husflidslag\"}";

    private boolean dbDown;
    private FakeDataRegistry registry;
    private Javalin app;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        dbDown = false;
        registry = new FakeDataRegistry() {
            @Override
            public void updateUser(int userId, String fornavn, String etternavn, String email, String lokallag) {
                if (dbDown) {
                    throw new RuntimeException("Database error during user update: Communications link failure");
                }
                super.updateUser(userId, fornavn, etternavn, email, lokallag);
            }
        };
        String hash = BCrypt.hashpw("hemmelig123", BCrypt.gensalt(4));
        registry.addUser("Kari", "Nordmann", "kari@example.com", hash, "halden husflidslag");
        registry.addUser("Ola", "Nordmann", "ola@example.com", hash, "halden husflidslag");

        SessionUser sessionUser = new SessionUser(registry);
        app = Javalin.create(config -> {
            WebServer.configure(config,
                    new LoginController(new LoginHandler(registry), sessionUser),
                    new RegisterController(new RegisterHandler(registry)),
                    new ForgotPasswordController(new ForgotPasswordHandler(registry, new ConsoleResetCodeSender(), Clock.systemUTC())),
                    null);
            WebServer.registerUserRoutes(config, new UserController(registry, sessionUser));
            config.jetty.port = 0;
        }).start();
        client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void updateWithoutLoginGives401() throws Exception {
        HttpResponse<String> response = put(VALID_UPDATE);

        assertEquals(401, response.statusCode(), response.body());
        assertFalse(json(response).get("success").asBoolean());
    }

    @Test
    void updateIsSavedAndSessionShowsNewData() throws Exception {
        login();

        HttpResponse<String> response = put(VALID_UPDATE.replace("kari.hansen@example.com", " Kari.Hansen@Example.com "));

        assertEquals(200, response.statusCode(), response.body());
        assertEquals("Hansen", json(response).get("user").get("etternavn").asText());
        // lagret med normalisert e-post, og den gamle e-posten er frigjort
        assertNotNull(registry.findUserByEmail("kari.hansen@example.com"));
        assertNull(registry.findUserByEmail("kari@example.com"));

        JsonNode me = json(get("/api/me")).get("user");
        assertEquals("kari.hansen@example.com", me.get("email").asText());
        assertEquals("moss husflidslag", me.get("lokallag").asText());
    }

    @Test
    void keepingOwnEmailIsAllowed() throws Exception {
        login();

        HttpResponse<String> response = put(VALID_UPDATE.replace("kari.hansen@example.com", "kari@example.com"));

        assertEquals(200, response.statusCode(), response.body());
    }

    @Test
    void emailOfAnotherUserGives409() throws Exception {
        login();

        HttpResponse<String> response = put(VALID_UPDATE.replace("kari.hansen@example.com", "OLA@example.com"));

        assertEquals(409, response.statusCode(), response.body());
        assertEquals("Email is already registered.", json(response).get("message").asText());
        assertEquals("Nordmann", registry.findUserByEmail("kari@example.com").getUser().getEtternavn());
    }

    @Test
    void emptyNameGives400() throws Exception {
        login();

        HttpResponse<String> response = put(VALID_UPDATE.replace("\"Kari\"", "\"  \""));

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("First name, last name and email are required.", json(response).get("message").asText());
    }

    @Test
    void tooLongNameGives400() throws Exception {
        login();

        HttpResponse<String> response = put(VALID_UPDATE.replace("\"Hansen\"", "\"" + "a".repeat(51) + "\""));

        assertEquals(400, response.statusCode(), response.body());
    }

    @Test
    void invalidEmailGives400() throws Exception {
        login();

        HttpResponse<String> response = put(VALID_UPDATE.replace("kari.hansen@example.com", "kari@"));

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("Please enter a valid email address.", json(response).get("message").asText());
    }

    @Test
    void unknownLocalChapterGives400() throws Exception {
        login();

        HttpResponse<String> response = put(VALID_UPDATE.replace("moss husflidslag", "oslo husflidslag"));

        assertEquals(400, response.statusCode(), response.body());
    }

    @Test
    void invalidJsonGives400() throws Exception {
        login();

        assertEquals(400, put("{fornavn:").statusCode());
        assertEquals(400, put("null").statusCode());
        assertEquals(400, put(VALID_UPDATE.replace("\"Kari\"", "123")).statusCode());   // S-006: tall i tekstfelt
    }

    @Test
    void databaseErrorGives500WithoutDetails() throws Exception {
        login();
        dbDown = true;

        HttpResponse<String> response = put(VALID_UPDATE);

        assertEquals(500, response.statusCode(), response.body());
        assertFalse(response.body().contains("Communications link failure"), response.body());
    }

    // --- Hjelpemetoder ---

    private void login() throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(
                        Map.of("email", "kari@example.com", "password", "hemmelig123")))).build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
    }

    private HttpResponse<String> put(String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri("/api/user/update"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + app.port() + path);
    }

    private static JsonNode json(HttpResponse<String> response) throws Exception {
        return JSON.readTree(response.body());
    }
}
