package Login;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tester innlogging, sesjon og utlogging gjennom HTTP, slik nettleseren gjør det.
 * Starter serveren med samme oppsett som WebServer, men med FakeDataRegistry og en ledig port.
 */
class SessionFlowTest {

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
                new ForgotPasswordHandler(registry, new ConsoleResetCodeSender(), Clock.systemUTC()));

        app = Javalin.create(config -> {
            WebServer.configure(config, loginController, registerController, forgotPasswordController, null);
            config.jetty.port = 0;  // ledig port, så testen ikke krasjer med en server som allerede kjører
        }).start();

        cookies = new CookieManager();
        client = HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void meWithoutLoginGives401() throws Exception {
        HttpResponse<String> me = get("/api/me");

        assertEquals(401, me.statusCode());
        assertFalse(json(me).get("success").asBoolean());
    }

    @Test
    void loginCreatesSessionSoMeReturnsUser() throws Exception {
        assertEquals(200, login("kari@example.com", "hemmelig123").statusCode());

        HttpResponse<String> me = get("/api/me");

        assertEquals(200, me.statusCode());
        JsonNode user = json(me).get("user");
        assertEquals("kari@example.com", user.get("email").asText());
        assertEquals("Kari", user.get("fornavn").asText());
        assertEquals("halden husflidslag", user.get("lokallag").asText());
        assertEquals("no-store", me.headers().firstValue("Cache-Control").orElse(""));
    }

    @Test
    void wrongPasswordGives401AndNoSession() throws Exception {
        assertEquals(401, login("kari@example.com", "feil").statusCode());

        assertEquals(401, get("/api/me").statusCode());
        assertTrue(cookies.getCookieStore().getCookies().isEmpty());
    }

    @Test
    void logoutEndsSession() throws Exception {
        login("kari@example.com", "hemmelig123");

        HttpResponse<String> logout = post("/api/logout", "");

        assertEquals(200, logout.statusCode());
        assertTrue(json(logout).get("success").asBoolean());
        assertEquals(401, get("/api/me").statusCode());
    }

    @Test
    void logoutWithoutSessionStillGives200() throws Exception {
        assertEquals(200, post("/api/logout", "").statusCode());
    }

    @Test
    void oldSessionIdCannotBeReusedAfterLogout() throws Exception {
        login("kari@example.com", "hemmelig123");
        String sessionId = sessionCookie();

        post("/api/logout", "");
        // Send den gamle cookien på nytt, som en angriper som har tatt vare på den
        HttpResponse<String> me = client.send(HttpRequest.newBuilder(uri("/api/me"))
                .header("Cookie", "JSESSIONID=" + sessionId).build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(401, me.statusCode());
    }

    @Test
    void newLoginGivesNewSessionId() throws Exception {
        login("kari@example.com", "hemmelig123");
        String first = sessionCookie();

        login("kari@example.com", "hemmelig123");

        assertNotEquals(first, sessionCookie());
    }

    @Test
    void deletedUserIsNotLoggedIn() throws Exception {
        login("kari@example.com", "hemmelig123");

        registry.removeUser("kari@example.com");

        assertEquals(401, get("/api/me").statusCode());
    }

    @Test
    void meShowsUpdatedDataFromDatabase() throws Exception {
        login("kari@example.com", "hemmelig123");
        User old = registry.findUserByEmail("kari@example.com").getUser();

        registry.replaceUser(new User(old.getId(), null, old.getEmail(), "Kari", "Hansen", "moss husflidslag"));

        JsonNode user = json(get("/api/me")).get("user");
        assertEquals("Hansen", user.get("etternavn").asText());
        assertEquals("moss husflidslag", user.get("lokallag").asText());
    }

    @Test
    void sessionCookieIsHttpOnlyAndSameSiteLax() throws Exception {
        HttpResponse<String> response = login("kari@example.com", "hemmelig123");

        String setCookie = response.headers().firstValue("Set-Cookie").orElse("");
        assertTrue(setCookie.startsWith("JSESSIONID="), setCookie);
        assertTrue(setCookie.contains("HttpOnly"), setCookie);
        assertTrue(setCookie.contains("SameSite=Lax"), setCookie);
    }

    @Test
    void responsesNeverContainPasswordHash() throws Exception {
        String loginBody = login("kari@example.com", "hemmelig123").body();
        String meBody = get("/api/me").body();

        for (String body : new String[] {loginBody, meBody}) {
            assertFalse(body.toLowerCase().contains("password"), body);
            assertFalse(body.contains("$2a$"), body);
        }
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

    private String sessionCookie() {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("JSESSIONID"))
                .map(HttpCookie::getValue)
                .findFirst().orElseThrow();
    }

    private static JsonNode json(HttpResponse<String> response) throws Exception {
        return JSON.readTree(response.body());
    }
}
